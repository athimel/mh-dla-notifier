package org.zoumbox.mh_dla_notifier;

/*
 * #%L
 * MountyHall DLA Notifier
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2012 - 2013 Zoumbox.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
import java.util.Map;

public class MainActivity extends MhDlaNotifierUI {

    private static final String TAG = Constants.LOG_PREFIX + MainActivity.class.getSimpleName();

    @Override
    protected void loadTroll() {

        Log.i(TAG, "Initial Loading Troll");

        try {
            // First load the troll without update
            Troll troll = ProfileProxy.fetchTrollWithoutUpdate(this);

            trollUpdated(troll);

            clearStatus();

            if (troll.updateRequestType.needUpdate()) {
//                showToast("Mise à jour");
                startUpdate(UpdateRequestType.ONLY_NECESSARY);
            }

        } catch (MissingLoginPasswordException mlpe) {
            startRegister("Vous devez saisir vos identifiants");
        }
    }

    @Override
    protected void manualRefresh() {
        int quota = ProfileProxy.GET_USABLE_QUOTA.apply(ScriptCategory.DYNAMIC) / 2; // %2 pour garder une marge de sécu vis à vis des maj auto
        showToast("Attention à ne pas dépasser %d mises à jour manuelles par jour.", quota);
        startUpdate(UpdateRequestType.FULL);
    }

    @Override
    protected Date getLastUpdate() {
        Date lastUpdate = null;
        try {
            Troll troll = ProfileProxy.fetchTrollWithoutUpdate(this);
            lastUpdate = troll.lastUpdate;
        } catch (MissingLoginPasswordException mlpe) {
            // do nothing except log
            Log.w("Missing login/password during getLastUpdate", mlpe);
        }
        return lastUpdate;
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
        clearStatus();
    }

    protected void updateFailure(MhDlaException exception) {
        String error = null;
        if (exception instanceof QuotaExceededException) {
            String message = "Rafraîchissement impossible pour le moment, quota atteint";
            showToast(message);
            Log.e(TAG, message, exception);

            error = "Quota atteint";
        } else if (exception instanceof PublicScriptException) {
            String message = exception.getMessage();
            Log.i(TAG, "Erreur : " + message);
            showToast(message);

            if (message.startsWith("Erreur 2") || message.startsWith("Erreur 3")) {
                startRegister("Veuillez vérifier vos paramètres");
            } else {
                error = message;
            }
        } else if (exception instanceof NetworkUnavailableException) {
            String message = "Pas de réseau, mise à jour des informations impossible";
            Log.i(TAG, message);
            showToast(message);

            error = "Pas de réseau";
        } else if (exception instanceof MissingLoginPasswordException) {
            startRegister("Veuillez saisir vos identifiants");
        } else {
            throw new RuntimeException("Unexpected exception", exception);
        }
        setStatusError(error);
    }

    protected void trollUpdated(Troll troll) {
        pushTrollToUI(troll);

        scheduleAlarms();
    }

    @Override
    protected void scheduleAlarms() {

        boolean fromNotification = getIntent().getBooleanExtra(EXTRA_FROM_NOTIFICATION, false);
        Log.i(TAG, "From notification: " + fromNotification);

        if (!fromNotification) {
            try {
                Map<AlarmType, Date> scheduledAlarms = Receiver.scheduleAlarms(this);
                if (scheduledAlarms != null) {
                    Date currentDlaAlarm = scheduledAlarms.get(AlarmType.CURRENT_DLA);
                    Date nextDlaAlarm = scheduledAlarms.get(AlarmType.NEXT_DLA);

                    if (currentDlaAlarm != null) {
                        String text = getText(R.string.next_alarm).toString();
                        String message = String.format(text, MhDlaNotifierUtils.formatDay(currentDlaAlarm), MhDlaNotifierUtils.formatHour(currentDlaAlarm));
                        showToast(message);
                    }

                    if (nextDlaAlarm != null) {
                        String text = getText(R.string.next_alarm).toString();
                        String message = String.format(text, MhDlaNotifierUtils.formatDay(nextDlaAlarm), MhDlaNotifierUtils.formatHour(nextDlaAlarm));
                        showToast(message);
                    }


                }
            } catch (MissingLoginPasswordException e) {
                startRegister(null);
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
                updateFailure(exception);
            } else {
                Troll troll = result.left();
                trollUpdated(troll);
                updateSuccess();
            }
        }
    }

}
