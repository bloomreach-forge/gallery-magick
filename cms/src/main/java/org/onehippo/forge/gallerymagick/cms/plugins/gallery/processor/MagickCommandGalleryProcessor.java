/*
 * Copyright 2024 Bloomreach B.V. (http://www.bloomreach.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.gallerymagick.cms.plugins.gallery.processor;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hippoecm.frontend.editor.plugins.resource.MimeTypeHelper;
import org.hippoecm.frontend.editor.plugins.resource.ResourceHelper;
import org.hippoecm.frontend.plugins.gallery.imageutil.ImageOperation;
import org.hippoecm.frontend.plugins.gallery.imageutil.ImageOperationResult;
import org.hippoecm.frontend.plugins.gallery.imageutil.ScaleImageOperationFactory;
import org.hippoecm.frontend.plugins.gallery.imageutil.ScalingParameters;
import org.hippoecm.frontend.plugins.gallery.model.GalleryException;
import org.hippoecm.frontend.plugins.gallery.model.GalleryProcessor;
import org.hippoecm.frontend.plugins.gallery.processor.AbstractGalleryProcessor;
import org.hippoecm.frontend.plugins.gallery.util.ImageGalleryUtils;
import org.hippoecm.repository.HippoStdNodeType;
import org.hippoecm.repository.gallery.HippoGalleryNodeType;
import org.onehippo.forge.gallerymagick.core.ImageDimension;
import org.onehippo.forge.gallerymagick.core.command.GraphicsMagickCommandUtils;
import org.onehippo.forge.gallerymagick.core.command.ImageMagickCommandUtils;
import org.onehippo.forge.gallerymagick.core.command.MagickExecuteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link GalleryProcessor} implementation using Gallery Magick Forge library.
 */
public class MagickCommandGalleryProcessor extends AbstractGalleryProcessor {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(MagickCommandGalleryProcessor.class);

    private static final String MAGICK_COMMAND_TEMP_FILE_PREFIX = "_magickproc";

    private static final String GALLERY_MAGICK_METADATA_PROP_NAME = "gallerymagick.metadata";

    private static ThreadLocal<Path> tlSourceDataFile = new ThreadLocal<>();

    private final Map<String, ScalingParameters> scalingParametersMap = new HashMap<>();

    private final String magickImageProcessor;

    public MagickCommandGalleryProcessor(final String magickImageProcessor, final Map<String, ScalingParameters> initScalingParametersMap) {
        this.magickImageProcessor = magickImageProcessor;

        if (initScalingParametersMap != null) {
            scalingParametersMap.putAll(initScalingParametersMap);
        }
    }

    @Override
    public void initGalleryNode(Node node, InputStream data, String mimeType, String fileName) throws RepositoryException {
        node.setProperty(HippoGalleryNodeType.IMAGE_SET_FILE_NAME, fileName);
    }

    @Override
    public void makeImage(Node node, InputStream data, String mimeType, String fileName) throws GalleryException, RepositoryException {
        Path sourceFile = null;

        try {
            sourceFile = saveOriginalImageDataToFile(data, fileName);
            extractAndSaveImageMetadata(node, sourceFile);

            try (InputStream sourceFileInput = Files.newInputStream(sourceFile, StandardOpenOption.READ)) {
                tlSourceDataFile.set(sourceFile);
                super.makeImage(node, sourceFileInput, mimeType, fileName);
            }
        } catch (IOException e) {
            throw new GalleryException(e.toString(), e);
        } finally {
            tlSourceDataFile.remove();

            IOUtils.closeQuietly(data);

            if (sourceFile != null) {
                try {
                    Files.delete(sourceFile);
                } catch (IOException e) {
                    log.error("Failed to delete an image file due to IO error.", e);
                }
            }
        }
    }

