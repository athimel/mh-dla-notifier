/*
 * #%L
 * MountyHall-DLA-Notifier
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2012 Zoumbox.org
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
package org.zoumbox.mh_dla_notifier;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.common.base.Preconditions;
import org.zoumbox.mh_dla_notifier.profile.Race;
import org.zoumbox.mh_dla_notifier.profile.Troll;
import org.zoumbox.mh_dla_notifier.profile.UpdateRequestType;

import java.util.Calendar;
import java.util.Date;

/**
 * Activité principale
 */
public abstract class MhDlaNotifierUI extends AbstractActivity {

    private static final String TAG = Constants.LOG_PREFIX + MhDlaNotifierUI.class.getSimpleName();

    public static final int REGISTER = 0;
    public static final int PREFERENCES = 1;

    protected static final int CREDIT_DIALOG = 0;

    public static final String EXTRA_FROM_NOTIFICATION = "from-notification";

    private ImageView blason;
    private TextView name;
    private TextView numero;
    private TextView race;
    private TextView guilde;
    private TextView pvs;
    private TextView fatigue;
    private TextView kd;
    private TextView position;
    private TextView dla;
    private TextView next_dla;
    private TextView dla_duration;
    private TextView remainingPAs;

    private TextView status;

    ///////////////////////////////////
    //  ANDROID INTERACTION METHODS  //
    ///////////////////////////////////

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        blason = (ImageView) findViewById(R.id.blason);
        name = (TextView) findViewById(R.id.name);
        numero = (TextView) findViewById(R.id.number);
        race = (TextView) findViewById(R.id.race);
        guilde = (TextView) findViewById(R.id.guilde);
        pvs = (TextView) findViewById(R.id.pvs);
        fatigue = (TextView) findViewById(R.id.fatigue);
        kd = (TextView) findViewById(R.id.kd);
        position = (TextView) findViewById(R.id.position);
        dla = (TextView) findViewById(R.id.dla_field);
        dla_duration = (TextView) findViewById(R.id.dla_duration_field);
        next_dla = (TextView) findViewById(R.id.next_dla_field);
        remainingPAs = (TextView) findViewById(R.id.pas);

        status = (TextView) findViewById(R.id.status);

