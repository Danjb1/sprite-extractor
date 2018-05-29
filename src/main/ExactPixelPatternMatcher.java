package main;

import java.awt.image.BufferedImage;

/**
 * PixelPatternMatcher that expects pixels to *exactly* match the corresponding
 * pixels of the pre-supplied background texture.
 * 
 * So, a pixel in an image will only be considered part of the background if it
 * is the same colour as the pixel at the same point in the background image.
 * 
 * The strictness parameter controls the number of neighbouring pixels that must
 * also match the background for a pixel to match. This helps prevent rare cases
 * where a pixel in the sprite just happens to be the exact same colour as the
 * background at that location.
 *
 * @author Dan Bryce
 */
public class ExactPixelPatternMatcher implements PixelPatternMatcher {

    private BufferedImage background;
    
    private int strictness;
    
    public ExactPixelPatternMatcher(
            BufferedImage background,
            int strictness,
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

        this.strictness = strictness;
    }
    
    @Override
    public boolean matches(BufferedImage image, int x, int y) {
        
        if (image.getRGB(x, y) != background.getRGB(x, y)) {
            // The pixel does not match the background
            return false;
        }
        
        // The pixel matches the background - how many neighbours also match?
        int validNeighbours = 0;

        // North
        if (image.getRGB(x, y - 1) == background.getRGB(x, y - 1)) {
            validNeighbours++;
        }

        // North-east
        if (image.getRGB(x + 1, y - 1) == background.getRGB(x + 1, y - 1)) {
            validNeighbours++;
        }

        // East
        if (image.getRGB(x + 1, y) == background.getRGB(x + 1, y)) {
            validNeighbours++;
        }

        // South-east
        if (image.getRGB(x + 1, y + 1) == background.getRGB(x + 1, y + 1)) {
            validNeighbours++;
        }

        // South
        if (image.getRGB(x, y + 1) == background.getRGB(x, y + 1)) {
            validNeighbours++;
        }

        // South-west
        if (image.getRGB(x - 1, y + 1) == background.getRGB(x - 1, y + 1)) {
            validNeighbours++;
        }

        // West
        if (image.getRGB(x - 1, y) == background.getRGB(x - 1, y)) {
            validNeighbours++;
        }

        // North-west
        if (image.getRGB(x - 1, y - 1) == background.getRGB(x - 1, y - 1)) {
            validNeighbours++;
        }

        if (validNeighbours >= strictness) {
            return true;
        }
        
        System.out.println("Pixel matches background, but only has " +
                validNeighbours + " valid neighbours!");
        
        if (SpriteExtractor.DebugFlags.HIGHLIGHT_UNCERTAIN_PIXELS) {
            image.setRGB(x, y, SpriteExtractor.DebugFlags.HIGHLIGHT_COLOUR);
        }
        
        return false;
    }

}
