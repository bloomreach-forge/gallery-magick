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
package org.onehippo.forge.gallerymagick.core.gm.command;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

public class GraphicsMagickCommandUtils {

    private static final String GM_EXECUTABLE = "org.onehippo.forge.gallerymagick.core.gm.executable";

    private GraphicsMagickCommandUtils() {
    }

    public static void resizeImage(File sourceFile, File targetFile, int width, int height)
            throws GraphicsMagickExecuteException, IOException {
        resizeImage(sourceFile, targetFile, width, height, (String []) null);
    }

    public static void resizeImage(File sourceFile, File targetFile, int width, int height, String ... extraOptions)
            throws GraphicsMagickExecuteException, IOException {
        GraphicsMagickCommand cmd = new GraphicsMagickCommand("convert");

        final String executable = getExecutableFromSystemProperty();

        if (executable != null) {
            cmd.setExecutable(executable);
        }

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

    public static String getExecutableFromSystemProperty() {
        String executable = null;
        final String value = System.getProperty(GM_EXECUTABLE);

        if (StringUtils.isNotBlank(value)) {
            executable = value;
        }

        return executable;
    }

    private static File getTempFolder() {
        File tempFolder = null;
        final String tmpDirPath = System.getProperty("java.io.tmpdir");

        if (StringUtils.isNotBlank(tmpDirPath)) {
            tempFolder = new File(tmpDirPath);
        }

        return tempFolder;
    }
}