    @Override
    public void initGalleryResource(final Node node, final InputStream data, final String mimeType, final String fileName, final Calendar lastModified)
            throws RepositoryException {
        node.setProperty("jcr:mimeType", mimeType);
        node.setProperty("jcr:lastModified", lastModified);
        final String nodeName = node.getName();

        final ScalingParameters parameters = getScalingParametersMap().get(nodeName);
        if (MimeTypeHelper.isSvgMimeType(mimeType)) {
            try {
                final ImageOperation operation = ScaleImageOperationFactory.getOperation(parameters, mimeType);
                final ImageOperationResult result = operation.run(data, mimeType);
                ImageGalleryUtils.saveImageNode(node, result.getData(), result.getWidth(), result.getHeight());
            } catch (GalleryException e) {
                log.warn("Scaling failed, using original image instead", e);
                ImageGalleryUtils.saveImageNode(node, data, 0, 0);
            }
            return;
        }

        boolean sourceFileCreated = false;
        Path sourceFile = tlSourceDataFile.get();

        if (sourceFile == null) {
            // sourceFile can be null sometimes when a user clicks on 'Restore' button to restore thumbnail in UI.
            try {
                sourceFile = saveOriginalImageDataToFile(data, fileName);
                sourceFileCreated = true;
            } catch (IOException e) {
                throw new MagickRuntimeException(e.getMessage(), e);
            }
        }

        Path targetFile = null;
        Path targetTempFile = null;

        if (MimeTypeHelper.isImageMimeType(mimeType)) {

            if (parameters != null && parameters.getWidth() > 0 && parameters.getHeight() > 0) {
                try {
                    targetTempFile = Files.createTempFile(MAGICK_COMMAND_TEMP_FILE_PREFIX + "_" + org.apache.commons.lang3.StringUtils.replace(nodeName, ":", "_"),
                            "." + FilenameUtils.getExtension(fileName));

                    ImageDimension dimension = ImageDimension.from(parameters.getWidth(), parameters.getHeight());
                    log.debug("Resizing the original image file ('{}') to '{}' with dimension, {}.", sourceFile, targetTempFile, dimension);
                    resizeImage(sourceFile, targetTempFile, dimension);
                    targetFile = targetTempFile;
                    targetTempFile = null;
                } catch (Exception e) {
                    log.warn("Scaling failed, using original image instead", e);
                }
            } else {
                log.debug("No scaling parameters specified for {} or width or height is zero or negative. So use original image", nodeName);
            }
        } else {
            log.debug("Unknown image MIME type: {}, using raw data", mimeType);
        }

        try {
            Path fileToProcess = (targetFile != null) ? targetFile : sourceFile;
            ImageDimension dimension = identifyDimension(fileToProcess);

            try (InputStream imageFileIn = Files.newInputStream(fileToProcess); BufferedInputStream imageBufIn = new BufferedInputStream(imageFileIn)) {
                Binary imageBinary = ResourceHelper.getValueFactory(node).createBinary(imageBufIn);

                log.debug("Storing an image binary at '{}' from file at '{}'.", node.getPath(), fileToProcess);

                node.setProperty("jcr:data", imageBinary);
                node.setProperty(HippoGalleryNodeType.IMAGE_WIDTH, dimension.getWidth());
                node.setProperty(HippoGalleryNodeType.IMAGE_HEIGHT, dimension.getHeight());

                if (imageBinary != null) {
                    imageBinary.dispose();
                }
            }
        } catch (IOException e) {
            log.error("Failed to store an image variant due to IO error.", e);
        } finally {
            try {
                if (targetTempFile != null) {
                    Files.delete(targetTempFile);
                }

                if (targetFile != null) {
                    Files.delete(targetFile);
                }

                if (sourceFileCreated) {
                    Files.delete(sourceFile);
                }
            } catch (IOException e) {
                log.error("Failed to delete an image file due to IO error.", e);
            }
        }
    }

    @Override
    public Dimension getDesiredResourceDimension(Node resource) throws RepositoryException {
        final String nodeName = resource.getName();
        final ScalingParameters parameters = scalingParametersMap.get(nodeName);
        if (parameters != null) {
            return new Dimension(parameters.getWidth(), parameters.getHeight());
        } else {
            log.warn("No scaling parameters found for: {}.", nodeName);
            return null;
        }
    }

    @Override
    public Map<String, ScalingParameters> getScalingParametersMap() {
        return scalingParametersMap;
    }

    protected boolean isImageMagickImageProcessor() {
        return org.apache.commons.lang.StringUtils.equalsIgnoreCase("ImageMagick", magickImageProcessor);
    }

    protected void resizeImage(Path sourceFile, Path targetFile, ImageDimension dimension) throws IOException {
        if (isImageMagickImageProcessor()) {
            ImageMagickCommandUtils.resizeImage(sourceFile, targetFile, dimension);
        } else {
            GraphicsMagickCommandUtils.resizeImage(sourceFile, targetFile, dimension);
        }
    }

    protected String identifyAllMetadata(Path sourceFile) throws IOException {
        if (isImageMagickImageProcessor()) {
            return ImageMagickCommandUtils.identifyAllMetadata(sourceFile);
        } else {
            return GraphicsMagickCommandUtils.identifyAllMetadata(sourceFile);
        }
    }

    protected ImageDimension identifyDimension(Path sourceFile) throws IOException {
        if (isImageMagickImageProcessor()) {
            return ImageMagickCommandUtils.identifyDimension(sourceFile);
        } else {
            return GraphicsMagickCommandUtils.identifyDimension(sourceFile);
        }
    }

    protected void extractAndSaveImageMetadata(Node node, Path sourceFile) {
        String nodePath = null;

        try {
            final String imageMetadata = identifyAllMetadata(sourceFile);
            nodePath = node.getPath();

            if (!node.isNodeType(HippoStdNodeType.NT_RELAXED)) {
                node.addMixin(HippoStdNodeType.NT_RELAXED);
                node.setProperty(GALLERY_MAGICK_METADATA_PROP_NAME, StringUtils.defaultString(imageMetadata));
            }
        } catch (Exception e) {
            log.error("Failed to extract image metadata or failed to store the metadata in '{}' property at '{}'.", GALLERY_MAGICK_METADATA_PROP_NAME, nodePath,
                    e);
        }
    }

    private Path saveOriginalImageDataToFile(final InputStream dataInput, final String fileName) throws IOException {
        Path sourceFile = Files.createTempFile(MAGICK_COMMAND_TEMP_FILE_PREFIX, "." + FilenameUtils.getExtension(fileName));
        Files.copy(dataInput, sourceFile, StandardCopyOption.REPLACE_EXISTING);
        return sourceFile;
    }
}
