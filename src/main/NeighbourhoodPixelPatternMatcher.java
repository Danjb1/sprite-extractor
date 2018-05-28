package main;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * PixelPatternMatcher that "learns" the composition of a background texture so
 * that it knows, for each possible pixel colour, which neighbouring pixels to
 * expect, if the given pixel is indeed part of the background.
 * 
 * It can then make a decision as to whether the pixel belongs to the
 * background, using a configurable strictness setting.
 * 
 * @author Dan Bryce
 */
public class NeighbourhoodPixelPatternMatcher implements PixelPatternMatcher {

    /**
     * Class that specifies a set of acceptable pixel colours for each
     * neighbour in a Moore neighbourhood.
     */
    private static class ValidNeighbours {
        public Set<Integer> north     = new HashSet<>();
        public Set<Integer> northEast = new HashSet<>();
        public Set<Integer> east      = new HashSet<>();
        public Set<Integer> southEast = new HashSet<>();
        public Set<Integer> south     = new HashSet<>();
        public Set<Integer> southWest = new HashSet<>();
        public Set<Integer> west      = new HashSet<>();
        public Set<Integer> northWest = new HashSet<>();
    }

    /**
     * Number of neighbouring pixels that must match the background texture
     * before a pixel will be matched.
     */
    private int strictness;

    /**
     * Map of pixel colour -> valid neighbours for that colour.
     */
    private Map<Integer, ValidNeighbours> entries = new HashMap<>();

    /**
     * Creates a NeighbourhoodPixelPatternMatcher from the given image.
     * 
     * @param image
     * @param strictness
     */
    public NeighbourhoodPixelPatternMatcher(BufferedImage image, int strictness) {

        // Loop over the image, ignoring edge pixels
        for (int y = 1; y < image.getHeight() - 1; y++) {
            for (int x = 1; x < image.getWidth() - 1; x++) {

                int col = image.getRGB(x, y);
                
                ValidNeighbours validNeighbours = entries.get(col);
                
                if (validNeighbours == null) {
                    // This is the first pixel of this colour we have seen
                    validNeighbours = new ValidNeighbours();
                    entries.put(col, validNeighbours);
                }
                
                // Add each neighbouring colour to this pixel's valid neighbours
                validNeighbours.north.add(    image.getRGB(x + 0, y - 1));
                validNeighbours.northEast.add(image.getRGB(x + 1, y - 1));
                validNeighbours.east.add(     image.getRGB(x + 1, y + 0));
                validNeighbours.southEast.add(image.getRGB(x + 1, y + 1));
                validNeighbours.south.add(    image.getRGB(x + 0, y + 1));
                validNeighbours.southWest.add(image.getRGB(x - 1, y + 1));
                validNeighbours.west.add(     image.getRGB(x - 1, y + 0));
                validNeighbours.northWest.add(image.getRGB(x - 1, y - 1));
            }
        }
        
        this.strictness = strictness;

        System.out.println("Pattern contains " + entries.size() + " colours");
    }

    public boolean matches(BufferedImage image, int x, int y) {

        int col = image.getRGB(x, y);
        
        ValidNeighbours validNeighbours = entries.get(col);
        if (validNeighbours == null) {
            // This pixel colour is not present in this pattern's texture
            return false;
        }
        
        int numValidNeighbours = 0;
        
        if (validNeighbours.north.contains(image.getRGB(x + 0, y - 1))) {
            numValidNeighbours++;
        }

        if (validNeighbours.northEast.contains(image.getRGB(x + 1, y - 1))) {
            numValidNeighbours++;
        }

        if (validNeighbours.east.contains(image.getRGB(x + 1, y + 0))) {
            numValidNeighbours++;
        }

        if (validNeighbours.southEast.contains(image.getRGB(x + 1, y + 1))) {
            numValidNeighbours++;
        }

        if (validNeighbours.south.contains(image.getRGB(x + 0, y + 1))) {
            numValidNeighbours++;
        }

        if (validNeighbours.southWest.contains(image.getRGB(x - 1, y + 1))) {
            numValidNeighbours++;
        }

        if (validNeighbours.west.contains(image.getRGB(x - 1, y + 0))) {
            numValidNeighbours++;
        }

        if (validNeighbours.northWest.contains(image.getRGB(x - 1, y - 1))) {
            numValidNeighbours++;
        }

        return numValidNeighbours >= strictness;
    }

}
