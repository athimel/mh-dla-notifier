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
package org.zoumbox.mh_dla_notifier;

import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.UpdateRequestType;
import org.zoumbox.mh_dla_notifier.profile.v2.ProfileProxyV2;
import org.zoumbox.mh_dla_notifier.sp.MhSpRequest;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptsProxy;
import org.zoumbox.mh_dla_notifier.troll.Race;
import org.zoumbox.mh_dla_notifier.troll.Troll;
import org.zoumbox.mh_dla_notifier.troll.Trolls;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

/**
 * Activité principale
 */
public abstract class MhDlaNotifierUI extends AppCompatActivity {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + MhDlaNotifierUI.class.getSimpleName();

    public static final int REGISTER = 0;
    public static final int PREFERENCES = 1;

    public static final String EXTRA_FROM_NOTIFICATION = "from-notification";

    private ActionBar actionBar;
    private Menu actionBarMenu;

    private ImageView blason;
    private TextView name;
    private TextView trollStatus;
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

    private TextView trollInfo;
    private TextView technicalStatus;

    private LinearLayout carReg;
    private LinearLayout carAtt;
    private LinearLayout carEsq;
    private LinearLayout carDeg;
    private LinearLayout carArm;
    private LinearLayout vue;
    private LinearLayout rm;
    private LinearLayout mm;

    private ProfileProxy profileProxy;

    public ProfileProxy getProfileProxy() {
        if (profileProxy == null) {
            profileProxy = new ProfileProxyV2();
        }
        return profileProxy;
    }

    protected void showToast(CharSequence message, Object... args) {
        MhDlaNotifierUtils.toast(getApplicationContext(), message, args);
    }

