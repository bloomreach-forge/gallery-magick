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

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphicsMagickCommandTest extends AbstractGraphicsMagickCommandTest {

    private static Logger log = LoggerFactory.getLogger(GraphicsMagickCommandTest.class);

    @Before
    public void before() throws Exception {
        Assume.assumeTrue(isGraphicsMagickAvailable());
    }

    @Test
    public void testGraphicsMagickConvert() throws Exception {
        String sourceFileName;
        String sourceExtension;
        String targetFileName;

        for (File sourceFile : getTestImageFiles()) {
            sourceFileName = sourceFile.getName();
            sourceExtension = FilenameUtils.getExtension(sourceFileName);

            if (StringUtils.equals("tiff", sourceExtension)) {
                log.warn("GraphicsMagick requires tiff-v3.5.4.tar.gz or later to support tiff files.");
                continue;
            }

            targetFileName = FilenameUtils.getBaseName(sourceFileName) + "-thumbnail." + sourceExtension;

            long sourceLength = sourceFile.length();
            File targetFile = new File("target/" + targetFileName);

            GraphicsMagickCommand cmd = new GraphicsMagickCommand(null, "convert");
            cmd.setWorkingDirectory(new File("target"));
            cmd.addArgument(sourceFile.getCanonicalPath());
            cmd.addArgument("-resize");
            cmd.addArgument("120x120");
            cmd.addArgument("+profile");
            cmd.addArgument("*");
            cmd.addArgument(targetFile.getCanonicalPath());
            cmd.execute();

            assertTrue(targetFile.isFile());
            assertTrue(targetFile.length() > 0L);
            assertTrue(targetFile.length() < sourceLength);
        }
    }

}
