package com.tugalsan.api.file.img.server;

import com.tianscar.imageio.plugins.png.PNGImageWriteParam;
import com.tugalsan.api.function.client.maythrowexceptions.checked.TGS_FuncMTCUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

//REF: https://github.com/Tianscar/imageio-apng/tree/main/src/test/java/com/tianscar/imageio/plugins/png/test
public class TS_FileImageAPNGUtils {

    public void merge(Path apng, BufferedImage... frames) {
        TGS_FuncMTCUtils.run(() -> {
            try (var os = ImageIO.createImageOutputStream(apng.toFile())) {
                var nativeMetadataFormatName = "javax_imageio_png_1.0"; // see PNGMetadata.java
                var writer = ImageIO.getImageWritersByFormatName("apng").next(); // get library's apng writer
                // writer = ImageIO.getImageWritersByMIMEType("image/apng").next();      // by mime type
                // writer = ImageIO.getImageWritersBySuffix("apng").next();              // by suffix
                writer.setOutput(os); // set the output stream

                var param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);    // enable compression control
                param.setCompressionType("Deflate");                        // only `Deflate` supported
                param.setCompressionQuality(1.0f);                          // 1.0 - Best quality
                if (param instanceof PNGImageWriteParam p) {
                    p.setAnimContainsIDAT(false);// true if the animation contains `IDAT` chunk, false otherwise, default true
                }

                writer.prepareWriteSequence(null);

                // more metadata information see https://wiki.mozilla.org/APNG_Specification and http://www.w3.org/TR/PNG/
                // You can also create PNGMetadata directly and use the constants in PNG.java!
                var metadata = writer.getDefaultImageMetadata(
                        ImageTypeSpecifier.createFromBufferedImageType(frames[0].getType()), // cannot be null
                        param // can be null
                ); // since PNG don't have a stream metadata, the first image metadata send to the writer will be applied to the whole image
                var treeToMerge = new IIOMetadataNode(nativeMetadataFormatName);
                // create the metadata tree to merge, the native tree, as standard tree not fully supported
                var acTL = new IIOMetadataNode("acTL");
                acTL.setAttribute("num_plays", "0");                            // infinite play
                acTL.setAttribute("num_frames", Integer.toString(frames.length));     // must be set before write frames
                treeToMerge.appendChild(acTL);
                metadata.mergeTree(nativeMetadataFormatName, treeToMerge);                  // merge tree

                writer.writeToSequence(new IIOImage(frames[0], null, metadata), param);
                // since the animation do not contain `IDAT` chunk, write `IDAT` separately

                for (var f : frames) {
                    metadata = writer.getDefaultImageMetadata(
                            ImageTypeSpecifier.createFromBufferedImageType(f.getType()), // cannot be null
                            param // can be null
                    );
                    treeToMerge = (IIOMetadataNode) metadata.getAsTree(nativeMetadataFormatName);
                    var fcTL = new IIOMetadataNode("fcTL");
                    fcTL.setAttribute("sequence_number", "0"); // the value doesn't matter, will automatically set by the writer later
                    fcTL.setAttribute("width", Integer.toString(f.getWidth()));
                    fcTL.setAttribute("height", Integer.toString(f.getHeight()));
                    fcTL.setAttribute("x_offset", "0");        // the x offset to the origin
                    fcTL.setAttribute("y_offset", "0");        // the y offset to the origin
                    // the delay time is in seconds
                    fcTL.setAttribute("delay_num", "2");       // the numerator of the delay time
                    fcTL.setAttribute("delay_den", "1");       // the denominator of the delay time
                    // 2s / 1 = 2s
                    fcTL.setAttribute("dispose_op", "background"); // see PNGMetadata#fcTL_disposalOperatorNames
                    fcTL.setAttribute("blend_op", "source");       // see PNGMetadata#fcTL_blendOperatorNames
                    treeToMerge.appendChild(fcTL);
                    metadata.mergeTree(nativeMetadataFormatName, treeToMerge);
                    writer.writeToSequence(new IIOImage(f, null, metadata), param);
                }
                writer.endWriteSequence(); // end write sequence
                writer.dispose();          // release the resources
                os.flush();
            }
        });
    }

    public List<BufferedImage> extract(Path apng) {
        return TGS_FuncMTCUtils.call(() -> {
            var nativeMetadataFormatName = "javax_imageio_png_1.0"; // see PNGMetadata.java
            var is = ImageIO.createImageInputStream(Files.newInputStream(apng));

            ImageReader reader = null;
            try {
                reader = ImageIO.getImageReadersByFormatName("apng").next(); // get library's apng reader

                //reader = ImageIO.getImageReadersByMIMEType("image/apng").next();       // by mime type
                //reader = ImageIO.getImageReadersBySuffix("apng").next();               // by suffix
                reader.setInput(is); // set the input stream

                // more metadata information see https://wiki.mozilla.org/APNG_Specification and http://www.w3.org/TR/PNG/
                // You can also cast PNGMetadata directly!
                IIOMetadata metadata;
                for (var i = 0; i < reader.getNumImages(true); i++) {
                    metadata = reader.getImageMetadata(i);
                    var child = (IIOMetadataNode) metadata.getAsTree(nativeMetadataFormatName).getFirstChild();
                    while (child != null) {
                        if ("fcTL".equals(child.getNodeName())) {
                            System.out.println("frame index: " + i);
                            System.out.println("width: " + child.getAttribute("width"));
                            System.out.println("height: " + child.getAttribute("height"));
                            System.out.println("x offset: " + child.getAttribute("x_offset"));
                            System.out.println("y offset: " + child.getAttribute("y_offset"));
                            System.out.println("disposal operator: " + child.getAttribute("dispose_op"));
                            System.out.println("blend operator: " + child.getAttribute("blend_op"));
                            System.out.println("delay time (seconds): "
                                    + (Integer.parseInt(child.getAttribute("delay_num"))
                                    / Integer.parseInt(child.getAttribute("delay_den"))));
                            break;
                        }
                        child = (IIOMetadataNode) child.getNextSibling();
                    }
                    ImageIO.write(reader.read(i), "png", new File("shield_" + (i * 90) + ".png"));
                }
            } finally {
                if (reader != null) {
                    reader.dispose();
                }
            }
            return null;
        });
    }
}
