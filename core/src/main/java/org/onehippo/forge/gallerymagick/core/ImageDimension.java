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
package org.onehippo.forge.gallerymagick.core;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.math.NumberUtils;

/**
 * Image Dimension.
 */
public class ImageDimension implements Serializable {

    private static final long serialVersionUID = 1L;

    private int width;
    private int height;

    public ImageDimension() {
    }

    public ImageDimension(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        if (width < 0) {
            throw new IllegalArgumentException("Invalid width: " + width);
        }

        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        if (height < 0) {
            throw new IllegalArgumentException("Invalid height: " + height);
        }

        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ImageDimension)) {
            return false;
        }

        ImageDimension other = (ImageDimension) o;

        return width == other.width && height == other.height;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(width).append(height).toHashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder(20).append(width).append('x').append(height).toString();
    }

    /**
     * @return String representation for ImageMagick/GraphicsMagick command line usage.
     * A width or height of 0 means "unbounded", and results in a bounding box that does not restrict scaling in either
     * the width or height, respectively.
     * When both width and height are 0 or less, the image is not scaled at all but merely copied.
     */
    public String toCommandArgument() {
        StringBuilder arg = new StringBuilder(20);
        if (width == 0 && height == 0) { //no resize
            arg.append("100%");
        }
        if (width > 0) {
            arg.append(width);
        }
        if (height > 0) {
            arg.append('x').append(height);
        }

        return arg.toString();
    }

    public static ImageDimension from(final int width, final int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Invalid width or height.");
        }

        return new ImageDimension(width, height);
    }

    public static ImageDimension from(final String dimension) {
        int width = -1;
        int height = -1;

        if (StringUtils.isNotBlank(dimension)) {
            int offset = dimension.indexOf('x');

            if (offset != -1) {
                width = NumberUtils.toInt(dimension.substring(0, offset));
                height = NumberUtils.toInt(dimension.substring(offset + 1));
            }
        }

        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Invalid dimension: '" + dimension + "'.");
        }

        return new ImageDimension(width, height);
    }
}
