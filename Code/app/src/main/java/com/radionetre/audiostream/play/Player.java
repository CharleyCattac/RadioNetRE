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
import com.radionetre.network.MetaParser;

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
    /*
     Timer to poll stream metadata every once in a while.
     (sending a request for metadata every minute)
     */
    private static Timer metadataTimer;
    /*
     The notification that will be displayed in the notification drawer.
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
    
    public void play() {
        if (player != null) {
            player.start();
            fetchMetaData();
        }
    }

    public void pause() {
        if (player != null) {
            player.pause();
            stopFetchingIcecastData();
        }
    }

    public void playPause() {
        if (player != null)
            if (player.isPlaying()) {
                stopFetchingMetaData();

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
        if (player != null) {
            stopFetchingMetaData();

            player.stop();
            player.release();
            player = null;

            radio = null;
            context = null;
        }
    }

    public void fetchMetaData() {
        // Stop any previous task fetching data
        stopFetchingMetaData();

        Player.metadataTimer = new Timer();

        // Fetch data automatically every few minutes
        Player.metadataTimer.schedule(
            new TimerTask() {
                @Override
                public void run() {

                    String songMetadata = "";

                    try {
                        Map<String, String> metadata =
                            getMetadata(new URL(radio.get("stream")));

                        if (metadata != null) {
                            songMetadata = metadata.get("StreamTitle").trim();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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

    public void stopFetchingMetaData() {
        if (Player.metadataTimer != null) {
            Player.metadataTimer.cancel();
            Player.metadataTimer.purge();
            Player.metadataTimer = null;
        }
    }

    // Show a notification about the new stream
    private void notifyNewStream() {
        // Normalize radio name
        String radioName = radio.get("name").trim();
        if (radioName.length() > TICKER_MAX_LENGTH)
            radioName = radioName.substring(0, TICKER_MAX_LENGTH) + "...";

        Player.streamNotification = new NotificationCompat.Builder(context)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_graphic_eq)
            .setContentTitle(radioName)
            .setContentText(context.getString(R.string.notification_buffering))
            .setTicker(context.getString(R.string.notification_buffering) + " " + radioName)
            .setShowWhen(true)
            .setUsesChronometer(true);

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
            (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        // Builds the notification and issues it.
        mNotifyMgr.notify (
            NOTIFICATION_ID,
            Player.streamNotification.build());

        updateNotificationLargeIcon();
    }

    // Change text when new meta data is available
    private void updateNotification(String content, String ticker, Bitmap largeIcon) {
        // Content is changed to update song info
        if (content != null) {
            Player.streamNotification.setContentText(content);
        }

        // Show a new ticker
        if (ticker != null) {
            ticker = ticker.trim();

            if (ticker.length() > TICKER_MAX_LENGTH) {
                ticker = ticker.substring(0, TICKER_MAX_LENGTH) + "...";
            }

            Player.streamNotification.setTicker(ticker);
        }

        if (largeIcon != null) {
            Player.streamNotification.setLargeIcon(largeIcon);
        }

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        mNotifyMgr.notify (
            NOTIFICATION_ID,
            Player.streamNotification.build());
    }

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