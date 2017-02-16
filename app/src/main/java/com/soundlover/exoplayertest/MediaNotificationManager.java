package com.soundlover.exoplayertest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;

import static com.soundlover.exoplayertest.MediaPlayerService.ACTION_NEXT;
import static com.soundlover.exoplayertest.MediaPlayerService.ACTION_PAUSE;
import static com.soundlover.exoplayertest.MediaPlayerService.ACTION_PREVIOUS;
import static com.soundlover.exoplayertest.MediaPlayerService.ACTION_STOP_SERVICE;

/**
 * Created by Sergiy on 07.02.2017.
 */

public class MediaNotificationManager {

    public static final int NOTIFICATION_ID_PLAYER = 555;

    public static int NOTIFICATION_IMAGE_PLAY = android.R.drawable.ic_media_play;
    public static int NOTIFICATION_IMAGE_PAUSE = android.R.drawable.ic_media_pause;

    private final NotificationManager mNotificationManager;
    private final MediaPlayerService playerService;
    private NotificationCompat.Builder mNotificationBuilder;
    private Notification mNotification;
    private RemoteViews bigNotificationView;
    private RemoteViews smallNotificationView;


    public MediaNotificationManager(MediaPlayerService playService) {
        playerService = playService;
        mNotificationManager = (NotificationManager) playService.getSystemService(
                Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID_PLAYER);
    }

    public void startNotification(MediaModel media, String channelArt) {
        Notification notification = setUpAsForeground(media, channelArt);

        if (notification != null) {
            playerService.startForeground(NOTIFICATION_ID_PLAYER,
                    notification);
            notifyForeground(NOTIFICATION_IMAGE_PAUSE);
        }
    }

    public void stopNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID_PLAYER);
        playerService.stopForeground(true);
    }

    private Notification setUpAsForeground(MediaModel media, String url) {

        bigNotificationView = new RemoteViews(playerService.getPackageName(),
                R.layout.player_status_bar_expanded);
        smallNotificationView = new RemoteViews(playerService.getPackageName(),
                R.layout.player_status_bar_small);

        Intent closeIntent = new Intent(playerService.getApplicationContext(),
                MediaPlayerService.class);
        closeIntent.setAction(ACTION_STOP_SERVICE);
        Intent pauseIntent = new Intent(playerService.getApplicationContext(),
                MediaPlayerService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        Intent nextIntent = new Intent(playerService.getApplicationContext(),
                MediaPlayerService.class);
        nextIntent.setAction(ACTION_NEXT);
        Intent previousIntent = new Intent(playerService.getApplicationContext(),
                MediaPlayerService.class);
        previousIntent.setAction(ACTION_PREVIOUS);

        PendingIntent pendingCloseIntent = PendingIntent.getService(playerService.getApplicationContext(), 0,
                closeIntent, 0);
        PendingIntent pendingPauseIntent = PendingIntent.getService(playerService.getApplicationContext(), 0,
                pauseIntent, 0);
        PendingIntent pendingNextIntent = PendingIntent.getService(playerService.getApplicationContext(), 0,
                nextIntent, 0);
        PendingIntent pendingPreviousIntent = PendingIntent.getService(playerService.getApplicationContext(), 0,
                previousIntent, 0);

        bigNotificationView.setOnClickPendingIntent(R.id.status_bar_collapse, pendingCloseIntent);
        smallNotificationView.setOnClickPendingIntent(R.id.status_bar_collapse, pendingCloseIntent);

        bigNotificationView.setOnClickPendingIntent(R.id.status_bar_play, pendingPauseIntent);
        smallNotificationView.setOnClickPendingIntent(R.id.status_bar_play, pendingPauseIntent);

        bigNotificationView.setOnClickPendingIntent(R.id.status_bar_next, pendingNextIntent);
        smallNotificationView.setOnClickPendingIntent(R.id.status_bar_next, pendingNextIntent);

        bigNotificationView.setOnClickPendingIntent(R.id.status_bar_prev, pendingPreviousIntent);
        smallNotificationView.setOnClickPendingIntent(R.id.status_bar_prev, pendingPreviousIntent);

        bigNotificationView.setTextViewText(R.id.status_bar_track_name, media.getTitle());
        smallNotificationView.setTextViewText(R.id.status_bar_track_name, media.getTitle());

        PendingIntent pi =
                PendingIntent.getActivity(playerService.getApplicationContext(), 0,
                        new Intent(playerService.getApplicationContext(), MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationBuilder = new NotificationCompat.Builder(playerService.getApplicationContext())
                .setSmallIcon(R.drawable.ic_play)
                .setOngoing(true)
                .setContentIntent(pi)
                .setContent(smallNotificationView)
                .setPriority(Notification.PRIORITY_MAX);

        mNotification = mNotificationBuilder.getNotification();
        mNotification.bigContentView = bigNotificationView;

        if (!TextUtils.isEmpty(url)) {
//            try {
//                Bitmap bitmap = Glide
//                        .with(playerService.getApplicationContext())
//                        .load(url)
//                        .asBitmap()
//                        .centerCrop()
//                        .into(500, 500)
//                        .get();

            NotificationTarget notificationTarget = new NotificationTarget(
                    playerService.getApplicationContext(),
                    bigNotificationView,
                    R.id.status_bar_album_art,
                    mNotification,
                    NOTIFICATION_ID_PLAYER);

            Glide.with(playerService.getApplicationContext())
                    .load(url)
                    .asBitmap()
                    .centerCrop()
                    .into(notificationTarget);

//                bigNotificationView.setImageViewBitmap(R.id.status_bar_album_art, bitmap);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }

        } else {
            bigNotificationView.setImageViewResource(R.id.status_bar_album_art, R.drawable.artwork_default);
        }
        return mNotificationBuilder.build();
    }

    public void notifyForeground(int srcImage) {
        bigNotificationView.setImageViewResource(R.id.status_bar_play, srcImage);
        smallNotificationView.setImageViewResource(R.id.status_bar_play, srcImage);
        mNotificationManager.notify(NOTIFICATION_ID_PLAYER, mNotification);
    }


}
