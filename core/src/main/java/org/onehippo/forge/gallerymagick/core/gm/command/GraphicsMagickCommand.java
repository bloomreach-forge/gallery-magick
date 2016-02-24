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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Encapsulation of <a href="http://www.graphicsmagick.org/utilities.html">Graphics Magick Command Utilities</a>.
 */
public class GraphicsMagickCommand {

    /**
     * Default Graphics Magick command executable.
     */
    public static final String DEFAULT_EXECUTABLE = "gm";

    /**
     * Working directory of a command execution.
     */
    private File workingDirectory;

    /**
     * Command executable. e.g, <code>gm</code>, <code>/usr/bin/gm</code> or <code>/usr/local/bin/gm</code>.
     */
    private String executable = DEFAULT_EXECUTABLE;

    /**
     * Sub-command of <code>gm</code> command. e.g, <code>convert</code>.
     */
    private final String subCommand;

    /**
     * Command line arguments after the sub-command.
     */
    private List<String> arguments;

    /**
     * Constructor with a sub-command.
     * @param subCommand sub-command of <code>gm</code> command
     */
    public GraphicsMagickCommand(final String subCommand) {
        this.subCommand = subCommand;
    }

    /**
     * Returns working directory.
     * @return working directory
     */
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Sets working directory
     * @param workingDirectory working directory
     */
    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Returns the executable of Graphics Magick command.
     * @return the executable of Graphics Magick command
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * Sets the executable of Graphics Magick command.
     * @param executable the executable of Graphics Magick command
     */
    public void setExecutable(String executable) {
        this.executable = executable;
    }

    /**
     * Returns the sub-command of Graphics Magick command.
     * @return the sub-command of Graphics Magick command
     */
    public String getSubCommand() {
        return subCommand;
    }

    /**
     * Returns the command line arguments after the sub-command.
     * @return the command line arguments after the sub-command
     */
    public List<String> getArguments() {
        if (arguments == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(arguments);
    }

    /**
     * Add a command line argument to Graphics Magick command.
     * @param argument a command line argument to Graphics Magick command
     */
    public void addArgument(final String argument) {
        if (StringUtils.isBlank(argument)) {
            throw new IllegalArgumentException("Blank argument.");
        }

        if (arguments == null) {
            arguments = new ArrayList<>();
        }

        arguments.add(argument);
    }

    /**
     * Remove all the Graphics Magick command line arguments.
     */
    public void clearArguments() {
        if (arguments != null) {
            arguments.clear();
        }
    }

    /**
     * Execute the Graphics Magick command with the sub-command and arguments.
     * @throws GraphicsMagickExecuteException if an execution exception occurs
     * @throws IOException if IO exception occurs
     */
    public void execute() throws GraphicsMagickExecuteException, IOException {
        CommandLine cmdLine = new CommandLine(getExecutable());
        cmdLine.addArgument(getSubCommand());

        for (String argument : getArguments()) {
            cmdLine.addArgument(argument);
        }

        ByteArrayOutputStream errStream = null;

        try {
            errStream = new ByteArrayOutputStream(512);

            final DefaultExecutor executor = new DefaultExecutor();
            executor.setStreamHandler(new PumpStreamHandler(System.out, errStream));

            if (getWorkingDirectory() != null) {
                executor.setWorkingDirectory(getWorkingDirectory());
            }

            int exitCode = executor.execute(cmdLine);
        } catch (ExecuteException e) {
            StringBuilder sbMsg = new StringBuilder(256);
            sbMsg.append(StringUtils.trim(errStream.toString("UTF-8")));
            sbMsg.append(' ').append(cmdLine.toString());
            sbMsg.append(". ").append(e.getMessage());

            if (e.getCause() == null) {
                throw new GraphicsMagickExecuteException(sbMsg.toString(), e.getExitValue());
            } else {
                throw new GraphicsMagickExecuteException(sbMsg.toString(), e.getExitValue(), e.getCause());
            }
        } finally {
            IOUtils.closeQuietly(errStream);
        }
    }
}
