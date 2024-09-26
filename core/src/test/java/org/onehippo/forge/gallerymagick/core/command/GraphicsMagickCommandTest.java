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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GraphicsMagickCommandTest extends AbstractGraphicsMagickCommandTest {
    private static final Path targetDirectory = Paths.get( "target");

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
        Path targetFile;
        long targetLength;

        for (Path sourceFile : getTestImageFiles()) {
            sourceFileName = sourceFile.getFileName().toString();
            sourceExtension = FilenameUtils.getExtension(sourceFileName);
            targetFileName = FilenameUtils.getBaseName(sourceFileName) + "-thumbnail." + sourceExtension;

            sourceLength = Files.size(sourceFile);
            targetFile = Files.createTempFile(targetDirectory,  "testGraphicsMagickResizeImage-120x120-",  targetFileName);

            GraphicsMagickCommand cmd = new GraphicsMagickCommand(null, "convert");
            cmd.setWorkingDirectory(Paths.get("target"));
            cmd.addArgument(sourceFile.toRealPath().toString());
            cmd.addArgument("-size");
            cmd.addArgument("120x120");
            cmd.addArgument("-resize");
            cmd.addArgument("120x120");
            cmd.addArgument("+profile");
            cmd.addArgument("*");
            cmd.addArgument(targetFile.toRealPath().toString());
            cmd.execute();
            targetLength = Files.size(targetFile);

            assertTrue(Files.exists(targetFile));
            assertTrue(targetLength > 0L);
            assertTrue(targetLength < sourceLength);
        }
    }

}
