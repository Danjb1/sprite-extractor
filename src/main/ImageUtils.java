package main;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;

public final class ImageUtils {

    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * Prevent this class from being instantiated.
     */
    private ImageUtils() {}
    
    /**
     * Saves the given image to a PNG file, if it doesn't already exist.
     * 
     * @param image
     * @param filename
     * @throws IOException
     */
    public static void saveImage(BufferedImage image, String filename)
            throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            ImageIO.write(image, "PNG", file);
        }
    }

    /**
     * Creates a copy of the given BufferedImage.
     * 
     * Note that this always creates an image of type TYPE_INT_ARGB, regardless
     * of the source image type. This is because we want to paint background
     * pixels an arbitrary colour, but some image types would restrict us from
     * doing this.
     * 
     * Based on:
     * https://stackoverflow.com/a/19327237/1624459
     * 
     * @param source
     * @return
     */
    public static BufferedImage copyImage(BufferedImage source) {
        BufferedImage b = new BufferedImage(
                source.getWidth(),
                source.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }

    /**
     * Crops the given image based on the given background colour.
     * 
     * @param image
     * @param backgroundColour
     * @return
     */
    public static BufferedImage crop(BufferedImage image,
            int backgroundColour) {

        int x1 = image.getWidth();
        int y1 = image.getHeight();
        int x2 = 0;
        int y2 = 0;
        
        for (int y = 0; y < image.getHeight(); y++) {
            
            // Find leftmost coloured pixel
            for (int x = 0; x < image.getWidth(); x++) {
                int col = image.getRGB(x, y);
                if (col != backgroundColour && x < x1) {
                    x1 = x;
                }
            }

            // Find rightmost coloured pixel
            for (int x = image.getWidth() - 1; x >= x1; x--) {
                int col = image.getRGB(x, y);
                if (col != backgroundColour && x > x2) {
                    x2 = x;
                }
            }
        }
        
        for (int x = x1; x <= x2; x++) {
            
            // Find topmost coloured pixel
            for (int y = 0; y < image.getHeight(); y++) {
                int col = image.getRGB(x, y);
                if (col != backgroundColour && y < y1) {
                    y1 = y;
                }
            }

            // Find bottom-most coloured pixel
            for (int y = image.getHeight() - 1; y >= y1; y--) {
                int col = image.getRGB(x, y);
                if (col != backgroundColour && y > y2) {
                    y2 = y;
                }
            }
        }
        
        // We have to add 1 here - best explained by example: if the leftmost
        // pixel is 0 and rightmost pixel is 10, width is actually 11 pixels!
        int width = x2 - x1 + 1;
        int height = y2 - y1 + 1;

        return image.getSubimage(x1, y1, width, height);
    }

    /**
     * Generates an MD5 hash of the given image.
     * 
     * Based on:
     * https://sites.google.com/site/matthewjoneswebsite/java/md5-hash-of-an-image
     * 
     * @param image
     * @throws IOException 
     * @throws NoSuchAlgorithmException 
     */
    public static String generateHash(BufferedImage image) throws IOException,
            NoSuchAlgorithmException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", outputStream);
        byte[] data = outputStream.toByteArray();
    
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(data);
        byte[] hash = md.digest();
        return bytesToHex(hash);
    }

    /**
     * Creates a hex string from the given byte array.
     * 
     * Based on:
     * https://stackoverflow.com/a/9855338/1624459
     * 
     * @param bytes
     * @return
     */
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Produces a sub-image of the given image in the ARGB colour model.
     * 
     * @param image
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage copySubimage(BufferedImage image, int x, int y,
            int width, int height) {
        
        BufferedImage newImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image,
                0, 0,
                width, height,
                x, y,
                x + width, y + height,
                null);
        
        return newImage;
    }
    
}
