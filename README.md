# Sprite Extractor

A simple program to extract sprites from screenshots.

![Sprite Extractor](http://danjb.com/images/sprite_extractor/sprite_extractor.png)

## Compile

From the `src` directory:

    javac main/*.java

## Run

From the `src` directory:

    java main.SpriteExtractor BG_IMAGE SOURCE_FOLDER STRICTNESS BORDERS

Images are saved to an `out` directory.

### Parameters

#### BG_IMAGE

Filename of the background texture to be removed from the screenshots.

#### SOURCE_FOLDER

Name of the directory containing the screenshots to be processed.

#### STRICTNESS

How strict the program should be when determining whether a pixel belongs to the background (0-7).

**Recommended:** 3- 5

    0 = All background-coloured pixels are removed
    7 = Background-coloured pixels are not removed unless at least 7 of the surrounding pixels match the background texture

Simply removing all background-coloured pixels can cause problems if the sprites themselves contain those colours. Therefore, when considering whether to remove a background-coloured pixel, the program also looks at the surrounding pixels to see if they match the background texture.

Specifically, this parameter is the number of neighbouring pixels that have to match the background texture in order for a background-coloured pixel to be removed.

#### BORDERS

Number of pixels to ignore on each side of the image. Useful for cropping out the UI from screenshots.

Order is: left, top, right, bottom.

### Example

    java main.SpriteExtractor bg.png screenshots 4 10 10 10 128
