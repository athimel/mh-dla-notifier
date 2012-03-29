package org.zoumbox.mh_dla_notifier;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.common.base.Predicate;
import org.zoumbox.mh_dla_notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;

import javax.annotation.Nullable;
import java.util.Date;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class Receiver extends BroadcastReceiver {

    private static final String TAG = Constants.LOG_PREFIX + Receiver.class.getSimpleName();

    protected static final Predicate<Date> IS_IN_THE_FUTURE = new Predicate<Date>() {
        @Override
        public boolean apply(@Nullable Date dla) {
            if (dla == null) {
                return false;
            }
            boolean result = dla.after(new Date());
            return result;
        }
    };

    protected static final Predicate<Date> IS_MORE_THAN_5_MIN = new Predicate<Date>() {
        @Override
        public boolean apply(@Nullable Date dla) {
            if (dla == null) {
                return false;
            }
            Date dlaMinus5Min = MhDlaNotifierUtils.substractMinutes(dla, 5);
            boolean result = new Date().before(dlaMinus5Min);
            return result;
        }
    };

    protected static final Predicate<Integer> NOTIFY_WITHOUT_PA = new Predicate<Integer>() {
        @Override
        public boolean apply(@Nullable Integer input) {
            // TODO AThimel 29/03/2012 Evolution #87
            return true;
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "Alarm received : " + Math.random());

        Pair<Date, Integer> dlaPaPair = null;
        try {
            dlaPaPair = ProfileProxy.refreshDLA(context);
        } catch (MissingLoginPasswordException mlpe) {
            // Nothing to do
        } catch (PublicScriptException e) {
            // Nothing to do
        }

        if (dlaPaPair != null) {
            Date dla = dlaPaPair.left();
            Integer pa = dlaPaPair.right();

            if (IS_MORE_THAN_5_MIN.apply(dla)) {
                registerDlaAlarm(context, dla);
            } else
                // On vérifie que la date est bien dans le futur et dans moins de 5 min
                if (IS_IN_THE_FUTURE.apply(dla) && NOTIFY_WITHOUT_PA.apply(pa)) {
                    displayDlaAboutToExpire(context, dla, pa);
                } else {
                    displayDlaExpired(context, dla, pa);
                }
        }
    }

    protected void displayDlaAboutToExpire(Context context, Date dla, Integer pa) {
        CharSequence notifTitle = context.getText(R.string.dla_expiring_title);
        String format = context.getText(R.string.dla_expiring_text).toString();
        CharSequence notifText = String.format(format, MhDlaNotifierUtils.formatHour(dla), pa);

        boolean vibrate = pa > 0;

        displayNotification(context, notifTitle, notifText, vibrate);
    }

    protected void displayDlaExpired(Context context, Date dla, Integer pa) {
        if (pa == 0 && !IS_IN_THE_FUTURE.apply(dla)) {
            CharSequence notifTitle = context.getText(R.string.dla_expired_title);
            String format = context.getText(R.string.dla_expired_text).toString();
            CharSequence notifText = String.format(format, MhDlaNotifierUtils.formatHour(dla));

            displayNotification(context, notifTitle, notifText, false);
        }
    }

    protected void displayNotification(Context context, CharSequence title, CharSequence text, boolean vibrate) {
        long now = System.currentTimeMillis();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.trarnoll_square_transparent_128, title, now);
        if (vibrate) {
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }

        // The PendingIntent to launch our activity if the user selects this notification
        Intent main = new Intent(context, Main.class);
        main.putExtra(Main.EXTRA_FROM_NOTIFICATION, true);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, main, 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(context, title, text, contentIntent);

        notificationManager.notify(0, notification);
    }

    public static Date registerDlaAlarm(Context context, Date dla) {
        Date nextAlarm = null;
        if (dla != null && IS_IN_THE_FUTURE.apply(dla)) {
            nextAlarm = MhDlaNotifierUtils.substractMinutes(dla, 5);

            Intent intent = new Intent(context, Receiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 86956675, intent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlarm.getTime(), pendingIntent);
        }
        return nextAlarm;

    }
}
