package com.soundlover.exoplayertest;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;

import java.io.File;

/**
 * Created by Sergiy on 06.02.2017.
 */

public class ExoMediaPlayer extends MediaPlayer
        implements ExoPlayer.EventListener {

    private static final String TAG = ExoMediaPlayer.class.getSimpleName();
    private static final String MEDIA_CACHE_DIR = "media-cache";

    private final Context mContext;
    private final SimpleExoPlayer mExoPlayer;
    private boolean mIsStreaming;
    private int mMediaPlayerState;
    private boolean isUpdatingProgress;
    private Runnable mProgressUpdater;

    private MediaModel mMedia;
    private Handler mHandler;


    public ExoMediaPlayer(Context context, ProgressUpdateListener progressUpdateListener, MediaPlayerStateListener mediaPlayerListener) {
        super(progressUpdateListener, mediaPlayerListener);

        mContext = context;
        mHandler = new Handler();
        TrackSelector trackSelector = new DefaultTrackSelector();
        DefaultLoadControl loadControl = new DefaultLoadControl();
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector, loadControl);
        mExoPlayer.addListener(this);
        mMediaPlayerState = MediaPlayerState.STATE_IDLE;
        mProgressUpdater = new ProgressUpdater();
    }

    @Override
    public void loadMedia(MediaModel media) {
        mMedia = media;
    }

    @Override
    public void startPlayback(boolean playImmediately) {
        if (mMedia.getProgress() > -1) {
            mExoPlayer.seekTo(mMedia.getProgress());
        } else {
            mExoPlayer.seekTo(0);
        }
        MediaSource mMediaSource = buildMediaSource();
        mExoPlayer.prepare(mMediaSource, false, false);
        mExoPlayer.setPlayWhenReady(playImmediately);
    }

    @Override
    public void resumePlayback() {
        mExoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pausePlayback() {
        mExoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void stopPlayback() {
        mExoPlayer.stop();
        mIsStreaming = false;
        mMedia = null;
    }

    @Override
    public void seekTo(long position) {
        mExoPlayer.seekTo(position);
    }

    @Override
    public boolean isStreaming() {
        return mIsStreaming;
    }

    @Override
    public int getState() {
        return mMediaPlayerState;
    }

    @Override
    public long getCurrentPosition() {
        return mExoPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return mExoPlayer.getDuration();
    }

    @Override
    public long getBufferedPosition() {
        return mExoPlayer.getBufferedPosition();
    }

    @Override
    public void release() {
        super.release();
        stopProgressUpdater();
        mExoPlayer.release();
        mExoPlayer.removeListener(this);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        String playbackStateStr;

        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                mMediaPlayerState = MediaPlayerState.STATE_CONNECTING;
                playbackStateStr = "Buffering";
                break;
            case ExoPlayer.STATE_ENDED:
                mMediaPlayerState = MediaPlayerState.STATE_ENDED;
                playbackStateStr = "Ended";
                break;
            case ExoPlayer.STATE_IDLE:
                mMediaPlayerState = MediaPlayerState.STATE_IDLE;
                playbackStateStr = "Idle";
                break;
            case ExoPlayer.STATE_READY:
                mMediaPlayerState = playWhenReady ? MediaPlayerState.STATE_PLAYING :
                        MediaPlayerState.STATE_PAUSED;
                playbackStateStr = "Ready";

                if (playWhenReady) {
                    startProgressUpdater();
                } else {
                    stopProgressUpdater();
                }
                Log.e(TAG, "onPlayerStateChanged: " + isUpdatingProgress );
                break;
            default:
                mMediaPlayerState = MediaPlayerState.STATE_IDLE;
                playbackStateStr = "Unknown";
                break;
        }
        mMediaPlayerListener.onStateChanged(mMediaPlayerState);
        Log.d(TAG, String.format("ExoPlayer state changed: %s, Play When Ready: %s",
                playbackStateStr,
                String.valueOf(playWhenReady)));
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.w(TAG, "Player error encountered", error);
        stopPlayback();
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    public void startProgressUpdater() {
        if (!isUpdatingProgress) {
            mProgressUpdater.run();
            isUpdatingProgress = true;
        }
    }

    public void stopProgressUpdater() {
        if (isUpdatingProgress) {
            mHandler.removeCallbacks(mProgressUpdater);
            isUpdatingProgress = false;
        }
    }

    private MediaSource buildMediaSource() {
        DataSource.Factory dataSourceFactory = null;
        Uri uri = null;
        uri = Uri.parse(mMedia.getUrl());
        dataSourceFactory = PathUtils.getCacheDataSource(
                new File(mContext.getCacheDir(), MEDIA_CACHE_DIR),
                mContext.getString(R.string.user_agent));

        if (uri != null) {
            return new ExtractorMediaSource(uri, dataSourceFactory, new AudioExtractorFactory(),
                    mHandler, null);
        }

        mIsStreaming = true;
        throw new IllegalStateException("Unable to build media source");
    }

    private class ProgressUpdater implements Runnable {

        private static final int TIME_UPDATE_MS = 150;

        @Override
        public void run() {
                mProgressUpdateListener.onProgressUpdate(mExoPlayer.getCurrentPosition(),
                        isStreaming() ? mExoPlayer.getBufferedPosition() : mExoPlayer.getDuration(),
                        mExoPlayer.getDuration());
                mHandler.postDelayed(mProgressUpdater, TIME_UPDATE_MS);
        }
    }
}
