package main;

import java.awt.image.BufferedImage;

/**
 * PixelPatternMatcher that expects pixels to *exactly* match the corresponding
 * pixels of the pre-supplied background texture.
 * 
 * So, a pixel in an image will only be considered part of the background if it
 * is the same colour as the pixel at the same point in the background image.
 *
 * @author Dan Bryce
 */
public class ExactPixelPatternMatcher implements PixelPatternMatcher {

    private BufferedImage background;
    
    public ExactPixelPatternMatcher(
            BufferedImage background,
            int borderLeft,
            int borderTop,
            int borderRight,
            int borderBottom) {
        
        // Cut the background to the part we are interested in, just like we do
        // for the image being processed
        this.background = background.getSubimage(
                borderLeft,
                borderTop,
                background.getWidth() - (borderLeft + borderRight),
                background.getHeight() - (borderTop + borderBottom));
    }
    
    @Override
    public boolean matches(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) == background.getRGB(x, y);
    }

}
