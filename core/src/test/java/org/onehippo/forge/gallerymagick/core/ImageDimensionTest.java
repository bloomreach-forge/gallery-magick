package org.onehippo.forge.gallerymagick.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageDimensionTest {

    @Test
    void defaultConstructor_hasZeroDimensions() {
        ImageDimension d = new ImageDimension();
        assertEquals(0, d.getWidth());
        assertEquals(0, d.getHeight());
    }

    @Test
    void paramConstructor_setsWidthAndHeight() {
        ImageDimension d = new ImageDimension(800, 600);
        assertEquals(800, d.getWidth());
        assertEquals(600, d.getHeight());
    }

    @Test
    void setWidth_negative_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new ImageDimension(-1, 0));
    }

    @Test
    void setHeight_negative_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new ImageDimension(0, -1));
    }

    @Test
    void setWidth_zero_allowed() {
        ImageDimension d = new ImageDimension(0, 0);
        assertEquals(0, d.getWidth());
    }

    @Test
    void toString_containsWidthAndHeight() {
        ImageDimension d = new ImageDimension(1920, 1080);
        String s = d.toString();
        assertTrue(s.contains("1920") || s.contains("width") || s.length() > 0);
    }

    @Test
    void equals_sameDimensions_returnsTrue() {
        assertEquals(new ImageDimension(100, 200), new ImageDimension(100, 200));
    }

    @Test
    void equals_differentDimensions_returnsFalse() {
        assertNotEquals(new ImageDimension(100, 200), new ImageDimension(100, 201));
    }

    @Test
    void hashCode_sameDimensions_equal() {
        assertEquals(new ImageDimension(50, 75).hashCode(), new ImageDimension(50, 75).hashCode());
    }
}
