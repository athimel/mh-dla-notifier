package org.zoumbox.mh_dla_notifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;

import java.util.Date;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class Receiver extends BroadcastReceiver {


    private static final String TAG = "MhDlaNotifier-" + Receiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        Date date = ProfileProxy.getDLA(context);
        CharSequence notifTitle = context.getText(R.string.dla_notif);
        CharSequence notifText = context.getText(R.string.dla_notif_expires) + " " + MhDlaNotifierUtils.formatHour(date);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.troll_accueil_1, notifTitle, System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        Intent main = new Intent(context, Main.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, main, 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(context, notifTitle, notifText, contentIntent);

        notificationManager.notify(0, notification);

        String trollNumber = ProfileProxy.getTrollNumber(context);
        Log.i(TAG, "Alarm ! " + new Date());
        Toast.makeText(context, "Troll: " + trollNumber, Toast.LENGTH_LONG).show();
    }
}
