# Sprite Extractor

A simple program to extract sprites from screenshots.

![Sprite Extractor](http://danjb.com/images/sprite_extractor/sprite_extractor.png)

## Compile

From the `src` directory:

    javac main/*.java

## Run

From the `src` directory:

    java main.SpriteExtractor BG_IMAGE SOURCE_FOLDER MODE STRICTNESS BORDER_LEFT BORDER_TOP BORDER_RIGHT BORDER_BOTTOM

Images are saved to an `out` directory.

### Parameters

#### BG_IMAGE

Filename of the background texture to be removed from the screenshots.

#### SOURCE_FOLDER

Name of the directory containing the screenshots to be processed.

#### MODE

There are 2 modes available here:

**Exact (0)**

In this mode, the supplied background image should be identical to the source images, but with no sprites present. A pixel in the source image will only be considered part of the background if it is the same colour as the pixel at the same point in the background image.

For example, the pixel at (100, 50) in the source image is #105e21. The program will look at the pixel in the background image at (100, 50). If this pixel is also #105e21, the pixel in the source image will be considered part of the background.

**Smart (1)**

In this mode, the supplied background image should be a sample of the background to be removed, but it can be of any size. The program will "learn" the composition of this background texture so that it knows, for each possible pixel colour, which neighbouring pixels to expect, if the given pixel is indeed part of the background.

#### STRICTNESS (0-7)

How strict the program should be when determining whether a pixel belongs to the background.

In **exact mode**, a pixel in the source image will only be considered part of the background if this many neighbouring pixels also match the background. This helps prevent rare cases where a pixel in the sprite just happens to be the exact same colour as the background at that location.

If a pixel matches the background but does *not* have the required number of valid neighbours, it will be highlighted, for manual inspection. This can be disabled by editing `SpriteExtractor.DebugFlags`.

*Recommended: 0-2*

In **smart mode**, a pixel in the source image will only be considered part of the background if this many neighbouring pixels match the expected values "learned" from the background image.

*Recommended: 3-5*

    0 = All background-coloured pixels are removed (problematic if the sprites have any colours in common with the background)
    7 = Background-coloured pixels are not removed unless at least 7 of the surrounding pixels are considered valid

#### BORDER_[LEFT|TOP|RIGHT|BOTTOM]

Number of pixels to ignore on each side of the image. Useful for cropping out the UI from screenshots.

### Example

    java main.SpriteExtractor bg.png screenshots 1 4 10 10 10 128
