package org.zoumbox.mh_dla_notifier;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import org.zoumbox.mh_dla_notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.Troll;
import org.zoumbox.mh_dla_notifier.profile.UpdateRequestType;
import org.zoumbox.mh_dla_notifier.sp.NetworkUnavailableException;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;
import org.zoumbox.mh_dla_notifier.sp.QuotaExceededException;
import org.zoumbox.mh_dla_notifier.sp.ScriptCategory;

import java.util.Date;

public class MainActivity extends MhDlaNotifierUI {

    private static final String TAG = Constants.LOG_PREFIX + MainActivity.class.getSimpleName();

    @Override
    protected void loadTroll() {

        Log.i(TAG, "Initial Loading Troll");

        try {
            // First load the troll without update
            Troll troll = ProfileProxy.fetchTroll(this, UpdateRequestType.NONE);

            trollUpdated(troll);

            if (troll.updateRequestType.needUpdate()) {
                startUpdate(UpdateRequestType.ONLY_NECESSARY);
            }

        } catch (MissingLoginPasswordException mlpe) {
            showToast("Vous devez saisir vos identifiants");
            Log.i(TAG, "Login or password are missing, calling RegisterActivity");
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivityForResult(intent, REGISTER);
        } catch (QuotaExceededException e) {
            showToast("Rafraichissement impossible pour le moment, quota dépassé");
            Log.e(TAG, "Unable to refresh, quota exceeded", e);
        } catch (PublicScriptException e) {
            String message = e.getMessage();
            Log.i(TAG, "Erreur de script public: " + message);
            showToast(message);
            if (message.startsWith("Erreur 2") || message.startsWith("Erreur 3")) {
                showToast("Veuillez vérifier vos paramètres");
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivityForResult(intent, REGISTER);
            }
        } catch (NetworkUnavailableException e) {
            Log.i(TAG, "Pas de réseau, mise à jour des informations impossible");
            showToast("Pas de réseau, mise à jour des informations impossible");
        }
    }

    @Override
    protected void manualRefresh() {
        int quota = ProfileProxy.GET_USABLE_QUOTA.apply(ScriptCategory.DYNAMIC) / 2; // %2 pour garder une marge de sécu vis à vis des maj auto
        showToast("Attention à ne pas dépasser %d mises à jour manuelles par jour.", quota);
        startUpdate(UpdateRequestType.FULL);
    }

    @Override
    protected void startUpdate(UpdateRequestType updateType, String message) {
        setStatus(message);
        new UpdateTrollTask().execute(updateType);
    }

    protected void startUpdate(UpdateRequestType updateType) {
        startUpdate(updateType, "Mise à jour...");
    }

    protected void updateSuccess() {
        String message = String.format("Mis à jour à %s", MhDlaNotifierUtils.formatHour(new Date()));
        setStatus(message, 60);
    }

    protected void updateFailure(String error) {
        setStatus(error, 30);
    }

    protected void trollUpdated(Troll troll) {
        pushTrollToUI(troll);

        registerAlarms();
    }

    @Override
    protected void registerAlarms() {

        boolean fromNotification = getIntent().getBooleanExtra(EXTRA_FROM_NOTIFICATION, false);
        Log.i(TAG, "From notification: " + fromNotification);

        if (!fromNotification) {
            Pair<Date, Date> nextAlarms = Receiver.registerDlaAlarms(this);
            if (nextAlarms != null) {
                Date currentDlaAlarm = nextAlarms.left();
                Date nextDlaAlarm = nextAlarms.right();

                if (currentDlaAlarm != null) {
                    Log.i(TAG, "Current DLA alarm at " + currentDlaAlarm);
                    String text = getText(R.string.next_alarm).toString();
                    String message = String.format(text, MhDlaNotifierUtils.formatDay(currentDlaAlarm), MhDlaNotifierUtils.formatHour(currentDlaAlarm));
                    showToast(message);
                }

                if (nextDlaAlarm != null) {
                    Log.i(TAG, "Next DLA alarm at " + nextDlaAlarm);
                    String text = getText(R.string.next_alarm).toString();
                    String message = String.format(text, MhDlaNotifierUtils.formatDay(nextDlaAlarm), MhDlaNotifierUtils.formatHour(nextDlaAlarm));
                    showToast(message);
                }

            }
        }
    }

    private class UpdateTrollTask extends AsyncTask<UpdateRequestType, Void, Pair<Troll, MhDlaException>> {

        @Override
        protected Pair<Troll, MhDlaException> doInBackground(UpdateRequestType... params) {
            Troll troll = null;
            MhDlaException exception = null;
            try {
                troll = ProfileProxy.fetchTroll(MainActivity.this, params[0]);

            } catch (MhDlaException e) {
                exception = e;
                e.printStackTrace();
            }
            Pair<Troll, MhDlaException> result = new Pair<Troll, MhDlaException>(troll, exception);
            return result;
        }

        @Override
        protected void onPostExecute(Pair<Troll, MhDlaException> result) {
            MhDlaException exception = result.right();
            if (exception != null) {
                String reason = exception.getText();
                updateFailure("Échec : " + reason);
            } else {
                Troll troll = result.left();
                trollUpdated(troll);
                updateSuccess();
            }
        }
    }

}
