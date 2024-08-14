package com.tugalsan.api.file.img.server;

import com.tugalsan.api.crypto.client.TGS_CryptUtils;
import java.net.*;
import java.util.*;
import java.util.stream.*;
import java.io.*;
import java.nio.file.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.imageio.*;
import net.coobird.thumbnailator.*;
import com.tugalsan.api.shape.client.*;
import com.tugalsan.api.log.server.*;
import com.tugalsan.api.random.server.*;
import com.tugalsan.api.stream.client.TGS_StreamUtils;
import com.tugalsan.api.union.client.TGS_UnionExcuse;
import com.tugalsan.api.unsafe.client.*;
import com.tugalsan.api.url.client.*;
import java.util.List;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;

public class TS_FileImageUtils {

    final private static TS_Log d = TS_Log.of(TS_FileImageUtils.class);

    public static TGS_UnionExcuse<BufferedImage> filter(BufferedImage imageSource, float[] filter, boolean zeroFillEnable) {
        return TGS_UnSafe.call(() -> {
            var dimension = (int) Math.sqrt(filter.length);
            var kernel = new Kernel(dimension, dimension, filter);
            var convolveOp = new ConvolveOp(kernel, zeroFillEnable ? ConvolveOp.EDGE_ZERO_FILL : ConvolveOp.EDGE_NO_OP, null);
            var imageDest = convolveOp.filter(imageSource, null);
            return TGS_UnionExcuse.of(imageDest);
        }, e -> TGS_UnionExcuse.ofExcuse(e));
    }

    public static float[] FILTER_IDENTITY() {
        return new float[]{
            0, 0, 0,
            0, 1, 0,
            0, 0, 0
        };
    }

    public static float[] FILTER_EDGE_DETECTION_0() {
        return new float[]{
            0, -1, 0,
            -1, 4, -1,
            0, -1, 0
        };
    }

    public static float[] FILTER_EDGE_DETECTION_1() {
        return new float[]{
            -1, -1, -1,
            -1, 8, -1,
            -1, -1, -1
        };
    }

    public static float[] FILTER_SHARPEN() {
        return new float[]{
            0, -1, 0,
            -1, 5, -1,
            0, -1, 0
        };
    }

    public static float[] FILTER_BLUR_BLOX() {
        return new float[]{
            1f / 9, 1f / 9, 1f / 9,
            1f / 9, 1f / 9, 1f / 9,
            1f / 9, 1f / 9, 1f / 9
        };
    }

    public static float[] FILTER_BLUR_GAUSSIAN_3_X_3() {
        return new float[]{
            1f / 16, 2f / 16, 1f / 16,
            2f / 16, 4f / 16, 2f / 16,
            1f / 16, 2f / 16, 1f / 16
        };
    }

    public static float[] FILTER_BLUR_GAUSSIAN_5_X_5() {
        return new float[]{
            1f / 256, 4f / 256, 6f / 256, 4f / 256, 1f / 256,
            4f / 256, 16f / 256, 24f / 256, 16f / 256, 4f / 256,
            6f / 256, 24f / 256, 36f / 256, 24f / 256, 6f / 256,
            4f / 256, 16f / 256, 24f / 256, 16f / 256, 4f / 256,
            1f / 256, 4f / 256, 6f / 256, 4f / 256, 1f / 256
        };
    }

    public static float[] FILTER_UNSHARP_MASKING() {
        return new float[]{
            1f / 256, 4f / -256, 6f / -256, 4f / -256, 1f / -256,
            4f / 256, 16f / -256, 24f / -256, 16f / -256, 4f / -256,
            6f / 256, 24f / -256, -476f / -256, 24f / -256, 6f / -256,
            4f / 256, 16f / -256, 24f / -256, 16f / -256, 4f / -256,
            1f / 256, 4f / -256, 6f / -256, 4f / -256, 1f / -256
        };
    }

    //ImageIO.write(renderedImage, "png", os);
    public static List<String[]> formatNames(String[] args) {
        return TGS_StreamUtils.toLst(
                TGS_StreamUtils.of(
                        IIORegistry.getDefaultInstance()
                                .getServiceProviders(ImageWriterSpi.class, false)
                ).map(item -> item.getFormatNames())
        );
    }

    public static BufferedImage toImage(CharSequence sourceText, int width, int height, int x, int y, Color colorFore, Color colorBack, Font font) {
        var bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var g = bi.createGraphics();
        g.setColor(colorBack == null ? Color.WHITE : colorBack);
        g.fillRect(0, 0, width, height);
        g.setColor(colorFore == null ? Color.BLACK : colorFore);
        if (font != null) {
            g.setFont(font);
        }
        g.drawString(sourceText.toString(), x, y);
        g.dispose();
        return bi;
    }

    public static BufferedImage readImageFromFile(Path sourceImage, boolean cast2RGB) {
        return TGS_UnSafe.call(() -> {
            var bufferedImage = ImageIO.read(sourceImage.toFile());
            return cast2RGB ? toImageRGB(bufferedImage) : bufferedImage;
        });
    }