        loadTroll();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        Log.i(TAG, "Clear notifications");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.qr_code_market:
                Intent intent_market = new Intent(this, QRCodeMarketActivity.class);
                startActivity(intent_market);
                return true;
            case R.id.credits:
                showDialog(CREDIT_DIALOG);
                return true;
            case R.id.register:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivityForResult(intent, REGISTER);
                return true;
            case R.id.refresh:
                manualRefresh();
                return true;
            case R.id.preferences:
                Intent intent_preferences = new Intent(this, MhPreferencesActivity.class);
                startActivityForResult(intent_preferences, PREFERENCES);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == CREDIT_DIALOG) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.credits);
            dialog.setTitle(R.string.app_name);
            Button btnExitInfo = (Button) dialog.findViewById(R.id.credits_close);
            btnExitInfo.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            return dialog;
        }
        return super.onCreateDialog(id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REGISTER && resultCode == RESULT_OK) {
            startUpdate(UpdateRequestType.FULL, "Récupération du profil");
        }
        if (requestCode == PREFERENCES) {
            registerAlarms();
        }
    }

    public void onPlayButtonClicked(View target) {
        Uri uri = Constants.MH_PLAY_URI;
        PreferencesHolder preferences = PreferencesHolder.load(this);
        if (preferences.useSmartphoneInterface) {
            uri = Constants.MH_PLAY_SMARTPHONE_URI;
        }
        Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(webIntent);
    }


    //////////////////////
    //  NOT UI METHODS  //
    //////////////////////

    protected abstract void loadTroll();

    protected abstract void registerAlarms();

    protected abstract void manualRefresh();

    protected abstract void startUpdate(UpdateRequestType updateType, String toast);


    //////////////////
    //  UI METHODS  //
    //////////////////

    protected void setStatus(String message) {
        this.status.setText(message);
    }

    protected void setStatus(String message, int duration) {
        setStatus(message);
        new ClearStatusTask().execute(duration);
    }

    protected void clearStatus() {
        this.status.setText("");
    }

    protected void pushTrollToUI(Troll troll) {

        Preconditions.checkNotNull(troll, "Troll cannot be null");

        this.name.setText(troll.nom);

        this.numero.setText("N° " + troll.id);

        this.race.setText(String.format("%s (%d)", troll.race, troll.nival));

        String kdString = String.format("%d / %d", troll.nbKills, troll.nbMorts);
        int kdStringLength = kdString.length();
        if (troll.nbMorts > 0) {
            kdString += String.format(" (ratio: %.1f) ", new Integer(troll.nbKills).doubleValue() / new Integer(troll.nbMorts).doubleValue());
        }
        SpannableString kdSpannable = new SpannableString(kdString);
        if (kdString.length() > kdStringLength) {
            kdSpannable.setSpan(new StyleSpan(Typeface.ITALIC), kdStringLength, kdString.length(), 0);
        }
        kd.setText(kdSpannable);

        int pvMax = troll.getPvMax();

        int additionalPvs = pvMax - troll.pvMaxBase;
        String pvMaxString = "" + troll.pvMaxBase;
        if (additionalPvs > 0) {
            pvMaxString += String.format("+%d", additionalPvs);
        }
        String pvText = String.format("%s / %s", troll.pv, pvMaxString);
        SpannableString pvSpannable = new SpannableString(pvText);
        try {
            int pvLength = 1;
            if (troll.pv > 100) {
                pvLength = 3;
            } else if (troll.pv > 10) {
                pvLength = 2;
            }

            if (troll.pv < (pvMax * Constants.PV_ALARM_THRESHOLD / 100)) {
                pvSpannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.pv_alarm)), 0, pvLength, 0);
                pvSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, pvLength, 0);
            } else if (troll.pv < (pvMax * Constants.PV_WARM_THRESHOLD / 100)) {
                pvSpannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.pv_warn)), 0, pvLength, 0);
                pvSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, pvLength, 0);
            }
        } catch (NumberFormatException nfe) {
            // Nothing to do, ignore it
        }
        pvs.setText(pvSpannable);

        String fatigueString = "" + troll.fatigue;
        SpannableString fatigueSpannable;
        if (Race.Kastar.equals(troll.race)) {
            int fatigueLength = fatigueString.length();
            fatigueString += String.format(" (AM: 1 PV = %d') ", Troll.GET_DLA_GAIN_BY_PV.apply(troll.fatigue));
            fatigueSpannable = new SpannableString(fatigueString);
            fatigueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), fatigueLength, fatigueString.length(), 0);
        } else {
            fatigueSpannable = new SpannableString(fatigueString);
        }
        fatigue.setText(fatigueSpannable);

        position.setText(
                String.format("X=%d | Y=%d | N=%d",
                        troll.posX, troll.posY, troll.posN));

        Date rawDla = troll.dla;
        int pa = troll.pa;

        SpannableString dlaSpannable = new SpannableString(MhDlaNotifierUtils.formatDLA(this, rawDla));
        SpannableString paSpannable = new SpannableString("" + pa); // Leave ""+ as integer is considered as an Android id

        dlaSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, dlaSpannable.length(), 0);

        Date now = new Date();
        if (now.after(rawDla)) {
            showToast(getText(R.string.dla_expired_title));
            dlaSpannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.dla_expired)), 0, dlaSpannable.length(), 0);
        } else {
            PreferencesHolder preferences = PreferencesHolder.load(this);
            Date dlaMinusNDMin = MhDlaNotifierUtils.substractMinutes(rawDla, preferences.notificationDelay);
            if (now.after(dlaMinusNDMin)) {
                dlaSpannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.dla_to_expire)), 0, dlaSpannable.length(), 0);
                if (pa > 0) {
                    showToast("Il vous reste des PA à jouer !");
                    paSpannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.dla_to_expire)), 0, paSpannable.length(), 0);
                    paSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, paSpannable.length(), 0);
                }
            }
        }

        dla.setText(dlaSpannable);
        remainingPAs.setText(paSpannable);

        int nextDlaDuration = Troll.GET_NEXT_DLA_DURATION.apply(troll);
        dla_duration.setText(MhDlaNotifierUtils.prettyPrintDuration(this, nextDlaDuration));

        Calendar nextDla = Calendar.getInstance();
        nextDla.setTime(rawDla);
        nextDla.add(Calendar.MINUTE, ((Double) Math.floor(nextDlaDuration)).intValue());
        String nextDlaText = MhDlaNotifierUtils.formatDLA(this, nextDla.getTime());
        next_dla.setText(nextDlaText);

        new LoadBlasonTask().execute(troll.blason);

        new LoadGuildeTask().execute(troll.guilde);

    }

    protected void updateBlason(Bitmap blason) {
        if (blason == null) {
            this.blason.setImageResource(R.drawable.pas_de_blason);
        } else {
            this.blason.setImageBitmap(blason);
        }
    }

    protected void updateGuilde(String guilde) {
        this.guilde.setText(guilde);
    }

    /////////////
    //  TASKS  //
    /////////////

    private class LoadBlasonTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            String blasonUrl = params[0];
            Bitmap result = MhDlaNotifierUtils.loadBlason(blasonUrl, getCacheDir());
            return result;
        }

        @Override
        protected void onPostExecute(Bitmap blason) {
            updateBlason(blason);
        }
    }

    private class ClearStatusTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(Integer... params) {
            Integer duration = params[0];
            try {
                Thread.sleep(duration * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return duration;
        }

        @Override
        protected void onPostExecute(Integer duration) {
            clearStatus();
        }
    }

    private class LoadGuildeTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... params) {
            int guildeNumber = params[0];
            String result = MhDlaNotifierUtils.loadGuilde(guildeNumber, getCacheDir());
            return result;
        }

        @Override
        protected void onPostExecute(String guilde) {
            updateGuilde(guilde);
        }
    }

}
