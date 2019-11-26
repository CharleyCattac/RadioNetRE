package com.radionetre.play;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Stop extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        // Stop the player
        Player.getInstance().stop();

        // Remove the (sticky) notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification ID set when notification was created
        // notificationManager.cancel(Player.NOTIFICATION_ID);
        notificationManager.cancelAll();
    }
}