package com.tugalsan.api.file.img.client;

import com.tugalsan.api.gui.client.widget.canvas.*;
import com.google.gwt.user.client.ui.*;
import com.tugalsan.api.shape.client.*;

public class TGC_FileImageFilterUtils {

    public static String getGrayScaleURL(Image backgroundImage) {
        var canvas = TGC_CanvasUtils.toCanvas(backgroundImage);
        var imageData = TGC_Canvas2DImageUtils.toImageData(canvas);
        TGC_Canvas2DImageUtils.filterGrayScale(imageData);
        TGC_Canvas2DPaintImageUtils.paint(canvas, imageData, new TGS_ShapeLocation(0, 0));
        return TGC_CanvasUtils.toUrl(canvas);
    }
}
