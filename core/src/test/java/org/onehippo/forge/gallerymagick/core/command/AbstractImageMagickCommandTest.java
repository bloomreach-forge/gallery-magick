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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class AbstractImageMagickCommandTest extends AbstractMagickCommandTest {

    private static Logger log = LoggerFactory.getLogger(AbstractImageMagickCommandTest.class);

    private static boolean _convertCommandAvailable;

    static {
        final String executableFromSysProp = ImageMagickCommand.getExecutableFromSystemProperty("convert");

        if (executableFromSysProp != null) {
            _convertCommandAvailable = new File(executableFromSysProp).exists();
        } else {
            if (!_convertCommandAvailable && new File("/bin/convert").isFile()) {
                _convertCommandAvailable = true;
            }
            if (!_convertCommandAvailable && new File("/usr/bin/convert").isFile()) {
                _convertCommandAvailable = true;
            }
            if (!_convertCommandAvailable && new File("/usr/local/bin/convert").isFile()) {
                _convertCommandAvailable = true;
            }
            if (!_convertCommandAvailable && new File("/opt/local/bin/convert").isFile()) {
                _convertCommandAvailable = true;
            }
        }
    }

    protected static boolean isImageMagickAvailable() {
        if (_convertCommandAvailable) {
            return true;
        }

        log.warn("Image Magick convert command not found.");
        return false;
    }
}
