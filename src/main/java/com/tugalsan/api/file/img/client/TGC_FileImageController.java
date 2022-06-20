package com.tugalsan.api.file.img.client;

import com.google.gwt.user.client.ui.*;
import com.tugalsan.api.list.client.*;
import com.tugalsan.api.thread.client.*;
import java.util.*;

public class TGC_FileImageController {

    private final TGC_ThreadUtils.TGC_Thread ms_timer;
    private final TGC_FileImageWidgets base;
    private boolean continuesAnimationEnabled = false;
    public List<String> activeAnimatorList = TGS_ListUtils.of();

    public Image[] getWidgets() {
        return base.wi;
    }

    public boolean isContinuesAnimationEnabled() {
        return continuesAnimationEnabled;
    }

    public void animateContinous(CharSequence sourceAnimatorTag, boolean enable) {
        var sourceAnimatorTagStr = sourceAnimatorTag.toString();
        this.continuesAnimationEnabled = enable;
        if (enable) {
            if (!activeAnimatorList.contains(sourceAnimatorTagStr)) {
                activeAnimatorList.add(sourceAnimatorTagStr);
            }
            animateNext();
        } else {
            activeAnimatorList.remove(sourceAnimatorTagStr);
            animateFin();
        }
    }

    public void animateNext() {
        base.wi_animate();
        if (!animateNextScheduled) {
            animateNextScheduled = true;
            ms_timer.execute_afterSeconds(1);
        }
    }
    boolean animateNextScheduled = false;

    public void animateFin() {
        base.wi_sleep();
    }

    public TGC_FileImageController(TGC_FileImageWidgets base) {
        this.base = base;
        ms_timer = TGC_ThreadUtils.create_afterGUIUpdate(t -> {
            animateNextScheduled = false;
            if (continuesAnimationEnabled) {
                animateNext();
            } else {
                animateFin();
            }
        });
    }

}
