package com.soundlover.exoplayertest;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import org.parceler.Parcels;

import static android.content.ContentValues.TAG;

/**
 * Created by Sergiy on 07.02.2017.
 */

public class MediaPlayerService extends Service implements AudioManager.OnAudioFocusChangeListener,
        MediaPlayerStateListener, ProgressUpdateListener {

    public static final String PARAM_PLAYLIST = "playlist";
    public static final String PARAM_MEDIA_ID = "mediaId";
    public static final String PARAM_SEEK_MS = "seekMs";
    public static final String PARAM_MEDIA = "media";


    public static final String ACTION_PLAY_QUEUE = "com.soundlover.playQueue";
    public static final String ACTION_PLAY_MEDIA = "com.soundlover.playNew";
    public static final String ACTION_RESUME_PLAYBACK = "com.soundlover.play";
    public static final String ACTION_PAUSE = "com.soundlover.pause";
    public static final String ACTION_NEXT = "com.soundlover.next";
    public static final String ACTION_PREVIOUS = "com.soundlover.previous";
    public static final String ACTION_SEEK_TO = "com.soundlover.seekTo";
    public static final String ACTION_STOP_SERVICE = "com.soundlover.stopService";
    public static final String ACTION_STOP_UPDATE_PLAYER = "com.soundlover.stopUpdatePlayer";
    public static final String ACTION_START_UPDATE_PLAYER = "com.soundlover.startUpdatePlayer";
//    public static final String ACTION_UPDATE_WIDGET = "com.mainmethod.premofm.updateWidget";

    private static final float AUDIO_DUCK = 0.5f;

    private final IBinder mBinder = new ServiceBinder();

    private MediaNotificationManager mMediaNotificationManager;

    private int mStreamVolume;
    private int mMediaPlayerState;

    private boolean mReceiverRegistered;
    private boolean mServiceBound;
    private boolean mPlayingBeforeFocusChange;

    private WifiManager.WifiLock mWifiLock;

    private AudioManager mAudioManager;
    private ExoMediaPlayer mMediaPlayer;
    private HeadsetReceiver mHeadsetReceiver;
    private MediaModel mCurrentMedia;
    private PowerManager.WakeLock mWakeLock;

    private Playlist mPlaylist;

    public static void sendIntent(Context context, String action, MediaModel media) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(action);

        if (media != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(PARAM_MEDIA, Parcels.wrap(media));
            intent.putExtras(bundle);
        }
        context.startService(intent);
    }

    public static void sendIntent(Context context, String action, Playlist playlist) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(action);

        if (playlist != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(PARAM_PLAYLIST, Parcels.wrap(playlist));
            intent.putExtras(bundle);
        }
        context.startService(intent);
    }

    public static void sendIntent(Context context, String action) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Service", "onCreate");
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMediaNotificationManager = new MediaNotificationManager(this);

        mMediaPlayerState = MediaPlayerState.STATE_IDLE;

        mHeadsetReceiver = new HeadsetReceiver();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "wakelock");
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "uAmp_lock");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mMediaPlayerState == MediaPlayerState.STATE_IDLE) {
            stopSelf();
        }
        mServiceBound = false;
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        mServiceBound = true;
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            Log.d("Service", "OnStartCommand intent action: %s" + action);

            MediaModel media = null;
            if (Parcels.unwrap(intent.getParcelableExtra(PARAM_MEDIA)) != null) {
                media = Parcels.unwrap(intent.getParcelableExtra(PARAM_MEDIA));
            }

            switch (action) {
                case ACTION_PLAY_QUEUE:
                    mPlaylist = Parcels.unwrap(intent.getParcelableExtra(PARAM_PLAYLIST));
                    playPlaylist();
                    break;
                case ACTION_NEXT:
                    playNext();
                    break;
                case ACTION_PREVIOUS:
                    playPrevious();
                    break;
                case ACTION_PLAY_MEDIA:
                    if (media != null) {
                        play(media, true);
                    }
                    break;
                case ACTION_RESUME_PLAYBACK:
                    if (mMediaPlayerState != MediaPlayerState.STATE_PLAYING) {
                        if (mCurrentMedia != null && media != null) {
                            play(media, true);
                        }
                    }
                    break;
                case ACTION_PAUSE:
                    pause();
                    break;
                case ACTION_STOP_UPDATE_PLAYER:
                    if (mMediaPlayer != null) {
                        mMediaPlayer.stopProgressUpdater();
                    }
                    break;
                case ACTION_START_UPDATE_PLAYER:
                    if (mMediaPlayer != null) {
                        mMediaPlayer.startProgressUpdater();
                    }
                    break;
                case ACTION_SEEK_TO:
                    long seekMs = intent.getLongExtra(PARAM_SEEK_MS, 20);
                    seekTo(seekMs);
                    break;
                case ACTION_STOP_SERVICE:
                    stopPlayback();
                    endPlayback(true);
                    if (!mServiceBound) {
                        stopSelf();
                    }
                    break;
               /* case ACTION_UPDATE_WIDGET:
                    if (mMediaPlayerState == MediaPlayerState.STATE_PLAYING ||
                            mMediaPlayerState == MediaPlayerState.STATE_PAUSED) {
                        updateWidget();
                    }
                    break;*/
            }
        }
        return START_REDELIVER_INTENT;
    }

    private void playPlaylist() {
        if (mPlaylist != null){
            if (mPlaylist.next()) {
                MediaModel track = mPlaylist.getTracks().get(mPlaylist.getCurrentIndex());
                play(track, true);
            }
        }
    }

    private void playNext() {
        if (mPlaylist != null){
            if (mPlaylist.next()) {
                MediaModel track = mPlaylist.getTracks().get(mPlaylist.getCurrentIndex());
                seekPlayerTo(0);
                play(track, true);
            }
        }
    }

    private void playPrevious() {
        if (mPlaylist != null){
            if (mPlaylist.previous()) {
                MediaModel track = mPlaylist.getTracks().get(mPlaylist.getCurrentIndex());
                seekPlayerTo(0);
                play(track, true);
            }
        }
    }

    private void play(MediaModel media, boolean playImmediately) {

        if (mMediaPlayer == null) {
            mMediaPlayer = new ExoMediaPlayer(this, this, this);
        }

        switch (mMediaPlayerState) {
            case MediaPlayerState.STATE_CONNECTING:
            case MediaPlayerState.STATE_PLAYING:
            case MediaPlayerState.STATE_PAUSED:

                if (media != null) {
                    endPlayback(false);
                    startPlayback(media, playImmediately);
                } else if (mMediaPlayerState == MediaPlayerState.STATE_PAUSED) {
                    mMediaPlayer.resumePlayback();
                } else {
                    Log.w("Service", "Player is playing, episode cannot be null");
                }
                break;
            case MediaPlayerState.STATE_ENDED:
            case MediaPlayerState.STATE_IDLE:
                // stopped or uninitialized, so we need to start from scratch
                if (media != null) {
                    startPlayback(media, playImmediately);
                } else {
                    Log.w("Service", "Player is stopped/uninitialized, episode cannot be null");
                }
                break;
            default:
                Log.w("Service", "Trying to play an episode, but player is in state: %s" + mMediaPlayerState);
                break;
        }
    }

    private void pause() {
        switch (mMediaPlayerState) {
            case MediaPlayerState.STATE_PLAYING:
                pausePlayback();
                break;
            case MediaPlayerState.STATE_PAUSED:
                if (mMediaPlayer != null) mMediaPlayer.resumePlayback();
                break;
            default:
                Log.w("Service", "Trying to pause an episode, but player is in state: %s" + mMediaPlayerState);
                break;
        }
    }

    private void startPlayback(MediaModel media, boolean playImmediately) {
        // request audio focus
        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentMedia = media;
            registerReceivers();
            acquireWifiLock();
            mMediaPlayer.loadMedia(mCurrentMedia);
            mMediaPlayer.startPlayback(playImmediately);
            mMediaNotificationManager.startNotification(mCurrentMedia, "http://www.gxdmtl.com/pics/main/44/363830-note.jpg");
        } else {
            Log.e("Service", "Audiofocus not granted, result code: %d" + result);
        }
    }

    private void stopPlayback() {
        if (mMediaPlayer != null)
            mMediaPlayer.stopPlayback();
    }

    private void pausePlayback() {
        mMediaPlayer.pausePlayback();
    }

    private void endPlayback(boolean cancelNotification) {
        updateMedia(MediaPlayerState.STATE_IDLE);
        unregisterReceivers();
        Log.e(TAG, "endPlayback: end");

        if (cancelNotification) {
            mMediaNotificationManager.stopNotification();
        }
        mAudioManager.abandonAudioFocus(this);
        releaseWifiLock();
    }

    private void seekPlayerTo(long seekTo) {
        if (seekTo < 0) {
            seekTo = 0;
        } else if (seekTo > mMediaPlayer.getDuration()) {
            seekTo = mMediaPlayer.getDuration();
        }
        mMediaPlayer.seekTo(seekTo);
    }

    private void seekTo(long seekTo) {
        switch (mMediaPlayerState) {
            case MediaPlayerState.STATE_PAUSED:
            case MediaPlayerState.STATE_CONNECTING:
            case MediaPlayerState.STATE_PLAYING:
                seekPlayerTo(seekTo);
                break;
            case MediaPlayerState.STATE_ENDED:
                if (mMediaPlayer != null){
                    mMediaPlayer.resumePlayback();
                    seekPlayerTo(seekTo);
                }
            default:
                Log.w("Service", "Trying to play an media, but player is in state: %s" + mMediaPlayerState);
                break;
        }
    }

    private void updateMedia(int state) {
        if (mMediaPlayer == null || mCurrentMedia == null) {
            return;
        }

        switch (state) {
            case MediaPlayerState.STATE_PLAYING:
                mCurrentMedia.setStatus(MediaStatus.IN_PROGRESS);
                break;
            case MediaPlayerState.STATE_PAUSED:
            case MediaPlayerState.STATE_IDLE:
                mCurrentMedia.setStatus(MediaStatus.PLAYED);
                mCurrentMedia.setProgress(mMediaPlayer.getCurrentPosition());
                break;
            default:
                throw new IllegalArgumentException(
                        "Incorrect state for showing play pause notification");
        }
    }

    @Override
    public void onProgressUpdate(long progress, long bufferedProgress, long left) {
        BroadcastHelper.broadcastProgressUpdate(this, progress, bufferedProgress, left, mCurrentMedia);
    }

    @Override
    public void onStateChanged(int state) {
        mMediaPlayerState = state;

        switch (state) {
            case MediaPlayerState.STATE_CONNECTING:
                break;
            case MediaPlayerState.STATE_ENDED:
//                endPlayback(false);
                playPlaylist();
                break;
            case MediaPlayerState.STATE_IDLE:
                updateMedia(state);
                break;
            case MediaPlayerState.STATE_PLAYING:
                updateMedia(state);
                mMediaNotificationManager.notifyForeground(MediaNotificationManager.NOTIFICATION_IMAGE_PAUSE);
                break;
            case MediaPlayerState.STATE_PAUSED:
                updateMedia(state);
                mMediaNotificationManager.notifyForeground(MediaNotificationManager.NOTIFICATION_IMAGE_PLAY);
                break;
        }
//        updateWidget();
        BroadcastHelper.broadcastPlayerStateChange(this, mMediaPlayerState, mCurrentMedia);
    }

    public int getMediaPlayerState() {
        return mMediaPlayerState;
    }

    public boolean isStreaming() {
        return mMediaPlayer != null && mMediaPlayer.isStreaming();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        (int) (mStreamVolume * AUDIO_DUCK), 0);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // record the playing before focus change value
                mPlayingBeforeFocusChange = mMediaPlayerState == MediaPlayerState.STATE_PLAYING;
                pause();
                break;
            case AudioManager.AUDIOFOCUS_GAIN:

                if (mStreamVolume > -1) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0);
                    mStreamVolume = -1;
                }

                if (mPlayingBeforeFocusChange) {
                    mPlayingBeforeFocusChange = false;
                    play(null, true);
                }
                break;
        }
    }

    private void destroyMediaPlayer() {
        if (mMediaPlayer == null) {
            return;
        }
        endPlayback(true);
        mMediaPlayerState = MediaPlayerState.STATE_IDLE;
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    private void acquireWifiLock() {
        if (mWifiLock != null && !mWifiLock.isHeld() && mMediaPlayer.isStreaming()) {
            mWifiLock.acquire();
            if (mWakeLock != null) mWakeLock.acquire();
        }
    }

    private void releaseWifiLock() {
        if (mWifiLock != null && mWifiLock.isHeld()) {
            mWifiLock.release();
            if (mWakeLock != null) mWakeLock.release();
        }
    }

    private void registerReceivers() {
        if (!mReceiverRegistered) {
            registerReceiver(mHeadsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
            registerReceiver(mHeadsetReceiver, new IntentFilter(
                    AudioManager.ACTION_AUDIO_BECOMING_NOISY));
            mReceiverRegistered = true;
        }
    }

    private void unregisterReceivers() {
        if (mReceiverRegistered) {
            unregisterReceiver(mHeadsetReceiver);
            mReceiverRegistered = false;
        }
    }

    @Override
    public void onDestroy() {
        destroyMediaPlayer();
        mMediaNotificationManager.stopNotification();
        releaseWifiLock();
        super.onDestroy();
    }

    public class ServiceBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    private class HeadsetReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (isInitialStickyBroadcast()) {
                return;
            }

            String action = intent.getAction();

            switch (action) {
                case Intent.ACTION_HEADSET_PLUG:
                    int state = intent.getIntExtra("state", -1);
                    switch (state) {
                        case 0:
                            pause();
                            break;
                    }
                    break;
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    pause();
                    break;
            }
        }
    }
}
