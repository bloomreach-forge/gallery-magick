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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.onehippo.forge.gallerymagick.core.ImageDimension;

/**
 * Utility to run Graphics Magick Commands.
 */
public class GraphicsMagickCommandUtils {

    private GraphicsMagickCommandUtils() {
    }

    /**
     * Execute <code>identify -verbose</code> sub-command and return a string of all extracted metadata from the output.
     * @param sourceFile source image file
     * @return Execute <code>identify -verbose</code> sub-command and return a string of all extracted metadata from the output
     * @throws MagickExecuteException if execution exception occurs
     * @throws IOException if IO exception occurs
     */
    public static String identifyAllMetadata(File sourceFile) throws MagickExecuteException, IOException {
        GraphicsMagickCommand cmd = new GraphicsMagickCommand(null, "identify");

        final File tempFolder = getTempFolder();

        if (tempFolder != null) {
            cmd.setWorkingDirectory(tempFolder);
        }

        cmd.addArgument("-verbose");
        cmd.addArgument(sourceFile.getCanonicalPath());

        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
        cmd.execute(baos);
        return StringUtils.trim(baos.toString("UTF-8"));
    }

    /**
     * Execute <code>identify</code> sub-command and return an {@link ImageDimension} instance from the output.
     * @param sourceFile source image file
     * @return Execute <code>identify</code> sub-command and return an {@link ImageDimension} instance from the output
     * @throws MagickExecuteException if execution exception occurs
     * @throws IOException if IO exception occurs
     */
    public static ImageDimension identifyDimension(File sourceFile) throws MagickExecuteException, IOException {
        GraphicsMagickCommand cmd = new GraphicsMagickCommand(null, "identify");

        final File tempFolder = getTempFolder();

        if (tempFolder != null) {
            cmd.setWorkingDirectory(tempFolder);
        }

        cmd.addArgument("-format");
        cmd.addArgument("%wx%h");
        cmd.addArgument(sourceFile.getCanonicalPath());

        ByteArrayOutputStream baos = new ByteArrayOutputStream(40);
        cmd.execute(baos);
        String output = StringUtils.trim(baos.toString("UTF-8"));

        return ImageDimension.from(output);
    }

    /**
     * Resize the given image {@code sourceFile} with resizing it to {@code width} and {@code height}
     * and store the resized image to {@code targetFile}.
     * @param sourceFile source image file
     * @param targetFile target image file
     * @param dimension image dimension
     * @throws MagickExecuteException if execution exception occurs
     * @throws IOException if IO exception occurs
     */
    public static void resizeImage(File sourceFile, File targetFile, ImageDimension dimension)
            throws MagickExecuteException, IOException {
        resizeImage(sourceFile, targetFile, dimension, (String []) null);
    }

    /**
     * Resize the given image {@code sourceFile} with resizing it to {@code width} and {@code height}
     * and store the resized image to {@code targetFile}, with appending {@code extraOptions} in the command line if provided.
     * @param sourceFile source image file
     * @param targetFile target image file
     * @param dimension image dimension
     * @param extraOptions extra command line options
     * @throws MagickExecuteException if execution exception occurs
     * @throws IOException if IO exception occurs
     */
    public static void resizeImage(File sourceFile, File targetFile, ImageDimension dimension, String ... extraOptions)
            throws MagickExecuteException, IOException {
        if (dimension == null) {
            throw new IllegalArgumentException("Invalid dimension: " + dimension);
        }

        GraphicsMagickCommand cmd = new GraphicsMagickCommand(null, "convert");

        final File tempFolder = getTempFolder();

        if (tempFolder != null) {
            cmd.setWorkingDirectory(tempFolder);
        }

        cmd.addArgument(sourceFile.getCanonicalPath());
        final String dimensionArg = dimension.toCommandArgument();
        cmd.addArgument("-resize");
        cmd.addArgument(dimensionArg);

        final List<String> extraOptionList = (extraOptions != null) ? Arrays.asList(extraOptions) : Collections.emptyList();

        for (String extraOption : extraOptionList) {
            cmd.addArgument(extraOption);
        }

        if (dimension.getWidth() > 0 || dimension.getHeight() > 0) {
            if (!extraOptionList.contains("-size")) {
                cmd.addArgument("-size");
                cmd.addArgument(dimensionArg);
            }
        }

        if (!extraOptionList.contains("+profile") && !extraOptionList.contains("-profile")) {
            cmd.addArgument("+profile");
            cmd.addArgument("*");
        }

        cmd.addArgument(targetFile.getCanonicalPath());

        cmd.execute();
    }

    /**
     * Returns the temporary folder file.
     * @return the temporary foler file
     */
    private static File getTempFolder() {
        File tempFolder = null;
        final String tmpDirPath = System.getProperty("java.io.tmpdir");

        if (StringUtils.isNotBlank(tmpDirPath)) {
            tempFolder = new File(tmpDirPath);
        }

        return tempFolder;
    }
}
