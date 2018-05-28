package main;

import java.awt.image.BufferedImage;

/**
 * Classes implementing this interface are capable of determining, for each
 * pixel in an image, whether it matches a pre-supplied background texture.
 *
 * @author Dan Bryce
 */
public interface PixelPatternMatcher {

    /**
     * Determines whether the given pixel belongs to the background texture.
     * 
     * @param image
     * @param x
     * @param y
     * @return
     */
    boolean matches(BufferedImage image, int x, int y);

}
