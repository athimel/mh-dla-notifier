package org.zoumbox.mh_dla_notifier;

/*
 * #%L
 * MountyHall DLA Notifier
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2012 - 2014 Zoumbox.org
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

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zoumbox.mh_dla_notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.UpdateRequestType;
import org.zoumbox.mh_dla_notifier.sp.MhSpRequest;
import org.zoumbox.mh_dla_notifier.sp.NetworkUnavailableException;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptsProxy;
import org.zoumbox.mh_dla_notifier.sp.QuotaExceededException;
import org.zoumbox.mh_dla_notifier.sp.ScriptCategory;
import org.zoumbox.mh_dla_notifier.troll.Troll;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import android.os.AsyncTask;
import android.util.Log;

public class MainActivity extends MhDlaNotifierUI {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + MainActivity.class.getSimpleName();

    protected static final Function<Map.Entry<ScriptCategory, Integer>, String> QUOTA_ENTRY_TO_STRING = new Function<Map.Entry<ScriptCategory, Integer>, String>() {
        @Override
        public String apply(Map.Entry<ScriptCategory, Integer> input) {
            ScriptCategory category = input.getKey();
            String result = String.format("%s=%d/%d", category.name(), input.getValue(), category.getQuota());
            return result;
        }
    };

    protected String getCurrentTrollId() {
        // TODO AThimel 20/08/13 Manage several trolls
        Set<String> trollIds = getProfileProxy().getTrollIds(this);
        Iterator<String> iterator = trollIds.iterator();
        String trollId = null;
        if (iterator.hasNext()) {
            trollId = iterator.next();
        }
        return trollId;
    }

    @Override
    protected Troll readTrollWithoutUpdate() throws Exception {

        // First load the troll without update
        String trollId = getCurrentTrollId();

        if (Strings.isNullOrEmpty(trollId)) {
            return null;
        } else {
            Pair<Troll, Boolean> trollAndUpdate = getProfileProxy().fetchTrollWithoutUpdate(this, trollId);
            Troll troll = trollAndUpdate.left();
            return troll;
        }
    }

    @Override
    protected void loadTroll() {

        Log.i(TAG, "Initial Loading Troll");

        try {
            // First load the troll without update
            String trollId = getCurrentTrollId();

            if (Strings.isNullOrEmpty(trollId)) {
                startRegister("Vous devez saisir vos identifiants");
            } else {
                Pair<Troll, Boolean> trollAndUpdate = getProfileProxy().fetchTrollWithoutUpdate(this, trollId);
                Troll troll = trollAndUpdate.left();
                boolean needsUpdate = trollAndUpdate.right();

                trollUpdated(troll, needsUpdate);

                clearTechnicalStatus();

                if (needsUpdate) {
                    startUpdate(UpdateRequestType.ONLY_NECESSARY);
                }
            }

        } catch (MhDlaException mde) {
            updateFailure(mde);
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
        Date result = getProfileProxy().getLastUpdateSuccess(this, getCurrentTrollId());
        return result;
    }

    @Override
    protected void startUpdate(UpdateRequestType updateType, String message) {
        new UpdateTrollTask(message).execute(updateType);
    }

    protected void startUpdate(UpdateRequestType updateType) {
        String message = UpdateRequestType.FULL.equals(updateType) ? "Mise à jour..." : "Mise à jour partielle...";
        startUpdate(updateType, message);
    }

    protected void updateSuccess() {
        updateFinished();
        clearTechnicalStatus();
    }

    protected void updateFailure(MhDlaException exception) {
        updateFinished();
        String error = null;
        if (exception instanceof QuotaExceededException) {
            String message = "Mise à jour bloquée pour le moment, quota atteint";
            showToast(message);
            Log.e(TAG, message, exception);

            error = "Quota atteint";
        } else if (exception instanceof PublicScriptException) {
            String message = exception.getMessage();
            Log.w(TAG, "Erreur : " + message);
            showToast(message);

            if (message.startsWith("Erreur 2") || message.startsWith("Erreur 3")) {
                startRegister("Veuillez vérifier vos paramètres");
            } else {
                error = message;
            }
        } else if (exception instanceof NetworkUnavailableException) {
            String message = "Pas de réseau, mise à jour des informations impossible";
            Log.w(TAG, message);
            showToast(message);

            error = "Pas de réseau";
        } else if (exception instanceof MissingLoginPasswordException) {
            startRegister("Veuillez saisir vos identifiants");
        } else {
            throw new RuntimeException("Unexpected exception", exception);
        }
        setTechnicalStatusError(error);
    }

    protected void trollUpdated(Troll troll, boolean updateToFollow) {
        try {
            Map<ScriptCategory, Integer> quotas = PublicScriptsProxy.listQuotas(this, troll.getNumero());
            Log.i(TAG, String.format("24H quotas are: %s", Iterables.transform(quotas.entrySet(), QUOTA_ENTRY_TO_STRING)));
            List<MhSpRequest> requests = PublicScriptsProxy.listLatestRequests(this, troll.getNumero(), 50);
            Log.d(TAG, String.format("Here comes the %d latest requests:", requests.size()));
            for (MhSpRequest request : requests) {
                Log.d(TAG, " -> " + request);
            }
        } catch (Exception eee) {
            Log.w(TAG, "Exception", eee);
        }

        pushTrollToUI(troll, updateToFollow);

        scheduleAlarms(false);
    }

    protected void showAlarmToast(AlarmType type, Date date) {
        if (date != null) {
            int resId;
            switch (type) {
                case CURRENT_DLA:
                    resId = R.string.alarm_scheduled_for_current_dla;
                    break;
                case NEXT_DLA_ACTIVATION:
                    resId = R.string.alarm_scheduled_for_next_dla_activation;
                    break;
                case NEXT_DLA:
                    resId = R.string.alarm_scheduled_for_next_dla;
                    break;
                default:
                    resId = R.string.alarm_scheduled;
                    break;
            }
            String text = getText(resId).toString();
            String day = MhDlaNotifierUtils.formatDay(date);
            String hour = MhDlaNotifierUtils.formatHour(date);
            String message = String.format(text, day, hour);
            showToast(message);
        }
    }

    @Override
    protected void scheduleAlarms(boolean displayToast) {

        boolean fromNotification = getIntent().getBooleanExtra(EXTRA_FROM_NOTIFICATION, false);
        Log.d(TAG, "From notification: " + fromNotification);

        if (displayToast || !fromNotification) {
            String trollId = getCurrentTrollId();
            new ScheduleAlarmsTask(displayToast).execute(trollId);
        }
    }

    private class ScheduleAlarmsTask extends AsyncTask<String, Void, Pair<Map<AlarmType, Date>, MhDlaException>> {

        protected boolean displayToast;

        private ScheduleAlarmsTask(boolean displayToast) {
            this.displayToast = displayToast;
        }

        @Override
        protected Pair<Map<AlarmType, Date>, MhDlaException> doInBackground(String ... params) {

            MhDlaException exception = null;
            Map<AlarmType, Date> scheduledAlarms = null;
            try {
                String trollId = params[0];
                scheduledAlarms = Alarms.scheduleAlarms(MainActivity.this, getProfileProxy(), trollId);
            } catch (MissingLoginPasswordException e) {
                exception = e;
            }
            return Pair.of(scheduledAlarms, exception);
        }

        @Override
        protected void onPostExecute(Pair<Map<AlarmType, Date>, MhDlaException> result) {
            MhDlaException exception = result.right();
            if (exception != null) {
                startRegister(null);
            } else {
                Map<AlarmType, Date> scheduledAlarms = result.left();
                if (scheduledAlarms != null) {
                    Date currentDlaAlarm = scheduledAlarms.get(AlarmType.CURRENT_DLA);
                    Date nextDlaActivationAlarm = scheduledAlarms.get(AlarmType.NEXT_DLA_ACTIVATION);
                    Date nextDlaAlarm = scheduledAlarms.get(AlarmType.NEXT_DLA);

                    if (displayToast) {
                        showAlarmToast(AlarmType.CURRENT_DLA, currentDlaAlarm);
                        showAlarmToast(AlarmType.NEXT_DLA_ACTIVATION, nextDlaActivationAlarm);
                        showAlarmToast(AlarmType.NEXT_DLA, nextDlaAlarm);
                    }

                }
            }
        }
    }

    private class UpdateTrollTask extends AsyncTask<UpdateRequestType, Void, Pair<Troll, MhDlaException>> {

        protected String message;

        private UpdateTrollTask(String message) {
            this.message = message;
        }

        @Override
        protected void onPreExecute() {
            // set the progress bar view
            updateStarted(message);
        }

        @Override
        protected Pair<Troll, MhDlaException> doInBackground(UpdateRequestType... params) {

            Troll troll = null;
            MhDlaException exception = null;
            try {
                UpdateRequestType updateRequestType = params[0];
                Pair<Troll, Boolean> trollAndUpdate = getProfileProxy().fetchTroll(
                        MainActivity.this,
                        MainActivity.this.getCurrentTrollId(),
                        updateRequestType);
                troll = trollAndUpdate.left();
            } catch (MhDlaException e) {
                exception = e;
                e.printStackTrace();
            }
            Pair<Troll, MhDlaException> result = Pair.of(troll, exception);

            return result;
        }

        @Override
        protected void onPostExecute(Pair<Troll, MhDlaException> result) {
            MhDlaException exception = result.right();
            if (exception != null) {
                updateFailure(exception);
            } else {
                Troll troll = result.left();
                trollUpdated(troll, false);
                updateSuccess();
            }
        }
    }

}
