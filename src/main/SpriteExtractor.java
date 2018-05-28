package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Class capable of extracting sprites from an image.
 * 
 * <p>The process is as follows:
 * 
 * <ol>
 *  <li>Remove any background pixels.</li>
 *  <li>Identify regions of the image that still contain pixel data.</li>
 *  <li>Crop each region so that it contains no empty space.</li>
 *  <li>Save each region to a separate file.</li>
 * </ol>
 *
 * @author Dan Bryce
 */
public class SpriteExtractor {

    /**
     * Flags used for debugging purposes.
     */
    private static final class DebugFlags {
        
        /**
         * If set, no sprites will be saved.
         * 
         * Instead, a copy of the original image will be saved, with all
         * identified sprite regions framed in red.
         */
        public static final boolean DRAW_SPRITE_BORDERS = false;
    }
    
    /**
     * Width of a sprite region.
     * 
     * A region is created around each sprite after the background has been
     * removed. If this is too small, sprites may get cut up into multiple
     * images. If this is too large, multiple sprites may end up getting
     * included in the same image.
     */
    private static final int SPRITE_REGION_WIDTH = 192;

    /**
     * Height of a sprite region.
     * 
     * See SPRITE_REGION_WIDTH.
     */
    private static final int SPRITE_REGION_HEIGHT = 192;

    /**
     * Offset at which each sprite region is created, measured in a backwards
     * direction from the first pixel of the sprite that is found when searching
     * for non-empty pixels.
     *
     * If this is too small or too large, sprites may get cut up into multiple
     * images.
     */
    private static final int SPRITE_REGION_OFFSET_X = 64;

    /**
     * See SPRITE_REGION_OFFSET_X.
     */
    private static final int SPRITE_REGION_OFFSET_Y = 64;
    
    /**
     * Background colour to use for the produced sprites.
     */
    private static final int BG_COLOUR = 0xff80c0ff;

    /**
     * Regex used to match image filenames.
     */
    private static final String IMAGE_FILENAME_REGEX = 
            ".+\\.(?i)(bmp|jpg|gif|png)";

    /**
     * Minimum width of a sprite.
     * 
     * Sprites smaller than this will not be saved.
     */
    private static final int MIN_SPRITE_WIDTH = 4;

    /**
     * Minimum height of a sprite.
     * 
     * Sprites smaller than this will not be saved.
     */
    private static final int MIN_SPRITE_HEIGHT = 4;

    /**
     * PixelPatternMatcher used to match the background texture.
     */
    private PixelPatternMatcher pattern;

    /**
     * Ignored left margin of the input image (pixels).
     */
    private int borderLeft;

    /**
     * Ignored top margin of the input image (pixels).
     */
    private int borderTop;

    /**
     * Ignored right margin of the input image (pixels).
     */
    private int borderRight;

    /**
     * Ignored bottom margin of the input image (pixels).
     */
    private int borderBottom;

    /**
     * Constructs a SpriteExtractor with the given configuration.
     * 
     * @param pattern
     * @param borderLeft
     * @param borderTop
     * @param borderRight
     * @param borderBottom
     */
    public SpriteExtractor(
            PixelPatternMatcher pattern,
            int borderLeft,
            int borderTop,
            int borderRight,
            int borderBottom) {
        this.pattern = pattern;
        this.borderLeft = borderLeft;
        this.borderTop = borderTop;
        this.borderRight = borderRight;
        this.borderBottom = borderBottom;
    }