    ///////////////////////////////////
    //  ANDROID INTERACTION METHODS  //
    ///////////////////////////////////

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_with_cards);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);

        blason = (ImageView) findViewById(R.id.blason);
        name = (TextView) findViewById(R.id.name);
        trollStatus = (TextView) findViewById(R.id.troll_status);

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

        trollInfo = (TextView) findViewById(R.id.troll_info);
        technicalStatus = (TextView) findViewById(R.id.technical_status);

        carReg = (LinearLayout) findViewById(R.id.REG);
        carAtt = (LinearLayout) findViewById(R.id.ATT);
        carEsq = (LinearLayout) findViewById(R.id.ESQ);
        carDeg = (LinearLayout) findViewById(R.id.DEG);
        carArm = (LinearLayout) findViewById(R.id.ARM);
        vue = (LinearLayout) findViewById(R.id.VUE);

        rm = (LinearLayout) findViewById(R.id.RM);
        mm = (LinearLayout) findViewById(R.id.MM);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Because of an unexpected NPE
        trollInfo = (TextView) findViewById(R.id.troll_info);

        loadTroll();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        Log.d(TAG, "Clear notifications");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        this.actionBarMenu = menu;

        boolean displayTechnicalStuff = shouldDisplayTechnicalStuff();
        this.actionBarMenu.findItem(R.id.action_technical_stuff).setVisible(displayTechnicalStuff);

        return super.onCreateOptionsMenu(menu);
    }

    protected boolean shouldDisplayTechnicalStuff() {
//        try {
//            Troll troll = readTrollWithoutUpdate();
//            return "104259".equals(troll.getNumero());
//        } catch (Exception eee) {
//            // Impossible de charger le troll
//        }
//        return false;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_qr_code_market:
                showQRCode();
                return true;
            case R.id.action_credits:
                showCredits();
                return true;
            case R.id.action_refresh:
                manualRefresh();
                return true;
            case R.id.action_register:
                startRegister(null);
                return true;
            case R.id.action_preferences:
                Intent preferencesIntent = new Intent(MhDlaNotifierUI.this, MhPreferencesActivity.class);
                startActivityForResult(preferencesIntent, PREFERENCES);
                return true;
            case R.id.action_play:
                onPlayButtonClicked();
                return true;
            case R.id.action_displayAlarms:
                scheduleAlarms(true);
                return true;
            case R.id.action_displayRequests:
                showLastRequests();
                return true;
            case R.id.action_technical_stuff:
                showTechnicalStuff();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void showCredits() {

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.credits, null);

        String title = getString(R.string.app_name) + " - Version " + MhDlaNotifierUtils.readVersion(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setIcon(R.drawable.trarnoll_square_transparent)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.create().show();
    }

    protected void showTechnicalStuff() {

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.technical_stuff, null);

        String versionString = String.format("Version : %s", MhDlaNotifierUtils.readVersionAndCode(this));
        String encodingString = String.format("Encoding : %s", System.getProperty("file.encoding"));
        String charsetString = String.format("Charset : %s", Charset.defaultCharset());
        String localeString = String.format("Locale : %s", Locale.getDefault());
        String trollString;
        try {
            Troll troll = readTrollWithoutUpdate();
            if (troll == null) {
                trollString = "null";
            } else {
                trollString = "Profil : " + troll.toString();
            }
        } catch (Exception eee) {
            trollString = eee.getMessage();
        }

        TextView version = (TextView) view.findViewById(R.id.technical_version);
        version.setText(versionString);

        TextView encoding = (TextView) view.findViewById(R.id.technical_encoding);
        encoding.setText(encodingString);

        TextView charset = (TextView) view.findViewById(R.id.technical_charset);
        charset.setText(charsetString);

        TextView locale = (TextView) view.findViewById(R.id.technical_locale);
        locale.setText(localeString);

        TextView troll = (TextView) view.findViewById(R.id.technical_troll);
        troll.setText(trollString);

        final ClipData clip = ClipData.newPlainText(
                "Données techniques de votre troll",
                Joiner.on("\n").join(versionString, encodingString, charsetString, localeString, trollString));

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setIcon(R.drawable.trarnoll_square_transparent)
                .setTitle(getString(R.string.technical_stuff))
                .setView(view)
                .setNeutralButton(R.string.copy_to_clipboard, new Dialog.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {

                        ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setPrimaryClip(clip);

                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.create().show();
    }

    protected void showQRCode() {

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.qr_code, null);

        String title = getString(R.string.app_name) + " - Version " + MhDlaNotifierUtils.readVersion(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setIcon(R.drawable.trarnoll_square_transparent)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.create().show();
    }

    protected void showLastRequests() {

        String title = getString(R.string.app_name);

        Set<String> trollIds = getProfileProxy().getTrollIds(this);
        if (trollIds != null && !trollIds.isEmpty()) {
            List<MhSpRequest> requests = PublicScriptsProxy.
                    listLatestRequestsSince(this, trollIds.iterator().next(), 2);

            Iterable<String> requestsAsString = Iterables.transform(requests, new Function<MhSpRequest, String>() {
                @Override
                public String apply(MhSpRequest mhSpRequest) {
                    String result = "";
                    if (mhSpRequest.lessThan24Hours()) {
                        result += "* ";
                    }
                    result += MhDlaNotifierUtils.formatDLAForDisplayShort(MhDlaNotifierUI.this, mhSpRequest.getDate())
                            + " - " + mhSpRequest.getScript().name()
                            + " - " + mhSpRequest.getStatus()
                            + " - " + mhSpRequest.getDuration() + "ms";
                    return result;
                }
            });

            String message = "Les appels marqués par une * ont moins de 24H :\n\n"
                + Joiner.on("\n").join(requestsAsString);

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setIcon(R.drawable.trarnoll_square_transparent)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
            textView.setTextSize(12);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REGISTER && resultCode == RESULT_OK) {
            pushTrollToUI(new Troll(), true);
            startUpdate(UpdateRequestType.FULL, "Récupération du profil");
        }
        if (requestCode == PREFERENCES) {
            scheduleAlarms(false);
        }
    }

    public void onPlayButtonClicked() {
        Uri uri = MhDlaNotifierConstants.MH_PLAY_URI;
        PreferencesHolder preferences = PreferencesHolder.load(this);
        if (preferences.useSmartphoneInterface) {
            uri = MhDlaNotifierConstants.MH_PLAY_SMARTPHONE_URI;
        }
        Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(webIntent);
    }


    //////////////////////
    //  NOT UI METHODS  //
    //////////////////////

    protected abstract Troll readTrollWithoutUpdate() throws Exception;

    protected abstract void loadTroll();

    protected abstract void scheduleAlarms(boolean displayToast);

    protected abstract void manualRefresh();

    protected abstract void startUpdate(UpdateRequestType updateType, String toast);

    protected abstract Date getLastUpdate();

    //////////////////
    //  UI METHODS  //
    //////////////////

    protected MenuItem getRefreshMenuItem() {
        MenuItem result = null;
        if (actionBarMenu != null) {
            result = actionBarMenu.findItem(R.id.action_refresh);
            Log.d(TAG, "Menu item from actionBarMenu: " + result);
        }
        return result;
    }

    protected void updateStarted(String message) {
        setTechnicalStatus(message);
        MenuItem refreshMenuItem0 = getRefreshMenuItem();
        if (refreshMenuItem0 != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                refreshMenuItem0.setActionView(R.layout.action_progressbar);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                refreshMenuItem0.expandActionView();
            }
        }
    }

    protected void updateFinished() {
        MenuItem refreshMenuItem0 = getRefreshMenuItem();
        if (refreshMenuItem0 != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                refreshMenuItem0.collapseActionView();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                refreshMenuItem0.setActionView(null);
            }
        }
    }

    protected void startRegister(String toast) {
        if (!Strings.isNullOrEmpty(toast)) {
            showToast(toast);
        }
        Log.i(TAG, "Login or password are missing, calling RegisterActivity");
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivityForResult(intent, REGISTER);
    }

    private void internalSetTechnicalStatus(CharSequence message) {
        if (this.technicalStatus == null) {
            this.technicalStatus = (TextView) findViewById(R.id.technical_status);
        }
        if (this.technicalStatus == null) {
            Log.w(TAG, "Cannot get technicalStatus instance");
        } else {
            this.technicalStatus.setText(message);
        }
    }

    protected void setTechnicalStatus(CharSequence message) {
        setTechnicalStatus(message, 60);
    }

    protected void setTechnicalStatus(CharSequence message, int duration) {
        internalSetTechnicalStatus(MoreObjects.firstNonNull(message, ""));
        if (duration > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    clearTechnicalStatus();
                }
            }, duration * 1000);
        }
    }

    protected void setTechnicalStatusError(CharSequence error) {
        if (error != null) {
            SpannableString spannable = new SpannableString(error);
            colorize(spannable, getResources().getColor(R.color.error));
            setTechnicalStatus(spannable, 60);
        }
    }

    protected void clearTechnicalStatus() {
        String status = "";
        Date lastUpdate = getLastUpdate();
        if (lastUpdate != null) {
            status = "Dernière m-à-j : ";
            if ((System.currentTimeMillis() - lastUpdate.getTime()) > (24l * 60l * 60l * 1000l)) {
                status += MhDlaNotifierUtils.formatDay(lastUpdate) + " - ";
            }
            status += MhDlaNotifierUtils.formatHour(lastUpdate);
        }
        internalSetTechnicalStatus(status);
    }

    protected void pushTrollToUI(Troll troll, boolean updateToFollow) {

        Preconditions.checkNotNull(troll, "Troll cannot be null");

        Log.i(TAG, "Now rendering troll: " + troll);

        this.actionBar.setTitle(troll.getNom());

        this.name.setText(troll.getNom());
        this.numero.setText("N° " + troll.getNumero());
        this.race.setText(String.format("%s (%d)", troll.getRace(), troll.getNival()));

        SpannableString trollInfo = new SpannableString("");
        if (troll.getPvVariation() < 0) {
            String messageFormat = getText(R.string.pv_loss_title).toString();
            String message = String.format(messageFormat, Math.abs(troll.getPvVariation()));
            trollInfo = new SpannableString(message);
            int pvWarnColor = getResources().getColor(R.color.pv_warn);
            colorize(trollInfo, pvWarnColor);
        } else if (troll.getPvVariation() > 0 && troll.getPvVariation() < troll.getPv()) {
            String messageFormat = getText(R.string.pv_gain_title).toString();
            String message = String.format(messageFormat, troll.getPvVariation());
            trollInfo = new SpannableString(message);
            int pvGainColor = getResources().getColor(R.color.pv_gain);
            colorize(trollInfo, pvGainColor);
        } else {
            if (troll.getDateInscription() != null) {
                Calendar now = Calendar.getInstance();
                Calendar inscription = Calendar.getInstance();
                inscription.setTime(troll.getDateInscription());
                if (now.get(Calendar.MONTH) == inscription.get(Calendar.MONTH) && now.get(Calendar.DAY_OF_MONTH) == inscription.get(Calendar.DAY_OF_MONTH)) {
                    trollInfo = new SpannableString("Joyeux anniversaire ;)");
                }
            }
        }
        this.trollInfo.setText(trollInfo);

        Set<String> statuses = new LinkedHashSet<String>();
        if (troll.isATerre()) {
            statuses.add("[À terre]");
        }
        if (troll.isCamou()) {
            statuses.add("[Camou]");
        }
        if (troll.isInvisible()) {
            statuses.add("[Invi]");
        }
        if (troll.isIntangible()) {
            statuses.add("[Intangible]");
        }
        if (troll.isImmobile()) {
            statuses.add("[Englué]");
        }
        if (troll.isEnCourse()) {
            statuses.add("[Course]");
        }
        if (troll.isLevitation()) {
            statuses.add("[Lévitation]");
        }

        String status = Joiner.on(" ").join(statuses);
        this.trollStatus.setText(status);

        String kdString = String.format("%d / %d", troll.getNbKills(), troll.getNbMorts());
        int kdStringLength = kdString.length();
        if (troll.getNbMorts() > 0) {
            kdString += String.format(" (ratio: %.1f) ", Integer.valueOf(troll.getNbKills()).doubleValue() / Integer.valueOf(troll.getNbMorts()).doubleValue());
        }
        SpannableString kdSpannable = new SpannableString(kdString);
        if (kdString.length() > kdStringLength) {
            kdSpannable.setSpan(new StyleSpan(Typeface.ITALIC), kdStringLength, kdString.length(), 0);
        }
        kd.setText(kdSpannable);

        int pvMax = Trolls.GET_MAX_PV.apply(troll);

        int additionalPvs = pvMax - troll.getPvMaxCar();
        String pvMaxString = "" + troll.getPvMaxCar();
        if (additionalPvs > 0) {
            pvMaxString += String.format("+%d", additionalPvs);
        }
        String pvText = String.format("%s / %s", troll.getPv(), pvMaxString);
        SpannableString pvSpannable = new SpannableString(pvText);
        try {
            int pvLength = 1;
            if (troll.getPv() >= 100) {
                pvLength = 3;
            } else if (troll.getPv() >= 10) {
                pvLength = 2;
            }

            if (troll.getPv() <= (pvMax * MhDlaNotifierConstants.PV_ALARM_THRESHOLD / 100)) {
                pvSpannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.pv_alarm)), 0, pvLength, 0);
                pvSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, pvLength, 0);
            } else if (troll.getPv() < pvMax) { // At least 1 PV missing
                pvSpannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.pv_warn)), 0, pvLength, 0);
                if (troll.getPv() <= (pvMax * MhDlaNotifierConstants.PV_WARM_THRESHOLD / 100)) {
                    pvSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, pvLength, 0);
                }
            }
        } catch (NumberFormatException nfe) {
            // Nothing to do, ignore it
        }
        pvs.setText(pvSpannable);

        String fatigueString = "" + troll.getFatigue();
        SpannableString fatigueSpannable;
        if (Race.Kastar.equals(troll.getRace())) {
            int fatigueLength = fatigueString.length();
            fatigueString += String.format(" (AM: 1 PV = %d') ", Trolls.GET_DLA_GAIN_BY_PV.apply(troll.getFatigue()));
            fatigueSpannable = new SpannableString(fatigueString);
            fatigueSpannable.setSpan(new StyleSpan(Typeface.ITALIC), fatigueLength, fatigueString.length(), 0);
        } else {
            fatigueSpannable = new SpannableString(fatigueString);
        }
        fatigue.setText(fatigueSpannable);

        position.setText(
                String.format("X=%d | Y=%d | N=%d",
                        troll.getPosX(), troll.getPosY(), troll.getPosN()));

        Date currentDla = troll.getDla();
        int pa = troll.getPa();

        SpannableString dlaSpannable = new SpannableString(MhDlaNotifierUtils.formatDLAForDisplay(this, currentDla));
        SpannableString paSpannable = new SpannableString("" + pa); // Leave ""+ as integer is considered as an Android id

        stylize(dlaSpannable, Typeface.BOLD);

        Date now = new Date();
        int dlaExpiredColor = getResources().getColor(R.color.dla_expired);
        int dlaToExpireColor = getResources().getColor(R.color.dla_to_expire);

        if (currentDla != null) {
            if (now.after(currentDla)) {
                colorize(dlaSpannable, dlaExpiredColor);
            } else {
                PreferencesHolder preferences = PreferencesHolder.load(this);
                Date dlaMinusNDMin = MhDlaNotifierUtils.substractMinutes(currentDla, preferences.notificationDelay);
                if (now.after(dlaMinusNDMin)) {
                    colorize(dlaSpannable, dlaToExpireColor);
                    if (pa > 0) {
                        if (!updateToFollow) {
                            showToast("Il vous reste des PA à jouer !");
                        }
                        colorize(paSpannable, dlaToExpireColor);
                        stylize(paSpannable, Typeface.BOLD);
                    }
                }
            }
        }

        dla.setText(dlaSpannable);
        remainingPAs.setText(paSpannable);

        Integer nextDlaDuration = Trolls.GET_NEXT_DLA_DURATION.apply(troll);
        dla_duration.setText(MhDlaNotifierUtils.prettyPrintDuration(this, nextDlaDuration));

        Date nextDla = Trolls.GET_NEXT_DLA.apply(troll);
        String nextDlaText = MhDlaNotifierUtils.formatDLAForDisplay(this, nextDla);
        SpannableString nextDlaSpannable = new SpannableString(nextDlaText);

        if (nextDla != null) {
            if (now.after(nextDla)) {
                colorize(nextDlaSpannable, dlaExpiredColor);
            } else {
                PreferencesHolder preferences = PreferencesHolder.load(this);
                Date dlaMinusNDMin = MhDlaNotifierUtils.substractMinutes(nextDla, preferences.notificationDelay);
                if (now.after(dlaMinusNDMin)) {
                    colorize(nextDlaSpannable, dlaToExpireColor);
                }
            }
        }

        next_dla.setText(nextDlaSpannable);

        pushCar(carReg, 3, troll.getRegenerationCar(), troll.getRegenerationBmp(), troll.getRegenerationBmm());
        pushCar(carAtt, 6, troll.getAttaqueCar(), troll.getAttaqueBmp(), troll.getAttaqueBmm());
        pushCar(carEsq, 6, troll.getEsquiveCar(), troll.getEsquiveBmp(), troll.getEsquiveBmm());
        pushCar(carDeg, 3, troll.getDegatsCar(), troll.getDegatsBmp(), troll.getDegatsBmm());
        pushCar(carArm, 3, troll.getArmureCar(), troll.getArmureBmp(), troll.getArmureBmm());

        pushVue(vue, troll.getVueCar(), troll.getVueBmp(), troll.getVueBmm());
        pushM(rm, troll.getRmCar(), troll.getRmBmp(), troll.getRmBmm());
        pushM(mm, troll.getMmCar(), troll.getMmBmp(), troll.getMmBmm());

        blason.setImageResource(R.drawable.loading);
        new LoadBlasonTask().execute(troll.getBlason());

        new LoadGuildeTask().execute(troll.getGuilde());

        new UpdateWidgetsTask().execute(troll);
    }

    protected double d(int nb) {
        return Integer.valueOf(nb).doubleValue();
    }

    protected void pushCar(LinearLayout linearLayout, int dSize, int car, int bmp, int bmm) {
        double avg = d(car) * d(dSize + 1) / 2d + d(bmp) + d(bmm);

        TextView carTV = (TextView) linearLayout.getChildAt(0);
        carTV.setText(String.format("%dD%d", car, dSize));

        TextView bmpTV = (TextView) linearLayout.getChildAt(1);
        bmpTV.setText(String.format("%d", bmp));

        TextView bmmTV = (TextView) linearLayout.getChildAt(2);
        bmmTV.setText(String.format("%d", bmm));

        TextView avgTV = (TextView) linearLayout.getChildAt(3);
        String avgText = String.format("%.1f", avg);
        if (avgText.endsWith(".0") || avgText.endsWith(",0")) {
            avgText = avgText.substring(0, avgText.length() - 2);
        }
        avgTV.setText(avgText);
    }

    protected void pushVue(LinearLayout linearLayout, int car, int bmp, int bmm) {
        TextView carTV = (TextView) linearLayout.getChildAt(0);
        carTV.setText(String.format("%d", car));

        TextView bmpTV = (TextView) linearLayout.getChildAt(1);
        bmpTV.setText(String.format("%d", bmp));

        TextView bmmTV = (TextView) linearLayout.getChildAt(2);
        bmmTV.setText(String.format("%d", bmm));

        TextView totalTV = (TextView) linearLayout.getChildAt(3);
        String totalText = String.format("%d", car + bmp + bmm);
        totalTV.setText(totalText);
    }

    protected void pushM(LinearLayout linearLayout, int car, int bmp, int bmm) {
        TextView carTV = (TextView) linearLayout.getChildAt(0);
        carTV.setText(String.format("%d", car));

        int bm = bmp + bmm;
        TextView bmTV = (TextView) linearLayout.getChildAt(1);
        bmTV.setText(String.format("%s%d%s", bm > 0 ? "+" : "", bm, "%"));

        TextView totalTV = (TextView) linearLayout.getChildAt(2);

        double percent = (100d + bm) / 100d;
        int total = (int) Math.round(car * percent);
        String totalText = String.format("%d", total);
        totalTV.setText(totalText);
    }

    private void colorize(SpannableString spannable, int color) {
        spannable.setSpan(new ForegroundColorSpan(color), 0, spannable.length(), 0);
    }

    private void stylize(SpannableString spannable, int typeface) {
        spannable.setSpan(new StyleSpan(typeface), 0, spannable.length(), 0);
    }

    protected void updateBlason(Bitmap blason) {
        if (blason == null) {
            this.blason.setImageResource(R.drawable.pas_de_blason);
            actionBar.setIcon(R.drawable.trarnoll_square_transparent_128);
        } else {
            this.blason.setImageBitmap(blason);
            actionBar.setIcon(new BitmapDrawable(getResources(), blason));
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

    private class UpdateWidgetsTask extends AsyncTask<Troll, Void, Void> {

        @Override
        protected Void doInBackground(Troll... params) {

            try {
                AppWidgetManager widgetManager = AppWidgetManager.getInstance(MhDlaNotifierUI.this);

                ComponentName componentName = new ComponentName(MhDlaNotifierUI.this, HomeScreenWidget.class);
                int[] appWidgetIds = widgetManager.getAppWidgetIds(componentName);

                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    Troll troll = params[0];
                    String dlaText = Trolls.getWidgetDlaTextFunction(MhDlaNotifierUI.this).apply(troll);
                    Bitmap blasonImage = MhDlaNotifierUtils.loadBlasonForWidget(troll.getBlason(), getCacheDir());

                    for (int appWidgetId : appWidgetIds) {

                        RemoteViews views = new RemoteViews(getPackageName(), R.layout.home_screen_widget);
                        views.setTextViewText(R.id.widgetDla, dlaText);

                        if (blasonImage == null) {
                            views.setImageViewResource(R.id.widgetImage, R.drawable.trarnoll_square_transparent_128);
                        } else {
                            views.setImageViewBitmap(R.id.widgetImage, blasonImage);
                        }

                        // Tell the AppWidgetManager to perform an update on the current app widget
                        widgetManager.updateAppWidget(appWidgetId, views);
                    }
                }

            } catch (Exception eee) {
                Log.e(TAG, "Unable to update widget(s)", eee);
            }
            return null;
        }

    }

}
