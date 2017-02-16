package com.soundlover.exoplayertest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.soundlover.exoplayertest.seekbar.CircularSeekBar;
import com.soundlover.exoplayertest.seekbar.OnArcSeekChangeListener;
import com.soundlover.exoplayertest.seekbar.OnMultipleTouchListener;
import com.soundlover.exoplayertest.seekbar.WaveView;

import org.parceler.Parcels;

import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnArcSeekChangeListener, OnMultipleTouchListener {

    MediaModel[] mTracks = {
            new MediaModel("http://www.stephaniequinn.com/Music/Commercial%20DEMO%20-%2005.mp3"),
            new MediaModel("http://www.stephaniequinn.com/Music/Commercial%20DEMO%20-%2001.mp3"),
            new MediaModel("http://www.stephaniequinn.com/Music/Commercial%20DEMO%20-%2016.mp3"),
            new MediaModel("http://www.stephaniequinn.com/Music/Commercial%20DEMO%20-%2007.mp3")
    };

    @BindView(R.id.circularSeekBar)
    CircularSeekBar mCircularSeekBar;

    @BindView(R.id.wave)
    WaveView mWaveView;

    private MediaModel mMediaModel;
    private boolean seekTouched;

    private BroadcastReceiver mPlayerStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BroadcastHelper.EXTRA_PLAYER_STATE, -1);
            MediaModel media = Parcels.unwrap(intent.getParcelableExtra(BroadcastHelper.EXTRA_MEDIA));

            if (state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_PAUSED) {
                if (media != null) {
                    mMediaModel = media;
                    Log.e("activity", "onReceive: url" + mMediaModel.getUrl());
//                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private BroadcastReceiver mProgressUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!seekTouched) {
                long progress = intent.getLongExtra(BroadcastHelper.EXTRA_PROGRESS, 0L);
                long bufferedProgress = intent.getLongExtra(BroadcastHelper.EXTRA_BUFFERED_PROGRESS, 0L);
                long duration = intent.getLongExtra(BroadcastHelper.EXTRA_DURATION, 0L);
                MediaModel media = Parcels.unwrap(intent.getParcelableExtra(BroadcastHelper.EXTRA_MEDIA));
                if (media != null) {
                    mMediaModel = media;
                }
                if (mCircularSeekBar != null && mWaveView != null) {
                    mCircularSeekBar.setMax((int) duration);
                    mWaveView.setMax((int) duration);
                    mCircularSeekBar.setProgress((float) progress);
                }
            }
        }
    };
    private Playlist playlist;

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mProgressUpdateReceiver,
                new IntentFilter(BroadcastHelper.INTENT_PROGRESS_UPDATE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mPlayerStateChangeReceiver,
                new IntentFilter(BroadcastHelper.INTENT_PLAYER_STATE_CHANGE));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mPlayerStateChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mProgressUpdateReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mCircularSeekBar.setOnSeekArcChangeListener(this);
        mCircularSeekBar.setOnMultipleTouchListener(this);
        mWaveView.setOnMultipleTouchListener(this);

        // TEST
        LinkedList<MediaModel> list = new LinkedList<>();
        list.add(new MediaModel(mTracks[0]));
        list.add(new MediaModel(mTracks[1]));
        list.add(new MediaModel(mTracks[2]));
        playlist = new Playlist(-1, list);
    }

    @OnClick(R.id.prev)
    public void onPrevClick() {
        MediaPlayerService.sendIntent(this, MediaPlayerService.ACTION_PREVIOUS);

    }

    @OnClick(R.id.play)
    public void onPlayClick() {
        MediaPlayerService.sendIntent(this, MediaPlayerService.ACTION_PLAY_QUEUE,
                playlist);
    }

    @OnClick(R.id.next)
    public void onNextClick() {
        MediaPlayerService.sendIntent(this, MediaPlayerService.ACTION_NEXT);
    }

    @Override
    public void onProgressChanged(View seek, boolean fromArc, float progress, boolean fromUser) {
        if (fromUser) {
            seekTouched = true;
            if (mMediaModel != null) {
                long prog = (long) progress;
                Intent intent = new Intent(this, MediaPlayerService.class);
                intent.setAction(MediaPlayerService.ACTION_SEEK_TO);
                Bundle bundle = new Bundle();
                bundle.putLong(MediaPlayerService.PARAM_SEEK_MS, prog);
                intent.putExtras(bundle);
                startService(intent);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(View seek) {
        seekTouched = true;
        MediaPlayerService.sendIntent(this, MediaPlayerService.ACTION_STOP_UPDATE_PLAYER);
    }

    @Override
    public void onStopTrackingTouch(View seek) {
        MediaPlayerService.sendIntent(this, MediaPlayerService.ACTION_START_UPDATE_PLAYER);
        seekTouched = false;
    }

    @Override
    public void onProgressTouched(ViewType type, float progress) {
        if (type.equals(ViewType.SEEK)) {
            mWaveView.setTouchEvent(progress);
        } else {
            mCircularSeekBar.setTouchEvent(progress);
        }
    }
}
