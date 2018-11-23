/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.textalignment.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gundram
 */
public class ImageUtil {

    public static Logger LOG = LoggerFactory.getLogger(ImageUtil.class);


    public static boolean write(RenderedImage im,
                                String formatName,
                                File output) {
        try {
            return ImageIO.write(im, formatName, output);
        } catch (IOException ex) {
            throw new RuntimeException("cannot save image to file '" + output == null ? "null" : output.getAbsolutePath() + "' and format '" + formatName + "'.", ex);
        }
    }

    final public static BufferedImage convertColorspace(BufferedImage image, int newType) {
        try {
            BufferedImage raw_image = image;
            image = new BufferedImage(raw_image.getWidth(), raw_image.getHeight(), newType);
            ColorConvertOp xformOp = new ColorConvertOp(null);
            xformOp.filter(raw_image, image);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return image;
    }


    public static BufferedImage copy(BufferedImage original) {
        BufferedImage newImage = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        Graphics g = newImage.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return newImage;
    }

    private static List<Color> colorMap2 = new ArrayList<>();
    private static List<Color> colorMap3 = new ArrayList<>();
    private static List<Color> colorMap4 = new ArrayList<>();
    private static List<Color> colorMap5 = new ArrayList<>();
    private static List<Color> colorMap7 = new ArrayList<>();
//    private static List<Color> colorMap5;

    static {
        int[] black = new int[]{0, 0, 0};//black
        int[] blue = new int[]{0, 0, 1};//blue
        int[] cyan = new int[]{0, 1, 1};//cyan
        int[] green = new int[]{0, 1, 0};//green
        int[] yellow = new int[]{1, 1, 0};//yellow
        int[] red = new int[]{1, 0, 0};//red
        int[] white = new int[]{1, 1, 1};//white
        colorMap7 = getMap(new int[][]{black, blue, cyan, green, yellow, red, white});
        colorMap4 = getMap(new int[][]{black, red, yellow, white});
        colorMap5 = getMap(new int[][]{blue, cyan, green, yellow, red});
        colorMap3 = getMap(new int[][]{blue, green, red});
        colorMap2 = getMap(new int[][]{black, white});
    }

    private static List<Color> getMap(int[][] colors) {
        List<Color> res = new ArrayList<>(256 * (colors.length - 1));
        for (int c = 0; c < colors.length - 1; c++) {
            int[] low = colors[c];
            int[] high = colors[c + 1];
            for (int i = 0; i < 256; i++) {
                res.add(new Color(
                        high[0] * i + low[0] * (255 - i),
                        high[1] * i + low[1] * (255 - i),
                        high[2] * i + low[2] * (255 - i)));
            }
        }
        return res;
    }

    private static double[] getMinMax(double[][] mat) {
        double min = mat[0][0];
        double max = mat[0][0];
        for (int i = 0; i < mat.length; i++) {
            double[] vec = mat[i];
            for (int j = 0; j < vec.length; j++) {
                min = Math.min(min, vec[j]);
                max = Math.max(max, vec[j]);
            }
        }
        return new double[]{min, max};
    }

    public static BufferedImage getHeatMap(double[][] matrix, int colors) {
        return getHeatMap(matrix, colors, false);
    }

    public static BufferedImage getHeatMap(double[][] matrix, int colors, boolean invert) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (double[] ds : matrix) {
            for (int i = 0; i < ds.length; i++) {
                min = Math.min(ds[i], min);
                max = Math.max(ds[i], max);
            }
        }
        return invert ? getHeatMap(matrix, max, min, colors) : getHeatMap(matrix, min, max, colors);
    }

    public static BufferedImage getHeatMap(double[][] matrix, double low, double high, int colors) {
        List<Color> colorMap = null;
        switch (colors) {
            case 2:
                colorMap = colorMap2;
                break;
            case 3:
                colorMap = colorMap3;
                break;
            case 4:
                colorMap = colorMap4;
                break;
            case 5:
                colorMap = colorMap5;
                break;
            case 7:
                colorMap = colorMap7;
                break;
            default:
                throw new RuntimeException("unknown color count " + colors);
        }
        double factor = (colorMap.size() - 1) / (high - low);
        double offset = 0.5 - low * factor;
        BufferedImage bi = new BufferedImage(matrix[0].length, matrix.length, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < matrix.length; i++) {
            double[] vec = matrix[i];
            for (int j = 0; j < vec.length; j++) {
                bi.setRGB(j, i, colorMap.get((int) (vec[j] * factor + offset)).getRGB());
            }
        }
        return bi;
    }

}
