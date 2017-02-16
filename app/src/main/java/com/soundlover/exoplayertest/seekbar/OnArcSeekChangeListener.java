package com.soundlover.exoplayertest.seekbar;

import android.view.View;

/**
 * Created by Sergiy on 25.01.2017.
 */

public interface OnArcSeekChangeListener {

    void onProgressChanged(View seek, boolean fromArc, float progress, boolean fromUser);

    void onStartTrackingTouch(View seek);

    void onStopTrackingTouch(View seek);

}
