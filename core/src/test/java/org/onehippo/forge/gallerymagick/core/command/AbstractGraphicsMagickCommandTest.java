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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class AbstractGraphicsMagickCommandTest extends AbstractMagickCommandTest {

    private static Logger log = LoggerFactory.getLogger(AbstractGraphicsMagickCommandTest.class);

    private static boolean _gmCommandAvailable;

    static {
        final String executableFromSysProp = GraphicsMagickCommand.getExecutableFromSystemProperty();

        if (executableFromSysProp != null) {
            _gmCommandAvailable = new File(executableFromSysProp).exists();
        } else {
            if (!_gmCommandAvailable && new File("/bin/gm").isFile()) {
                _gmCommandAvailable = true;
            }
            if (!_gmCommandAvailable && new File("/usr/bin/gm").isFile()) {
                _gmCommandAvailable = true;
            }
            if (!_gmCommandAvailable && new File("/usr/local/bin/gm").isFile()) {
                _gmCommandAvailable = true;
            }
            if (!_gmCommandAvailable && new File("/opt/local/bin/gm").isFile()) {
                _gmCommandAvailable = true;
            }
        }
    }

    protected static boolean isGraphicsMagickAvailable() {
        if (_gmCommandAvailable) {
            return true;
        }

        log.warn("Graphics Magick command not found.");
        return false;
    }
}
