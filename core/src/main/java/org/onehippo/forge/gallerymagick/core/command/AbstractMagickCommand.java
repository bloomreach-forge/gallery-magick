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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract *Magick Command.
 */
abstract public class AbstractMagickCommand {

    private static final Logger log = LoggerFactory.getLogger(AbstractMagickCommand.class);

    /**
     * Default Graphics Magick command executable.
     */
    public static final String DEFAULT_SUBCOMMAND_CONVERT = "convert";

    /**
     * System property name for Graphics Magick command executable.
     */
    public static final String PROP_TIMEOUT = "org.onehippo.forge.gallerymagick.core.command.timeout";

    /**
     * The default command execution timeout.
     */
    private static final long DEFAULT_COMMAND_TIMEOUT = 3000L;

    /**
     * Working directory of a command execution.
     */
    private File workingDirectory;

    /**
     * Command executable. e.g, <code>gm</code>, <code>/usr/bin/gm</code> or <code>/usr/local/bin/gm</code>.
     */
    private final String executable;

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
     * @param executable executable of Magick command
     * @param subCommand sub-command
     */
    public AbstractMagickCommand(final String executable, final String subCommand) {
        this.executable = executable;
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
     * Returns the executable of Magick command.
     * @return the executable of Magick command
     */
    public String getExecutable() {
        return executable;
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
     * Add a command line argument to Magick command.
     * @param argument a command line argument to Magick command
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
     * Remove all the Magick command line arguments.
     */
    public void clearArguments() {
        if (arguments != null) {
            arguments.clear();
        }
    }

    /**
     * Execute the Magick command with the sub-command and arguments.
     * @throws MagickExecuteException if an execution exception occurs
     * @throws IOException if IO exception occurs
     */
    public void execute() throws IOException {
        execute(null);
    }

    /**
     * Execute the Magick command with the sub-command and arguments.
     * @param stdOut standard output stream
     * @throws MagickExecuteException if an execution exception occurs
     * @throws IOException if IO exception occurs
     */
    public void execute(final OutputStream stdOut) throws IOException {
        CommandLine cmdLine = createCommandLine();
        ByteArrayOutputStream errStream = null;
        int exitValue = 0;
        DefaultExecuteResultHandler resultHandler = null;

        try {
            errStream = new ByteArrayOutputStream(512);

            final DefaultExecutor executor = new DefaultExecutor();
            ExecuteStreamHandler streamHandler;

            if (stdOut != null) {
                streamHandler = new PumpStreamHandler(stdOut, errStream);
            } else {
                streamHandler = new PumpStreamHandler(System.out, errStream);
            }

            executor.setStreamHandler(streamHandler);

            if (getWorkingDirectory() != null) {
                executor.setWorkingDirectory(getWorkingDirectory());
            }

            long timeout = NumberUtils.toLong(System.getProperty(PROP_TIMEOUT), DEFAULT_COMMAND_TIMEOUT);

            if (timeout > 0) {
                ExecuteWatchdog watchdog = new ExecuteWatchdog(DEFAULT_COMMAND_TIMEOUT);
                executor.setWatchdog(watchdog);
                resultHandler = new DefaultExecuteResultHandler();
                executor.execute(cmdLine, resultHandler);
                log.debug("Executed with watchdog: {}", cmdLine);
                resultHandler.waitFor();
            } else {
                exitValue = executor.execute(cmdLine);
                log.debug("Executed without watchdog: {}", cmdLine);
            }
        } catch (ExecuteException | InterruptedException e) {
            if (resultHandler != null) {
                exitValue = resultHandler.getExitValue();
            }
            if (e.getCause() == null) {
                throw new MagickExecuteException(getExecutionErrorMessage(cmdLine, errStream, e), exitValue);
            } else {
                throw new MagickExecuteException(getExecutionErrorMessage(cmdLine, errStream, e), exitValue, e.getCause());
            }
        } finally {
            IOUtils.closeQuietly(errStream);
        }
    }

    private String getExecutionErrorMessage(final CommandLine cmdLine, final ByteArrayOutputStream errStream,
            final Exception e) throws UnsupportedEncodingException {
        StringBuilder sbMsg = new StringBuilder(256);
        sbMsg.append(StringUtils.trim(errStream.toString("UTF-8")));
        sbMsg.append(' ').append(cmdLine.toString());
        sbMsg.append(". ").append(e.getMessage());
        return sbMsg.toString();
    }

    /**
     * Create a {@link CommandLine} from executable and arguments.
     * @return a {@link CommandLine} from executable and arguments
     */
    abstract protected CommandLine createCommandLine();

}
