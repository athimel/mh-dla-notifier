package org.zoumbox.mh_dla_notifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;

import java.util.Date;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class Receiver extends BroadcastReceiver {

    private static final String TAG = Constants.LOG_PREFIX + Receiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "Alarm received : " + Math.random());

        Date date = ProfileProxy.getDLA(context);
        Integer pa = ProfileProxy.getPA(context);

        CharSequence notifTitle = context.getText(R.string.dla_notif);
        String format = context.getText(R.string.dla_notif_expires).toString();
        CharSequence notifText = String.format(format, MhDlaNotifierUtils.formatHour(date), pa);

        long now = System.currentTimeMillis();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.troll_accueil_1, notifTitle, now);
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        // The PendingIntent to launch our activity if the user selects this notification
        Intent main = new Intent(context, Main.class);
        main.putExtra(Main.EXTRA_FROM_NOTIFICATION, true);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, main, 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(context, notifTitle, notifText, contentIntent);

        notificationManager.notify(0, notification);
    }

}