    /**
     * Extracts all sprites from the given image.
     * 
     * @param image
     * @return
     */
    private List<BufferedImage> process(BufferedImage image) {
        
        // Cut the image to the part we are interested in
        BufferedImage subImage = image.getSubimage(
                borderLeft,
                borderTop,
                image.getWidth() - (borderLeft + borderRight),
                image.getHeight() - (borderTop + borderBottom));
        
        // Remove the background texture
        subImage = clearBackgroundPixels(subImage);

        // Identify regions that still contain colour
        List<Rectangle> regions = getColourRegions(subImage);
        System.out.println("Found " + regions.size() + " sprite regions");
        
        if (DebugFlags.DRAW_SPRITE_BORDERS) {
            Graphics g = subImage.getGraphics();
            g.setColor(Color.RED);
            for (Rectangle region : regions) {
                g.drawRect(region.x, region.y, region.width, region.height);
            }
            List<BufferedImage> spriteList = new ArrayList<>();
            spriteList.add(image);
            return spriteList;
        }

        return getCroppedSprites(subImage, regions);
    }
    
    /**
     * Removes all of the background colours from the given image.
     * 
     * @param image
     */
    private BufferedImage clearBackgroundPixels(BufferedImage image) {

        // We need to keep the original image in-tact so that our pattern
        // can check the pixels!
        BufferedImage newImage = ImageUtils.copyImage(image);
        
        // Ignore edge pixels as the pattern won't work on them
        for (int y = 1; y < image.getHeight() - 1; y++) {
            for (int x = 1; x < image.getWidth() - 1; x++) {

                if (pattern.matches(image, x, y)) {
                    newImage.setRGB(x, y, BG_COLOUR);
                }
            }
        }
        
        return newImage;
    }

    /**
     * Produces a list of regions containing non-blank pixels.
     * 
     * @param image
     * @return
     */
    private List<Rectangle> getColourRegions(BufferedImage image) {
        
        List<Rectangle> regions = new ArrayList<>();

        // Skip the edge pixels as they are never modified
        for (int y = 1; y < image.getHeight() - 1; y++) {
            for (int x = 1; x < image.getWidth() - 1; x++) {
                
                int col = image.getRGB(x, y);
                
                // We want to find all non-background pixels that have not
                // already been captured by another region
                if (col != BG_COLOUR && 
                        !doRegionsContainPixel(regions, x, y)) {
                    image.setRGB(x, y, col);
                    regions.add(createRegion(x, y));
                }
            }
        }
        
        return regions;
    }

