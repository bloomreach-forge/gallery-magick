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
import org.hippoecm.frontend.editor.plugins.resource.ResourceHelper;
import org.hippoecm.frontend.plugins.gallery.imageutil.ScalingParameters;
import org.hippoecm.frontend.plugins.gallery.model.GalleryException;
import org.hippoecm.frontend.plugins.gallery.processor.AbstractGalleryProcessor;
import org.hippoecm.repository.gallery.HippoGalleryNodeType;
import org.onehippo.forge.gallerymagick.core.ImageDimension;
import org.onehippo.forge.gallerymagick.core.command.GraphicsMagickCommandUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MagickCommandGalleryProcessor extends AbstractGalleryProcessor {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(MagickCommandGalleryProcessor.class);

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
            sourceFile = File.createTempFile("_magickproc", "." + FilenameUtils.getExtension(fileName));
            sourceFileOut = new FileOutputStream(sourceFile);
            IOUtils.copy(data, sourceFileOut);
            sourceFileOut.close();
            sourceFileOut = null;
            tlSourceDataFile.set(sourceFile);
            super.makeImage(node, data, mimeType, fileName);
        } catch (IOException e) {
            throw new GalleryException(e.toString(), e);
        } finally {
            IOUtils.closeQuietly(sourceFileOut);

            if (sourceFile != null) {
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
        final ScalingParameters scalingParameters = getScalingParametersMap().get(nodeName);

        File targetFile = null;
        InputStream targetFileIn = null;
        BufferedInputStream targetBufIn = null;
        Binary targetBinary = null;

        try {
            File sourceFile = tlSourceDataFile.get();
            targetFile = File.createTempFile("_magickproc_" + StringUtils.replace(nodeName, ":", "_"),
                    "." + FilenameUtils.getExtension(fileName));

            GraphicsMagickCommandUtils.resizeImage(sourceFile, targetFile,
                    ImageDimension.from(scalingParameters.getWidth(), scalingParameters.getHeight()));
            ImageDimension dimension = GraphicsMagickCommandUtils.identifyDimension(targetFile);

            targetFileIn = new FileInputStream(targetFile);
            targetBufIn = new BufferedInputStream(targetFileIn);
            targetBinary = ResourceHelper.getValueFactory(node).createBinary(targetBufIn);

            node.setProperty("jcr:data", targetBinary);
            node.setProperty(HippoGalleryNodeType.IMAGE_WIDTH, (long) dimension.getWidth());
            node.setProperty(HippoGalleryNodeType.IMAGE_HEIGHT, (long) dimension.getHeight());
        } catch (Exception e) {
            log.warn("Image resizing failed.", e);
        } finally {
            try {
                if (targetBinary != null) {
                    targetBinary.dispose();
                }
            } catch (Exception ignore) {
            }

            IOUtils.closeQuietly(targetBufIn);
            IOUtils.closeQuietly(targetFileIn);

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