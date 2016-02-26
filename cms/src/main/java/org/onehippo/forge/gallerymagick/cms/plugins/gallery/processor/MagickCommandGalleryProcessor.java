/*
 * Copyright 2016-2016 Hippo B.V. (http://www.onehippo.com)
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
import java.io.OutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hippoecm.frontend.editor.plugins.resource.MimeTypeHelper;
import org.hippoecm.frontend.editor.plugins.resource.ResourceHelper;
import org.hippoecm.frontend.plugins.gallery.imageutil.ScalingParameters;
import org.hippoecm.frontend.plugins.gallery.model.GalleryException;
import org.hippoecm.frontend.plugins.gallery.processor.AbstractGalleryProcessor;
import org.hippoecm.repository.gallery.HippoGalleryNodeType;
import org.onehippo.forge.gallerymagick.core.ImageDimension;
import org.onehippo.forge.gallerymagick.core.command.GraphicsMagickCommandUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link GalleryProcessor} implementation using Gallery Magick Forge library.
 */
public class MagickCommandGalleryProcessor extends AbstractGalleryProcessor {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(MagickCommandGalleryProcessor.class);

    private static final String MAGICK_COMMAND_TEMP_FILE_PREFIX = "_magickproc";

    private static ThreadLocal<File> tlSourceDataFile = new ThreadLocal<>();

    private final Map<String, ScalingParameters> scalingParametersMap = new HashMap<>();

    public MagickCommandGalleryProcessor(final Map<String, ScalingParameters> initScalingParametersMap) {
        if (initScalingParametersMap != null) {
            scalingParametersMap.putAll(initScalingParametersMap);
        }
    }

    @Override
    public void initGalleryNode(Node node, InputStream data, String mimeType, String fileName)
            throws RepositoryException {
        node.setProperty(HippoGalleryNodeType.IMAGE_SET_FILE_NAME, fileName);
    }

    @Override
    public void makeImage(Node node, InputStream data, String mimeType, String fileName)
            throws GalleryException, RepositoryException {
        File sourceFile = null;
        OutputStream sourceFileOut = null;

        try {
            sourceFile = File.createTempFile(MAGICK_COMMAND_TEMP_FILE_PREFIX,
                    "." + FilenameUtils.getExtension(fileName));
            sourceFileOut = new FileOutputStream(sourceFile);
            IOUtils.copy(data, sourceFileOut);
            sourceFileOut.close();
            sourceFileOut = null;

            data.close();
            data = null;
            data = new FileInputStream(sourceFile);

            tlSourceDataFile.set(sourceFile);

            super.makeImage(node, data, mimeType, fileName);
        } catch (IOException e) {
            throw new GalleryException(e.toString(), e);
        } finally {
            IOUtils.closeQuietly(sourceFileOut);

            if (sourceFile != null) {
                log.debug("Removing the original image file at '{}'.", sourceFile);
                sourceFile.delete();
            }

            tlSourceDataFile.remove();
        }
    }

    @Override
    public void initGalleryResource(final Node node, final InputStream data, final String mimeType,
            final String fileName, final Calendar lastModified) throws RepositoryException {
        node.setProperty("jcr:mimeType", mimeType);
        node.setProperty("jcr:lastModified", lastModified);

        final String nodeName = node.getName();
        final File sourceFile = tlSourceDataFile.get();

        if (sourceFile == null) {
            throw new IllegalStateException("Original image source file not provided.");
        }

        File targetFile = null;
        File targetTempFile = null;

        if (MimeTypeHelper.isImageMimeType(mimeType)) {
            final ScalingParameters scalingParameters = getScalingParametersMap().get(nodeName);

            if (scalingParameters != null && scalingParameters.getWidth() > 0 && scalingParameters.getHeight() > 0) {
                try {
                    targetTempFile = File.createTempFile(
                            MAGICK_COMMAND_TEMP_FILE_PREFIX + "_" + StringUtils.replace(nodeName, ":", "_"),
                            "." + FilenameUtils.getExtension(fileName));

                    ImageDimension dimension = ImageDimension.from(scalingParameters.getWidth(),
                            scalingParameters.getHeight());
                    log.debug("Resizing the original image file ('{}') to '{}' with dimension, {}.", sourceFile,
                            targetTempFile, dimension);
                    GraphicsMagickCommandUtils.resizeImage(sourceFile, targetTempFile, dimension);
                    targetFile = targetTempFile;
                    targetTempFile = null;
                } catch (Exception e) {
                    log.warn("Scaling failed, using original image instead", e);
                }
            } else {
                log.debug(
                        "No scaling parameters specified for {} or width or height is zero or negative. So use original image",
                        nodeName);
            }
        } else {
            log.debug("Unknown image MIME type: {}, using raw data", mimeType);
        }

        ImageDimension dimension = null;
        InputStream imageFileIn = null;
        BufferedInputStream imageBufIn = null;
        Binary imageBinary = null;

        try {
            if (targetFile != null) {
                dimension = GraphicsMagickCommandUtils.identifyDimension(targetFile);
                imageFileIn = new FileInputStream(targetFile);
            } else {
                dimension = GraphicsMagickCommandUtils.identifyDimension(sourceFile);
                imageFileIn = new FileInputStream(sourceFile);
            }

            imageBufIn = new BufferedInputStream(imageFileIn);
            imageBinary = ResourceHelper.getValueFactory(node).createBinary(imageBufIn);

            log.debug("Storing an image binary at '{}' from file at '{}'.", node.getPath(),
                    targetFile != null ? targetFile : sourceFile);

            node.setProperty("jcr:data", imageBinary);
            node.setProperty(HippoGalleryNodeType.IMAGE_WIDTH, (long) dimension.getWidth());
            node.setProperty(HippoGalleryNodeType.IMAGE_HEIGHT, (long) dimension.getHeight());
        } catch (IOException e) {
            log.error("Failed to store an image variant due to IO error.", e);
        } finally {
            try {
                if (imageBinary != null) {
                    imageBinary.dispose();
                }
            } catch (Exception ignore) {
            }

            IOUtils.closeQuietly(imageBufIn);
            IOUtils.closeQuietly(imageFileIn);

            if (targetTempFile != null) {
                targetTempFile.delete();
            }

            if (targetFile != null) {
                targetFile.delete();
            }
        }
    }

    @Override
    public Dimension getDesiredResourceDimension(Node resource) throws GalleryException, RepositoryException {
        String nodeName = resource.getName();
        ScalingParameters params = scalingParametersMap.get(nodeName);

        if (params != null) {
            int width = params.getWidth();
            int height = params.getHeight();
            return new Dimension(width, height);
        } else {
            return null;
        }
    }

    @Override
    public Map<String, ScalingParameters> getScalingParametersMap() throws RepositoryException {
        return scalingParametersMap;
    }
}