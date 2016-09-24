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
import org.junit.Test;
import org.onehippo.forge.gallerymagick.core.ImageDimension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScalrProcessorUtilsTest extends AbstractMagickCommandTest {

    private static Logger log = LoggerFactory.getLogger(ScalrProcessorUtilsTest.class);

    protected static final String [] SCALR_PROCESSOR_TEST_IMAGE_FILE_EXTENSIONS = { "jpg", "png", "gif", "bmp" };

    @Test
    public void testHippoGalleryProcessorIdentifyDimension() throws Exception {
        ImageDimension dimension;

        for (File sourceFile : getTestImageFiles()) {
            dimension = ScalrProcessorUtils.identifyDimension(sourceFile);
            log.debug("Dimension of {} : {}", sourceFile, dimension);
            assertTrue(dimension.getWidth() > 0);
            assertTrue(dimension.getHeight() > 0);
        }
    }

    @Test
    public void testHippoGalleryProcessorResizeImage() throws Exception {
        String sourceFileName;
        String sourceExtension;
        String targetFileName;

        for (File sourceFile : getTestImageFiles()) {
            sourceFileName = sourceFile.getName();
            sourceExtension = FilenameUtils.getExtension(sourceFileName);
            targetFileName = FilenameUtils.getBaseName(sourceFileName) + "-thumbnail." + sourceExtension;

            long sourceLength = sourceFile.length();
            File targetFile = new File("target/testScalrProcessorResizeImage-" + targetFileName);

            ScalrProcessorUtils.resizeImage(sourceFile, targetFile, ImageDimension.from("120x120"));

            assertTrue(targetFile.isFile());
            assertTrue(targetFile.length() > 0L);
            assertTrue(targetFile.length() < sourceLength);
        }
    }

    @Override
    protected String [] getTestImageFileExtensions() {
        return SCALR_PROCESSOR_TEST_IMAGE_FILE_EXTENSIONS;
    }

}
