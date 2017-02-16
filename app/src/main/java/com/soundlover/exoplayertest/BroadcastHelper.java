package com.soundlover.exoplayertest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import org.parceler.Parcels;

public class BroadcastHelper {

    public static final String INTENT_PLAYER_STATE_CHANGE = "com.soundlover.playerStateChange";
    public static final String INTENT_PROGRESS_UPDATE = "com.soundlover.progressUpdate";

    public static final String EXTRA_MEDIA = "media";
    public static final String EXTRA_PLAYER_STATE = "playerStateId";
    public static final String EXTRA_PROGRESS = "progress";
    public static final String EXTRA_BUFFERED_PROGRESS = "bufferedProgress";
    public static final String EXTRA_DURATION = "duration";

    public static void broadcastPlayerStateChange(Context context, int playerStateId,
                                                  MediaModel media) {
        Intent intent = new Intent(INTENT_PLAYER_STATE_CHANGE);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_MEDIA, Parcels.wrap(media));
        intent.putExtra(EXTRA_PLAYER_STATE, playerStateId);
        intent.putExtras(bundle);
        sendBroadcast(context, intent);
    }

    public static void broadcastProgressUpdate(Context context, long progress, long bufferedProgress,
                                               long duration, MediaModel media) {
        Intent intent = new Intent(INTENT_PROGRESS_UPDATE);
        intent.putExtra(EXTRA_PROGRESS, progress);
        intent.putExtra(EXTRA_BUFFERED_PROGRESS, bufferedProgress);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_MEDIA, Parcels.wrap(media));
        intent.putExtras(bundle);
        intent.putExtra(EXTRA_DURATION, duration);
        sendBroadcast(context, intent);
    }

    private static void sendBroadcast(Context context, Intent intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void registerReceiver(Context context, BroadcastReceiver receiver, String... actions) {
        for (int i = 0; i < actions.length; i++) {
            LocalBroadcastManager.getInstance(context).registerReceiver(receiver, new IntentFilter(actions[i]));
        }
    }

    public static void unregisterReceiver(Context context, BroadcastReceiver... receivers) {
        for (int i = 0; i < receivers.length; i++) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receivers[i]);
        }
    }
}
