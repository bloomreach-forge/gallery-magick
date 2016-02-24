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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * AbstractMagickCommandTest.
 */
abstract public class AbstractMagickCommandTest {

    private List<File> testImageFiles;

    protected List<File> getTestImageFiles() throws URISyntaxException {
        if (testImageFiles == null) {
            testImageFiles = new ArrayList<>();
            testImageFiles.add(new File(AbstractMagickCommandTest.class.getResource("/hippo.jpg").toURI()));
            testImageFiles.add(new File(AbstractMagickCommandTest.class.getResource("/hippo.png").toURI()));
            testImageFiles.add(new File(AbstractMagickCommandTest.class.getResource("/hippo.gif").toURI()));
            testImageFiles.add(new File(AbstractMagickCommandTest.class.getResource("/hippo.bmp").toURI()));
            testImageFiles.add(new File(AbstractMagickCommandTest.class.getResource("/hippo.tiff").toURI()));
        }

        return testImageFiles;
    }

    protected void setTestImageFiles(List<File> testImageFiles) {
        this.testImageFiles = testImageFiles;
    }

}
