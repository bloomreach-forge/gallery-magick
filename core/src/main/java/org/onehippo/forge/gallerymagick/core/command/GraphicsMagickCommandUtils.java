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
package org.onehippo.forge.gallerymagick.core.command;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

/**
 * Utility to run Graphics Magick Commands.
 */
public class GraphicsMagickCommandUtils {

    private GraphicsMagickCommandUtils() {
    }

    /**
     * Resize the given image {@code sourceFile} with resizing it to {@code width} and {@code height}
     * and store the resized image to {@code targetFile}.
     * @param sourceFile source image file
     * @param targetFile target image file
     * @param width resize width in pixels
     * @param height reisze height in pixels
     * @throws MagickExecuteException if execution exception occurs
     * @throws IOException if IO exception occurs
     */
    public static void resizeImage(File sourceFile, File targetFile, int width, int height)
            throws MagickExecuteException, IOException {
        resizeImage(sourceFile, targetFile, width, height, (String []) null);
    }

    /**
     * Resize the given image {@code sourceFile} with resizing it to {@code width} and {@code height}
     * and store the resized image to {@code targetFile}, with appending {@code extraOptions} in the command line if provided.
     * @param sourceFile source image file
     * @param targetFile target image file
     * @param width resize width in pixels
     * @param height reisze height in pixels
     * @param extraOptions extra command line options
     * @throws MagickExecuteException if execution exception occurs
     * @throws IOException if IO exception occurs
     */
    public static void resizeImage(File sourceFile, File targetFile, int width, int height, String ... extraOptions)
            throws MagickExecuteException, IOException {
        GraphicsMagickCommand cmd = new GraphicsMagickCommand(null, "convert");

        final File tempFolder = getTempFolder();

        if (tempFolder != null) {
            cmd.setWorkingDirectory(tempFolder);
        }

        cmd.addArgument(sourceFile.getCanonicalPath());
        cmd.addArgument("-resize");
        cmd.addArgument("" + width + "x" + height);

        if (extraOptions != null) {
            for (String extraOption : extraOptions) {
                cmd.addArgument(extraOption);
            }
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
