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

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang.StringUtils;

/**
 * Encapsulation of <a href="http://www.imagemagick.org/">Image Magick Command Utilities</a>.
 */
public class ImageMagickCommand extends AbstractMagickCommand {

    /**
     * System property name for Image Magick command executable.
     */
    public static final String PROP_EXECUTABLE_CONVERT = "org.onehippo.forge.gallerymagick.core.command.im.convert";

    /**
     * Constructor with an {@code executable} and a {@code subCommand}.
     * If {@code executable} is null, it tries to find it from a system property keyed by {@link #PROP_EXECUTABLE_CONVERT}.
     * If not found from the system property, it uses the default Image Magick convert command executable, {@link #DEFAULT_SUBCOMMAND_CONVERT}.
     * @param executable executable of Graphics Magick command
     * @param subCommand sub-command of <code>gm</code> command
     */
    public ImageMagickCommand(final String executable, final String subCommand) {
        super(StringUtils.defaultIfBlank(getExecutableFromSystemProperty(subCommand), executable),
                StringUtils.defaultIfBlank(subCommand, DEFAULT_SUBCOMMAND_CONVERT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CommandLine createCommandLine() {
        String executable = getExecutable();

        if (StringUtils.isBlank(executable)) {
            executable = getSubCommand();
        }

        CommandLine cmdLine = new CommandLine(executable);

        for (String argument : getArguments()) {
            cmdLine.addArgument(argument);
        }

        return cmdLine;
    }

    /**
     * Finds the Image Magick command executable by finding it from the system property, {@link #PROP_EXECUTABLE_CONVERT},
     * and returns it if found.
     * @return Image Magick command executable
     */
    static String getExecutableFromSystemProperty(final String subCommand) {
        String executable = null;

        if (StringUtils.equals("convert", subCommand)) {
            final String value = System.getProperty(PROP_EXECUTABLE_CONVERT);
            if (StringUtils.isNotBlank(value)) {
                executable = value;
            }
        }

        return executable;
    }

}
