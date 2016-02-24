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

import org.apache.commons.exec.ExecuteException;

/**
 * An exception indicating that the executing a Graphics Magick processe failed.
 */
public class GraphicsMagickExecuteException extends ExecuteException {

    private static final long serialVersionUID = 1L;

    public GraphicsMagickExecuteException(final String message, final int exitValue) {
        super(message, exitValue);
    }

    public GraphicsMagickExecuteException(final String message, final int exitValue, final Throwable cause) {
        super(message, exitValue, cause);
    }

}
