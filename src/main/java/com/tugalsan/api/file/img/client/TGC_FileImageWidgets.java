package com.tugalsan.api.file.img.client;

import com.google.gwt.user.client.ui.*;
import java.util.stream.*;
import com.tugalsan.api.log.client.*;
import com.tugalsan.api.string.client.*;

public class TGC_FileImageWidgets {

    final private static TGC_Log d = TGC_Log.of(TGC_FileImageWidgets.class);

    public TGC_FileImageWidgets(CharSequence mediaBase) {
        wi = new Image[5];
        IntStream.range(0, wi.length).parallel().forEach(i -> {
            wi[i] = new Image(TGS_StringUtils.cmn().concat(mediaBase, "wi_", String.valueOf(i), ".jpg"));
            d.ci(TGS_StringUtils.cmn().concat("wi[", String.valueOf(i), "].getUrl()"), wi[i].getUrl());
        });
        wi_repaint_idx = 2;
        wi_repaint();
    }
    final public Image[] wi;
    private int wi_repaint_idx;

    public void wi_animate() {
        wi_repaint_idx++;
        if (wi_repaint_idx >= wi.length) {
            wi_repaint_idx = 1;
        }
        wi_repaint();
    }

    public void wi_sleep() {
        wi_repaint_idx = 0;
        wi_repaint();
    }

    private void wi_repaint() {
        IntStream.range(0, wi.length).parallel().forEach(i -> wi[i].setVisible(i == wi_repaint_idx));
    }

}
