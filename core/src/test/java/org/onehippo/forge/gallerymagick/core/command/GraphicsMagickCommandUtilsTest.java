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
import org.onehippo.forge.gallerymagick.core.ImageDimension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GraphicsMagickCommandUtilsTest extends AbstractGraphicsMagickCommandTest {

    private static Logger log = LoggerFactory.getLogger(GraphicsMagickCommandUtilsTest.class);
    private static final Path targetDirectory = Paths.get( "target");

    @Before
    public void before() throws Exception {
        Assume.assumeTrue(isGraphicsMagickAvailable());
    }

    @Test
    public void testGraphicsMagickIdentifyDimension() throws Exception {
        ImageDimension dimension;

        for (Path sourceFile : getTestImageFiles()) {
            dimension = GraphicsMagickCommandUtils.identifyDimension(sourceFile);
            log.debug("Dimension of {} : {}", sourceFile, dimension);
            assertTrue(dimension.getWidth() > 0);
            assertTrue(dimension.getHeight() > 0);
        }
    }

    @Test
    public void testGraphicsMagickResizeImage() throws Exception {
        String sourceFileName;
        String sourceExtension;
        String targetFileName;
        ImageDimension dimension;
        long sourceLength;
        Path targetFile;
        long targetLength;

        for (Path sourceFile : getTestImageFiles()) {
            sourceFileName = sourceFile.getFileName().toString();
            sourceExtension = FilenameUtils.getExtension(sourceFileName);
            targetFileName = FilenameUtils.getBaseName(sourceFileName) + "-thumbnail." + sourceExtension;
            sourceLength = Files.size(sourceFile);

            targetFile = Files.createTempFile(targetDirectory,  "testGraphicsMagickResizeImage-120x120",  targetFileName);
            GraphicsMagickCommandUtils.resizeImage(sourceFile, targetFile, ImageDimension.from("120x120"), "+profile", "*");
            targetLength = Files.size(targetFile);

            assertTrue(Files.exists(targetFile));
            assertTrue(targetLength > 0L);
            assertTrue(targetLength < sourceLength);

            targetFile = Files.createTempFile(targetDirectory,  "testGraphicsMagickResizeImage-120x0",  targetFileName);
            GraphicsMagickCommandUtils.resizeImage(sourceFile, targetFile, ImageDimension.from("120x0"), "+profile", "*");
            dimension = GraphicsMagickCommandUtils.identifyDimension(targetFile);
            targetLength = Files.size(targetFile);

            assertTrue(Files.exists(targetFile));
            assertTrue(targetLength > 0L);
            assertTrue(targetLength < sourceLength);
            assertEquals(120, dimension.getWidth());

            targetFile = Files.createTempFile(targetDirectory,  "testGraphicsMagickResizeImage-0x120",  targetFileName);
            GraphicsMagickCommandUtils.resizeImage(sourceFile, targetFile, ImageDimension.from("0x120"), "+profile", "*");
            dimension = GraphicsMagickCommandUtils.identifyDimension(targetFile);
            targetLength = Files.size(targetFile);

            assertTrue(Files.exists(targetFile));
            assertTrue(targetLength > 0L);
            assertTrue(targetLength < sourceLength);
            assertEquals(120, dimension.getHeight());

            targetFile = Files.createTempFile(targetDirectory,  "testGraphicsMagickResizeImage-0x0",  targetFileName);
            GraphicsMagickCommandUtils.resizeImage(sourceFile, targetFile, ImageDimension.from("0x0"), "+profile", "*");
            dimension = GraphicsMagickCommandUtils.identifyDimension(targetFile);
            ImageDimension sourceDimension = GraphicsMagickCommandUtils.identifyDimension(sourceFile);
            targetLength = Files.size(targetFile);

            assertTrue(Files.exists(targetFile));
            assertTrue(targetLength > 0L);
            assertEquals(sourceDimension, dimension);
        }
    }

}
