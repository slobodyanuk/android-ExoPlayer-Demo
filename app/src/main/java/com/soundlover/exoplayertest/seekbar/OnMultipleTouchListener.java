package com.soundlover.exoplayertest.seekbar;

/**
 * Created by Sergiy on 27.01.2017.
 */

public interface OnMultipleTouchListener {

    void onProgressTouched(ViewType type, float progress);

    public enum ViewType{
        WAVE, SEEK;
    }
}
