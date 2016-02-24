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
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class ImageMagickCommandUtilsTest extends AbstractImageMagickCommandTest {

    @Before
    public void before() throws Exception {
        Assume.assumeTrue(isImageMagickAvailable());
    }

    @Test
    public void testImageMagickResizeImage() throws Exception {
        String sourceFileName;
        String sourceExtension;
        String targetFileName;

        for (File sourceFile : getTestImageFiles()) {
            sourceFileName = sourceFile.getName();
            sourceExtension = FilenameUtils.getExtension(sourceFileName);
            targetFileName = FilenameUtils.getBaseName(sourceFileName) + "-thumbnail." + sourceExtension;

            long sourceLength = sourceFile.length();
            File targetFile = new File("target/" + targetFileName);

            ImageMagickCommandUtils.resizeImage(sourceFile, targetFile, 120, 120, "+profile", "*");

            assertTrue(targetFile.isFile());
            assertTrue(targetFile.length() > 0L);
            assertTrue(targetFile.length() < sourceLength);
        }
    }

}