    public static void resize(BufferedImage src, BufferedImage desWithSize, boolean clever) {
        var g = desWithSize.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setBackground(Color.BLACK);//UIManager.getColor("Panel.background"));
        g.clearRect(0, 0, desWithSize.getWidth(), desWithSize.getHeight());
        if (clever) {
            var xScale = (double) desWithSize.getWidth() / src.getWidth();
            var yScale = (double) desWithSize.getHeight() / src.getHeight();
            var scale = Math.min(xScale, yScale);
            var width = (int) (scale * src.getWidth());
            var height = (int) (scale * src.getHeight());
            var x = (desWithSize.getWidth() - width) / 2;
            var y = (desWithSize.getHeight() - height) / 2;
            g.drawImage(src, x, y, width, height, null);
        } else {
            g.drawImage(src, 0, 0, desWithSize.getWidth(), desWithSize.getHeight(), 0, 0, src.getWidth(), src.getHeight(), null);
        }
        g.dispose();
    }

    public static BufferedImage rotateGraphics2DAffineTransform(BufferedImage src, float angle) {
        var des = new BufferedImage(src.getHeight(), src.getWidth(), BufferedImage.TYPE_INT_RGB);
        rotateGraphics2DAffineTransform(src, angle, des);
        return des;
    }

    public static void rotateGraphics2DAffineTransform(BufferedImage src, float angle, BufferedImage des) {
        var g2d = des.createGraphics();
        var origAT = g2d.getTransform();
        var rot = new AffineTransform();
        rot.rotate(angle, des.getWidth() / 2, des.getHeight() / 2);
        g2d.transform(rot);
        g2d.drawImage(src, 0, 0, null); // copy in the image
        g2d.setTransform(origAT); // restore original transform
        g2d.dispose();
    }

    public static BufferedImage rotateGraphics2DAffineTransformby90(BufferedImage image) {
        var rads = Math.toRadians(90);
        var sin = Math.abs(Math.sin(rads));
        var cos = Math.abs(Math.cos(rads));
        var w = (int) Math.floor(image.getWidth() * cos + image.getHeight() * sin);
        var h = (int) Math.floor(image.getHeight() * cos + image.getWidth() * sin);
        var rotatedImage = new BufferedImage(w, h, image.getType());
        var at = new AffineTransform();
        at.translate(w / 2, h / 2);
        at.rotate(rads, 0, 0);
        at.translate(-image.getWidth() / 2, -image.getHeight() / 2);
        var rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        return rotateOp.filter(image, rotatedImage);
    }

    public static BufferedImage rotateGraphics2Dby90(BufferedImage src) {
        var des = new BufferedImage(src.getHeight(), src.getWidth(), src.getType());
        var g2d = (Graphics2D) des.getGraphics();
        g2d.rotate(Math.toRadians(90.0));
        g2d.drawImage(src, 0, -des.getWidth(), null);
        g2d.dispose();
        return des;
    }

    public static void rotateGraphics2D(BufferedImage src, float angle, BufferedImage des) {
        var w = src.getWidth();
        var h = src.getHeight();
        var g = des.createGraphics();
        g.rotate(angle, w / 2, h / 2);
        g.drawImage(src, null, 0, 0);
    }

