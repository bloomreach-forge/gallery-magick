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

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GraphicsMagickCommandTest extends AbstractGraphicsMagickCommandTest {

    @Before
    public void before() throws Exception {
        Assume.assumeTrue(isGraphicsMagickAvailable());
    }

    @Test
    public void testGraphicsMagickConvert() throws Exception {
        String sourceFileName;
        String sourceExtension;
        String targetFileName;
        long sourceLength;
        File targetFile;

        for (File sourceFile : getTestImageFiles()) {
            sourceFileName = sourceFile.getName();
            sourceExtension = FilenameUtils.getExtension(sourceFileName);
            targetFileName = FilenameUtils.getBaseName(sourceFileName) + "-thumbnail." + sourceExtension;

            sourceLength = sourceFile.length();
            targetFile = new File("target/testGraphicsMagickConvert-120x120-" + targetFileName);

            GraphicsMagickCommand cmd = new GraphicsMagickCommand(null, "convert");
            cmd.setWorkingDirectory(new File("target"));
            cmd.addArgument(sourceFile.getCanonicalPath());
            cmd.addArgument("-size");
            cmd.addArgument("120x120");
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
