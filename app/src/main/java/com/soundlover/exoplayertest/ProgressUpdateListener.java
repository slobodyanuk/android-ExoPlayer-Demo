package com.soundlover.exoplayertest;

/**
 * Created by Sergiy on 03.02.2017.
 */
public interface ProgressUpdateListener {

    void onProgressUpdate(long progress, long bufferedProgress, long duration);

}
