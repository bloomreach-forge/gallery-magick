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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * AbstractMagickCommandTest.
 */
abstract public class AbstractMagickCommandTest {

    protected static final String [] DEFAULT_TEST_IMAGE_FILE_EXTENSIONS = { "jpg", "png", "gif", "bmp", "tiff" };

    private List<File> testImageFiles;

    protected String [] getTestImageFileExtensions() {
        return DEFAULT_TEST_IMAGE_FILE_EXTENSIONS;
    }

    protected List<File> getTestImageFiles() throws URISyntaxException {
        if (testImageFiles == null) {
            testImageFiles = new ArrayList<>();

            for (String extension : getTestImageFileExtensions()) {
                testImageFiles.add(new File(AbstractMagickCommandTest.class.getResource("/hippo." + extension).toURI()));
            }
        }

        return testImageFiles;
    }

}
