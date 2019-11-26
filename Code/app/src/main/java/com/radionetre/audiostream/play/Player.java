package com.radionetre.play;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.support.v4.app.NotificationCompat;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.radionetre.R;
import com.radionetre.network.Icecast;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Player {

    //singleton implementation
    private static Player instance = new Player();

    private Player() {
    }

    public static Player getInstance() {
        return instance;
    }


    private final int NOTIFICATION_ID = 0;
    private final int TICKER_MAX_LENGTH = 100;

    private HashMap<String, String> radio;
    private MediaPlayer player;
    protected Activity context;

    /**
     * Timer to poll stream metadata every once in a while.
     * I don't know if Icecast supports push events, so I'm
     * sending a request for metadata every minute.
     */
    private static Timer metadataTimer;

    /**
     * The notification that will be displayed in the notification drawer.
     */
    private static NotificationCompat.Builder streamNotification;

    public String getStream() {
        return radio.get("stream");
    }

    public MediaPlayer getMP() {
        return player;
    }

    public void playStream(Activity theContext,
                           HashMap<String, String> theRadio) {
        stop();

        context = theContext;
        radio = theRadio;
        player = new MediaPlayer();

        try {
            player.setDataSource(radio.get("stream"));
            player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                }
            });
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                // Player ready, start automatically
                play();
                }
            });
            player.prepareAsync();

            notifyNewStream();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Restart media player after pause
    public void play() {
        if (player != null)
        {
            player.start();
            fetchIcecastData();
        }
    }


    /**
     * Do not pause radio. "Pause" caches the stream, so when we play()
     * it again later, it will restart from where it was paused.
     */
    public void pause() {
        if (player != null)
        {
            player.pause();
            stopFetchingIcecastData();
        }
    }

    public void playPause() {
        if (player != null)
            if (player.isPlaying())
            {
                stopFetchingIcecastData();

                /**
                 * Stop and reset the MediaPlayer.
                 * The reason we reset() after stop() is because when
                 * prepareAsync() is called after stop(), the stream
                 * stops automatically after a few second. Looks like
                 * it empties the stream cache and then stops. I don't
                 * know why this is happening, so we just return to the
                 * Idle() state and re-initialize the data source again.
                 */
                player.stop();
                player.reset();

                updateNotification("Paused", null, null);
            } else {
                try {
                    player.setDataSource(radio.get("stream"));
                    player.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                notifyNewStream();
            }
    }

    public void stop() {
        if (player != null)
        {
            stopFetchingIcecastData();

            player.stop();
            player.release();
            player = null;

            radio = null;
            context = null;
        }
    }

    public void fetchIcecastData() {
        // Stop any previous task fetching data
        stopFetchingIcecastData();

        /**
         * When a timer is no longer needed, users should call cancel(),
         * which releases the timer's thread and other resources. Timers
         * not explicitly cancelled may hold resources indefinitely.
         */
        Player.metadataTimer = new Timer();

        // Fetch data automatically every few minutes
        Player.metadataTimer.schedule(
            new TimerTask() {
                @Override
                public void run()
                {
                    String songMetadata = "";

                    try {
                        Map<String, String> metadata =
                            Icecast.getMetadata(new URL(radio.get("stream")));

                        if (metadata != null)
                            songMetadata = metadata.get("StreamTitle").trim();
                    } catch (Exception e) {
                        // e.printStackTrace();
                    }

                    // Update notification with new metadata about current song
                    if (songMetadata.length() == 0) {
                        updateNotification("", null, null);
                    }
                    else {
                        updateNotification(songMetadata,
                                context.getString(R.string.notification_listening)
                                        + songMetadata, null);
                    }
                }
            },
            // Start after 0s, repeat after 60s
            0, 60000);
    }

    public void stopFetchingIcecastData() {
        if (Player.metadataTimer != null)
        {
            Player.metadataTimer.cancel();
            Player.metadataTimer.purge();
            Player.metadataTimer = null;
        }
    }

    /**
     * Show/Hide extended notification with Play/Stop buttons
     */
    public void togglePlayerCompact() {
        if (context == null)
            return;

        // Play/Pause intent when tapping the notification
        Intent play_intent = new Intent(context, PlayPause.class);
        PendingIntent play_pintent = PendingIntent.getBroadcast(context, 0, play_intent, 0);

        // Stop intent when removing the notification
        Intent stop_intent = new Intent(context, Stop.class);
        PendingIntent stop_pintent = PendingIntent.getBroadcast(context, 0, stop_intent, 0);

        Player.streamNotification
            // Add intent when tapping the notification
            .setContentIntent(play_pintent)
            // Add intent when removing notification
            .setDeleteIntent(stop_pintent);

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
            (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        // Builds the notification and issues it.
        // Because the ID remains unchanged, the existing notification is updated
        mNotifyMgr.notify (
            NOTIFICATION_ID,
            streamNotification.build());
    }

    // Show a notification about the new stream
    private void notifyNewStream() {
        // Normalize radio name
        String radioName = radio.get("name").trim();
        if (radioName.length() > TICKER_MAX_LENGTH)
            radioName = radioName.substring(0, TICKER_MAX_LENGTH) + "...";

        Player.streamNotification = new NotificationCompat.Builder(context)
            .setAutoCancel(false)
            // Show controls on lock screen even when user hides sensitive content.
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_graphic_eq)
            .setContentTitle(radioName)
            .setContentText(context.getString(R.string.notification_buffering))
            //.setLargeIcon(albumArtBitmap)
            .setTicker(context.getString(R.string.notification_buffering) + " " + radioName)
            // Display datetime in notification
            .setShowWhen(true)
            // Show elapsed time instead of creation time
            .setUsesChronometer(true);

        // If not compact player, show notification buttons
        togglePlayerCompact();

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
            (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        // Builds the notification and issues it.
        mNotifyMgr.notify (
            NOTIFICATION_ID,
            Player.streamNotification.build());

        updateNotificationLargeIcon();
    }

    // Change text when new Icecast data is available
    private void updateNotification(String content, String ticker, Bitmap largeIcon) {
        // Content is changed to update song info
        if (content != null)
            Player.streamNotification.setContentText(content);

        // Show a new ticker
        if (ticker != null)
        {
            // Remove leading/trailing spaces
            ticker = ticker.trim();

            // Cut ticker message if it's too long
            if (ticker.length() > TICKER_MAX_LENGTH)
                ticker = ticker.substring(0, TICKER_MAX_LENGTH) + "...";

            Player.streamNotification.setTicker(ticker);
        }

        // Update large icon in notification
        if (largeIcon != null)
            Player.streamNotification.setLargeIcon(largeIcon);

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        // Builds the notification and issues it.
        // Because the ID remains unchanged, the existing notification is updated
        mNotifyMgr.notify (
            NOTIFICATION_ID,
            Player.streamNotification.build());
    }

    /**
     * Send a new async HTTP request to fetch radio image, and then
     * upload the notification with the new logo.
     */
    private void updateNotificationLargeIcon() {
        RequestQueue queue = Volley.newRequestQueue(context);
        ImageLoader imageLoader = new ImageLoader(queue, new ImageLoader.ImageCache() {
            @Override
            public Bitmap getBitmap(String url) {
                return null;
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {

            }
        });

        imageLoader.get(radio.get("logo"), new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                updateNotification(null, null, response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }
}