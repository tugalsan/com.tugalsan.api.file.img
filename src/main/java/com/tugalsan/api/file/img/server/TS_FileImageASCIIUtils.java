package com.tugalsan.api.file.img.server;

import java.util.stream.*;
import java.awt.*;
import java.awt.image.*;

public class TS_FileImageASCIIUtils {
    
    private TS_FileImageASCIIUtils(){
        
    }

    public static String convert(BufferedImage image, boolean negative) {
        var sb = new StringBuilder((image.getWidth() + 1) * image.getHeight());
        IntStream.range(0, image.getHeight()).forEachOrdered(y -> {
            if (sb.length() != 0) {
                sb.append("\n");
            }
            IntStream.range(0, image.getWidth()).forEachOrdered(x -> {
                var pixelColor = new Color(image.getRGB(x, y));
                var gValue = pixelColor.getRed() * 0.2989d + pixelColor.getBlue() * 0.5870d + pixelColor.getGreen() * 0.1140d;
                var s = negative ? returnStrNeg(gValue) : returnStrPos(gValue);
                sb.append(s);
            });
        });
        return sb.toString();
    }

    /**
     * Create a new string and assign to it a string based on the grayscale
     * value. If the grayscale value is very high, the pixel is very bright and
     * assign characters such as . and , that do not appear very dark. If the
     * grayscale value is very lowm the pixel is very dark, assign characters
     * such as # and @ which appear very dark.
     *
     * @param g grayscale
     * @return char
     */
    private static char returnStrPos(double g)//takes the grayscale value as parameter
    {
        final char str;

        if (g >= 230.0) {
            str = ' ';
        } else if (g >= 200.0) {
            str = '.';
        } else if (g >= 180.0) {
            str = '*';
        } else if (g >= 160.0) {
            str = ':';
        } else if (g >= 130.0) {
            str = 'o';
        } else if (g >= 100.0) {
            str = '&';
        } else if (g >= 70.0) {
            str = '8';
        } else if (g >= 50.0) {
            str = '#';
        } else {
            str = '@';
        }
        return str; // return the character

    }

    /**
     * Same method as above, except it reverses the darkness of the pixel. A
     * dark pixel is given a light character and vice versa.
     *
     * @param g grayscale
     * @return char
     */
    private static char returnStrNeg(double g) {
        final char str;

        if (g >= 230.0) {
            str = '@';
        } else if (g >= 200.0) {
            str = '#';
        } else if (g >= 180.0) {
            str = '8';
        } else if (g >= 160.0) {
            str = '&';
        } else if (g >= 130.0) {
            str = 'o';
        } else if (g >= 100.0) {
            str = ':';
        } else if (g >= 70.0) {
            str = '*';
        } else if (g >= 50.0) {
            str = '.';
        } else {
            str = ' ';
        }
        return str;
    }
}
