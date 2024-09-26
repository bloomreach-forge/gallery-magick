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

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang.StringUtils;

/**
 * Encapsulation of <a href="http://www.graphicsmagick.org/">Graphics Magick Command Utilities</a>.
 */
public class GraphicsMagickCommand extends AbstractMagickCommand {

    /**
     * System property name for Graphics Magick command executable.
     * The default value is {@link #DEFAULT_EXECUTABLE}.
     */
    public static final String PROP_EXECUTABLE = "org.onehippo.forge.gallerymagick.core.command.gm";

    /**
     * Default Graphics Magick command executable.
     */
    public static final String DEFAULT_EXECUTABLE = "gm";

    /**
     * Constructor with an {@code executable} and a {@code subCommand}.
     * If {@code executable} is null, it tries to find it from a system property keyed by {@link #PROP_EXECUTABLE}.
     * If not found from the system property, it uses the default Graphics Magick executable, {@link #DEFAULT_EXECUTABLE}.
     * @param executable executable of Graphics Magick command
     * @param subCommand sub-command of <code>gm</code> command
     */
    public GraphicsMagickCommand(final String executable, final String subCommand) {
        super(StringUtils.defaultIfBlank(getExecutableFromSystemProperty(), DEFAULT_EXECUTABLE),
                StringUtils.defaultIfBlank(subCommand, DEFAULT_SUBCOMMAND_CONVERT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CommandLine createCommandLine() {
        CommandLine cmdLine = new CommandLine(getExecutable());

        cmdLine.addArgument(getSubCommand());

        for (String argument : getArguments()) {
            cmdLine.addArgument(argument);
        }

        return cmdLine;
    }

    /**
     * Finds the Graphics Magick command executable by finding it from the system property, {@link #PROP_EXECUTABLE},
     * and returns it if found.
     * @return Graphics Magick command executable
     */
    static String getExecutableFromSystemProperty() {
        String executable = null;
        final String value = System.getProperty(PROP_EXECUTABLE);

        if (StringUtils.isNotBlank(value)) {
            executable = value;
        }

        return executable;
    }

}
