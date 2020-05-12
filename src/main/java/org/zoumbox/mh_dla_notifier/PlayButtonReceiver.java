package org.zoumbox.mh_dla_notifier;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class PlayButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // trick to reduce notification panel before starting new activity
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
        // fin trick
        playTroll(context);
        int notificationId = intent.getIntExtra("NOTIFICATION_ID", 0);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
    }

    public void playTroll(Context context) {
        Uri uri = MhDlaNotifierConstants.MH_PLAY_URI;
        PreferencesHolder preferences = PreferencesHolder.load(context);
        if (preferences.useSmartphoneInterface) {
            uri = MhDlaNotifierConstants.MH_PLAY_SMARTPHONE_URI;
        }
        Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(webIntent);
    }

}