    public static void clear(BufferedImage target, Color backgroundColor) {
        var g2d = target.createGraphics();
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, target.getWidth(), target.getHeight());
        g2d.dispose();
    }

    public static BufferedImage toImage(Path source) {
        return TGS_UnSafe.call(() -> {
            var imgbytes = Files.readAllBytes(source);
            return toImage(imgbytes);
        });
    }

    public static BufferedImage toImage(byte[] imgbytes) {
        return TGS_UnSafe.call(() -> {
            try (var bis = new ByteArrayInputStream(imgbytes)) {
                return ImageIO.read(bis);
            }
        });
    }

    public static BufferedImage toImageRGB(BufferedImage preImage) {
        var convertToRGB = new BufferedImage(preImage.getWidth(), preImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        convertToRGB.createGraphics().drawImage(preImage, 0, 0, Color.WHITE, null);
        return convertToRGB;
    }

    public static BufferedImage resize_and_rotate(BufferedImage preImage, TGS_ShapeDimension<Integer> newDim0, Integer rotate0, boolean respect) {
        return TGS_UnSafe.call(() -> {
            d.ci("resize_and_rotate.init: ", preImage.getClass().getSimpleName());

            var rotate = rotate0;
            var newDim = newDim0;

            if (rotate == null) {
                rotate = 0;
            } else {
                while (rotate < 0) {
                    rotate += 360;
                }
                rotate = rotate % 360;
            }
            d.ci("resize_and_rotate.fixRotate: ", rotate);

            TGS_ShapeDimension<Integer> org = new TGS_ShapeDimension(preImage.getWidth(), preImage.getHeight());
            if (newDim == null) {
                newDim = org.cloneIt();
            }
            if (newDim.width < 1) {
                newDim.width = null;
            }
            if (newDim.height < 1) {
                newDim.height = null;
            }
            if (newDim.width == null && newDim.height == null) {
                newDim.sniffFrom(org);
            } else if (newDim.width == null) {
                newDim.width = newDim.height * org.width / org.height;
            } else if (newDim.height == null) {
                newDim.height = newDim.width * org.height / org.width;
            } else if (respect) {
                var widthByHeight = newDim.height * org.width / org.height;
                var heightByWidth = newDim.width * org.height / org.width;
                if (heightByWidth < newDim.height) {
                    newDim.height = heightByWidth;
                } else {
                    newDim.width = widthByHeight;
                }
            }
            d.ci("resize_and_rotate.fixDim: ", org.width, org.height, newDim.width, newDim.height, "@r:", rotate);

            var b = Thumbnails.of(preImage);
            b = b.size(newDim.width, newDim.height);
            if (rotate != 0) {
                b = b.rotate(rotate);
            }
            d.ci("resize_and_rotate.fin");
            return b.asBufferedImage();
        });
    }

    public static BufferedImage autoSizeRespectfully(BufferedImage bi, TGS_ShapeDimension<Integer> max, float quality_fr0_to1) {
        return TGS_UnSafe.call(() -> {
            var b = Thumbnails.of(bi);
            d.ci("castFromIMGtoPDF_A4PORT", "init", bi.getWidth(), bi.getHeight());
            if ((max.width < max.height && bi.getWidth() > bi.getHeight()) || (max.width > max.height && bi.getWidth() < bi.getHeight())) {
                d.ci("castFromIMGtoPDF_A4PORT", "rotated");
                b = b.rotate(90);
            }
            var dimBi = new TGS_ShapeDimension<Integer>(bi.getWidth(), bi.getHeight());
            d.ci("castFromIMGtoPDF_A4PORT", "mid", dimBi.width, dimBi.height);

            var scaleFactorW = 1f * max.width / dimBi.width;
            d.ci("castFromIMGtoPDF_A4PORT", "scaleFactorW", scaleFactorW);
            var scaleFactorH = 1f * max.height / dimBi.height;
            d.ci("castFromIMGtoPDF_A4PORT", "scaleFactorH", scaleFactorH);
            var scaleFactor = Math.min(scaleFactorW, scaleFactorH);
            d.ci("castFromIMGtoPDF_A4PORT", "scaleFactor", scaleFactor);
            b = b.scale(scaleFactor).outputQuality(quality_fr0_to1);
            return b.asBufferedImage();
        });
    }

    public static byte[] toBytes(BufferedImage image, CharSequence fileType) {
        return TGS_UnSafe.call(() -> {
            try (var baos = new ByteArrayOutputStream()) {
                ImageIO.write(image, fileType.toString(), baos);
                baos.flush();
                return baos.toByteArray();
            }
        });
    }

    public static String toBase64(BufferedImage image, CharSequence fileType) {
        return TGS_CryptUtils.encrypt64(toBytes(image, fileType));
    }

    public static BufferedImage ToImage(CharSequence base64) {
        return TGS_UnSafe.call(() -> toImage(Base64.getDecoder().decode(base64.toString())));
    }

//    public static BufferedImage toImageFromBase64(CharSequence base64) {
//        return TGS_UnSafe.call(() -> {
//            var imgbytes = TGS_CryptUtils.decrypt64_toBytes(base64);
//            return toImage(imgbytes);
//        });
//    }
    public static BufferedImage ToImage(TGS_Url url) {
        return TGS_UnSafe.call(() -> ImageIO.read(URI.create(url.toString()).toURL()));
    }

    public static void toFile(BufferedImage image, Path imgFile, double quality_fr0_to1) {
//        //DEPRECATED: NOT GOOD QUALITY
//            image = TS_ImageUtils.toImageRGB(image);
//            ImageIO.write(image, TS_FileUtils.getNameType(imgFile), imgFile.toFile());
        TGS_UnSafe.run(() -> {
            if (quality_fr0_to1 > 0.99) {
                Thumbnails.of(image).scale(1).toFile(imgFile.toFile());
            } else {
                Thumbnails.of(image).scale(1).outputQuality(quality_fr0_to1).toFile(imgFile.toFile());
            }
        });
    }

    public static BufferedImage createRGB(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public static BufferedImage fill(BufferedImage bi, Color color) {
        var graphics = bi.createGraphics();
        graphics.setPaint(color);
        graphics.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        graphics.dispose();
        return bi;
    }

    public static int[][] createNoiseData(int width, int height) {
        var data = new int[height][width];
        IntStream.range(0, height).parallel().forEach(ci -> {
            data[ci] = TS_RandomUtils.nextIntArray(width, 0, 255);
        });
        return data;
    }
}
