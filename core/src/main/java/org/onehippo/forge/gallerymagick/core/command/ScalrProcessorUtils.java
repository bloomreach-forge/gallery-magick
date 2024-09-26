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
package org.onehippo.forge.gallerymagick.core.command;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;
import org.onehippo.forge.gallerymagick.core.ImageDimension;

/**
 * Utility to identify or resize images using Scalr library.
 */
public class ScalrProcessorUtils {

    private ScalrProcessorUtils() {
    }

    /**
     * Detects the {@code sourceFile} and returns the size dimension from it.
     * @param sourceFile source image file
     * @return Detects the {@code sourceFile} and returns the size dimension from it
     * @throws IOException if IO exception occurs
     */
    public static ImageDimension identifyDimension(File sourceFile) throws IOException {
        ImageDimension dimension = null;
        String extension = FilenameUtils.getExtension(sourceFile.getName());
        Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(extension);

        if (!it.hasNext()) {
            throw new IllegalArgumentException("Unsupported file name extension: " + sourceFile);
        }

        ImageReader reader = null;

        try {
            reader = getImageReader(sourceFile);
            int width = reader.getWidth(reader.getMinIndex());
            int height = reader.getHeight(reader.getMinIndex());
            dimension = ImageDimension.from(width, height);
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }

        return dimension;
    }

    /**
     * Resize the given image {@code sourceFile} with resizing it to {@code width} and {@code height}
     * and store the resized image to {@code targetFile}.
     * @param sourceFile source image file
     * @param targetFile target image file
     * @param dimension image dimension
     * @throws IOException if IO exception occurs
     */
    public static void resizeImage(File sourceFile, File targetFile, ImageDimension dimension) throws IOException {
        resizeImage(sourceFile, targetFile, dimension, (String[]) null);
    }

    /**
     * Resize the given image {@code sourceFile} with resizing it to {@code width} and {@code height}
     * and store the resized image to {@code targetFile}, with appending {@code extraOptions} in the command line if provided.
     * @param sourceFile source image file
     * @param targetFile target image file
     * @param dimension image dimension
     * @param extraOptions extra command line options
     * @throws IOException if IO exception occurs
     */
    public static void resizeImage(File sourceFile, File targetFile, ImageDimension dimension, String... extraOptions)
            throws IOException {
        if (dimension == null) {
            throw new IllegalArgumentException("Invalid dimension: " + dimension);
        }

        ImageReader reader = null;
        ImageWriter writer = null;

        try {
            reader = getImageReader(sourceFile);

            if (reader == null) {
                throw new IllegalArgumentException("Unsupported image file name extension for reading: " + sourceFile);
            }

            writer = getImageWriter(targetFile);

            if (writer == null) {
                throw new IllegalArgumentException("Unsupported image file name extension for writing: " + targetFile);
            }

            BufferedImage sourceImage = reader.read(0);
            BufferedImage resizedImage;
            if (dimension.getHeight() == 0 && dimension.getWidth() == 0) {
                resizedImage = sourceImage;
            } else {
                Scalr.Mode mode = Scalr.Mode.AUTOMATIC;
                if (dimension.getWidth() == 0) {
                    mode = Scalr.Mode.FIT_TO_HEIGHT;
                } else if (dimension.getHeight() == 0) {
                    mode = Scalr.Mode.FIT_TO_WIDTH;
                }
                resizedImage = Scalr.resize(sourceImage, Scalr.Method.QUALITY, mode,
                        dimension.getWidth(), dimension.getHeight());
            }

            final ImageWriteParam writeParam = writer.getDefaultWriteParam();

            if (writeParam.canWriteCompressed()) {
                String[] compressionTypes = writeParam.getCompressionTypes();

                if (compressionTypes != null && compressionTypes.length > 0) {
                    writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    writeParam.setCompressionType(compressionTypes[0]);
                    writeParam.setCompressionQuality(1.0f);
                }
            }

            final IIOImage iioImage = new IIOImage(resizedImage, null, null);
            writer.write(null, iioImage, writeParam);
        } finally {
            if (reader != null) {
                reader.dispose();
            }
            if (writer != null) {
                writer.dispose();
            }
        }
    }

    /**
     * Creates and returns an {@link ImageReader} instance from the {@code sourceFile}.
     * @param sourceFile source file
     * @return an {@link ImageReader} instance
     * @throws IOException if IOException occurs
     */
    private static ImageReader getImageReader(File sourceFile) throws IOException {
        ImageReader reader = null;
        String extension = FilenameUtils.getExtension(sourceFile.getName());
        Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(extension);

        if (it.hasNext()) {
            reader = it.next();
            ImageInputStream input = new FileImageInputStream(sourceFile);
            reader.setInput(input);
        }

        return reader;
    }

    /**
     * Creates and returns an {@link ImageWriter} instance from the {@code targetFile}.
     * @param targetFile target file
     * @return an {@link ImageWriter} instance
     * @throws IOException if IOException occurs
     */
    private static ImageWriter getImageWriter(File targetFile) throws IOException {
        ImageWriter writer = null;
        String extension = FilenameUtils.getExtension(targetFile.getName());
        Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix(extension);

        if (it.hasNext()) {
            writer = it.next();
            ImageOutputStream output = new FileImageOutputStream(targetFile);
            writer.setOutput(output);
        }

        return writer;
    }
}
