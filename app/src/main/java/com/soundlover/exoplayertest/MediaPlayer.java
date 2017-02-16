package com.soundlover.exoplayertest;

/**
 * Created by Sergiy on 03.02.2017.
 */

public abstract class MediaPlayer {

    protected ProgressUpdateListener mProgressUpdateListener;
    protected MediaPlayerStateListener mMediaPlayerListener;

    public MediaPlayer(ProgressUpdateListener progressUpdateListener,
                       MediaPlayerStateListener mediaPlayerListener) {
        mProgressUpdateListener = progressUpdateListener;
        mMediaPlayerListener = mediaPlayerListener;
    }

    public abstract void startPlayback(boolean playImmediately);

    public abstract void resumePlayback();

    public abstract void loadMedia(MediaModel media);

    public abstract void pausePlayback();

    public abstract void stopPlayback();

    public abstract void seekTo(long position);

    public abstract boolean isStreaming();

    public abstract int getState();

    public abstract long getCurrentPosition();

    public abstract long getDuration();

    public abstract long getBufferedPosition();

    public void release(){
        mProgressUpdateListener = null;
        mMediaPlayerListener = null;
    }
}