    /**
     * Determines if any of the given regions contain the given point.
     * 
     * @param regions
     * @param x
     * @param y
     * @return
     */
    private boolean doRegionsContainPixel(List<Rectangle> regions, 
            int x, int y) {
        for (Rectangle rect : regions) {
            if (rect.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a region around the given pixel.
     * 
     * @param x
     * @param y
     * @return
     */
    private Rectangle createRegion(int x, int y) {
        return new Rectangle(
                x - SPRITE_REGION_OFFSET_X,
                y - SPRITE_REGION_OFFSET_Y,
                SPRITE_REGION_WIDTH,
                SPRITE_REGION_HEIGHT);
    }

    /**
     * Extracts and crops the given sprite regions from the given image.
     * 
     * @param image
     * @param regions
     * @return
     */
    private List<BufferedImage> getCroppedSprites(BufferedImage image,
            List<Rectangle> regions) {
        
        List<BufferedImage> sprites = new ArrayList<>();

        for (Rectangle region : regions) {
            
            // Don't try to go outside the image bounds
            int x = Math.max(0, region.x);
            int y = Math.max(0, region.y);
            x = Math.min(x, image.getWidth() - SPRITE_REGION_WIDTH);
            y = Math.min(y, image.getHeight() - SPRITE_REGION_HEIGHT);
            
            BufferedImage subImage = image.getSubimage(
                    x, y, region.width, region.height);
            
            subImage = ImageUtils.crop(subImage, BG_COLOUR);
            
            if (subImage.getWidth() < MIN_SPRITE_WIDTH ||
                    subImage.getHeight() < MIN_SPRITE_HEIGHT) {
                // Sprite is too small, ignore it
                continue;
            }
            
            sprites.add(subImage);
        }
        
        return sprites;
    }

    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Entry point for the application.
     * 
     * @param args
     */
    public static void main(String[] args) {
        
        if (args.length < 7) {
            System.out.println("Expected: " +
                    "BG_IMAGE " +
                    "SOURCE_FOLDER " +
                    "STRICTNESS " +
                    "BORDER_LEFT " +
                    "BORDER_TOP " +
                    "BORDER_RIGHT " + 
                    "BORDER_BOTTOM");
            System.exit(-1);
        }

        SpriteExtractor se = null;
        
        String bgFilename = args[0];
        String imageDir = args[1];
        
        try {
            
            int strictness   = Integer.parseInt(args[2]);
            int borderLeft   = Integer.parseInt(args[3]);
            int borderTop    = Integer.parseInt(args[4]);
            int borderRight  = Integer.parseInt(args[5]);
            int borderBottom = Integer.parseInt(args[6]);
            
            System.out.println("Reading background texture");
            BufferedImage bgImage = ImageIO.read(new File(bgFilename));

            System.out.println("Producing pattern matcher");
            PixelPatternMatcher pattern;
            if (strictness == -1) {
                pattern = new ExactPixelPatternMatcher(
                        bgImage,
                        borderLeft,
                        borderTop,
                        borderRight,
                        borderBottom);
            } else {
                pattern = new NeighbourhoodPixelPatternMatcher(
                        bgImage,
                        strictness);
            }
            
            se = new SpriteExtractor(
                    pattern,
                    borderLeft,
                    borderTop,
                    borderRight,
                    borderBottom);

        } catch (NumberFormatException ex) {
            System.out.println("Argument is not a valid integer!");
            System.exit(1);
        } catch (IOException ex) {
            System.out.println("Unable to read background texture");
            ex.printStackTrace();
            System.exit(1);
        }
        
        // Find all screenshots in directory
        System.out.println("Finding files");
        File dir = new File(imageDir);
        File[] imageFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(IMAGE_FILENAME_REGEX);
            }
        });

        if (imageFiles.length == 0) {
            System.out.println("No image files found in directory: " + 
                    dir.getAbsolutePath());
            System.exit(1);
        }
        
        List<String> processedHashes = new ArrayList<>();
        List<String> spriteHashes = new ArrayList<>();

        // Extract the sprites from each screenshot
        for (File file : imageFiles) {
            
            BufferedImage image = null;

            // Read image
            try {
                System.out.println("Reading image: " + file);
                image = ImageIO.read(file);
            } catch (IOException ex) {
                System.out.println("Unable to read image");
                ex.printStackTrace();
                continue;
            }
            
            // Skip duplicate images
            try {
                String hash = ImageUtils.generateHash(image);
                if (processedHashes.contains(hash)) {
                    System.out.println("Skipping duplicate input image");
                    continue;
                }
                processedHashes.add(hash);
            } catch (NoSuchAlgorithmException | IOException e) {
                System.out.println("Error generating hash for image");
            }

            // Extract the sprites
            System.out.println("Processing...");
            List<BufferedImage> sprites = se.process(image);
            System.out.println("Extracted " + sprites.size() + " sprites");
            
            // Ensure "out" directory exists
            new File("out").mkdir();
            
            // Save new sprites
            for (int i = 0; i < sprites.size(); i++) {
                
                BufferedImage sprite = sprites.get(i);
                
                // Don't save duplicates
                try {
                    String hash = ImageUtils.generateHash(sprite);
                    if (spriteHashes.contains(hash)) {
                        System.out.println("Skipping duplicate sprite");
                        continue;
                    }
                    spriteHashes.add(hash);
                } catch (NoSuchAlgorithmException | IOException e) {
                    System.out.println("Error generating hash for sprite");
                }

                String filename = file.getName();
                
                // Remove extension
                int pos = filename.lastIndexOf(".");
                if (pos > 0) {
                    filename = filename.substring(0, pos);
                }
                
                filename = "out/" + filename + "_" + i + ".png";
                
                try {
                    ImageUtils.saveImage(sprite, filename);
                } catch (IOException e) {
                    System.out.println("Unable to save image");
                    e.printStackTrace();
                }
            }
        }
        
        System.out.println("Success!");
    }

}
