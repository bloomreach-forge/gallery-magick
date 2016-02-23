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
package org.onehippo.forge.gallerymagick.core.gm.command;

import java.io.File;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphicsMagickCommandTest {

    private static Logger log = LoggerFactory.getLogger(GraphicsMagickCommandTest.class);

    private static boolean _gmCommandAvailable;

    private URL HIPPO_79_JPG = GraphicsMagickCommandTest.class.getResource("hippo-79.jpg");

    @BeforeClass
    public static void beforeClass() throws Exception {
        if (new File("/bin/gm").isFile()) {
            _gmCommandAvailable = true;
            return;
        }

        if (new File("/usr/bin/gm").isFile()) {
            _gmCommandAvailable = true;
            return;
        }

        if (new File("/usr/local/bin/gm").isFile()) {
            _gmCommandAvailable = true;
            return;
        }
    }

    @Test
    public void testGraphicsMagickConvert() throws Exception {
        if (!isGraphicsMagickAvailable()) {
            return;
        }

        GraphicsMagickCommand cmd = new GraphicsMagickCommand("convert");
        cmd.setWorkingDirectory(new File("target"));
        File hippo79File = new File(HIPPO_79_JPG.toURI());
        cmd.addArgument(hippo79File.getCanonicalPath());
        cmd.addArgument("-resize");
        cmd.addArgument("120x120");
        cmd.addArgument("+profile");
        cmd.addArgument("*");
        cmd.addArgument("thumbnail.jpg");

        try {
            cmd.execute();
        } catch (Exception e) {
            log.error("Execution failure.", e);
        }
    }

    private static boolean isGraphicsMagickAvailable() {
        if (_gmCommandAvailable) {
            return true;
        }

        log.warn("Graphics Magick command not found.");
        return false;
    }
}
