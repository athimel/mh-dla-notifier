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

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.common.base.Strings;
import org.zoumbox.mh_dla_notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;
import org.zoumbox.mh_dla_notifier.sp.QuotaExceededException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Activité principale
 */
public class Main extends AbstractActivity {

    private static final String TAG = "MhDlaNotifier-" + Main.class.getSimpleName();

    public static final int REGISTER = 0;
    protected static final int CREDIT_DIALOG = 0;

    public static final String EXTRA_FROM_NOTIFICATION = "from-notification";

    protected ImageView blason;
    protected TextView name;
    protected TextView pvs;
    protected TextView kd;
    protected TextView position;
    protected TextView dla;
    protected TextView remainingPAs;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        blason = (ImageView) findViewById(R.id.blason);
        name = (TextView) findViewById(R.id.name);
        pvs = (TextView) findViewById(R.id.pvs);
        kd = (TextView) findViewById(R.id.kd);
        position = (TextView) findViewById(R.id.position);
        dla = (TextView) findViewById(R.id.dla_field);
        remainingPAs = (TextView) findViewById(R.id.pas);

        loadDLAs();
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
                Intent intent_market = new Intent(this, QRCodeMarket.class);
                startActivity(intent_market);
                return true;
            case R.id.credits:
                showDialog(CREDIT_DIALOG);
                return true;
            case R.id.register:
                Intent intent = new Intent(this, Register.class);
                startActivityForResult(intent, REGISTER);
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
        if (resultCode == RESULT_OK) {
            if (requestCode == REGISTER) {
                loadDLAs();
            }
        }
    }

    protected void loadDLAs() {
        
        Log.i(TAG, "Loading DLAs");

        try {
            Map<String, String> properties = ProfileProxy.fetchProperties(this,
                    "nom", "race", "niveau", "pv", "pvMax", "posX", "posY", "posN", ProfileProxy.PROPERTY_DLA, ProfileProxy.PROPERTY_PA_RESTANT, "blason", "nbKills", "nbMorts");
            String blasonUri = properties.get("blason");
            Bitmap blason = loadBlason(blasonUri);
            if (blason != null) {
                this.blason.setImageBitmap(blason);
            }
            String nom = String.format("%s (n°%s) - %s (%s)", properties.get("nom"), ProfileProxy.getTrollNumber(this), properties.get("race"), properties.get("niveau"));
            name.setText(nom);
            kd.setText(String.format("%s / %s", properties.get("nbKills"), properties.get("nbMorts")));
            pvs.setText(String.format("%s / %s", properties.get("pv"), properties.get("pvMax")));
            position.setText(String.format("X=%s | Y=%s | N=%s", properties.get("posX"), properties.get("posY"), properties.get("posN")));

            Date rawDla = MhDlaNotifierUtils.parseDate(properties.get(ProfileProxy.PROPERTY_DLA));
            if (rawDla != null) {
                SpannableString dlaSpannable = new SpannableString(MhDlaNotifierUtils.formatDate(rawDla));

                if (rawDla.getTime() < System.currentTimeMillis()) {
                    showToast("Vous pouvez réactiver votre DLA");
                    dlaSpannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, dlaSpannable.length(), 0);
                }
                dla.setText(dlaSpannable);
            }

            remainingPAs.setText(properties.get(ProfileProxy.PROPERTY_PA_RESTANT));

            registerDlaAlarm();
        } catch (MissingLoginPasswordException mlpe) {
            showToast("Vous devez saisir vos identifiants");
            Log.i(TAG, "Login or password are missing, calling Register");
            Intent intent = new Intent(this, Register.class);
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
                Intent intent = new Intent(this, Register.class);
                startActivityForResult(intent, REGISTER);
            }
        }

    }

    private void registerDlaAlarm() {

        boolean fromNotification = getIntent().getBooleanExtra(EXTRA_FROM_NOTIFICATION, false);
        Log.i(TAG, "From notification: " + fromNotification);
        if (fromNotification) {
            // Do not add a notification because, intent is coming from the notification itself

            Log.i(TAG, "Clear notifications");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        } else {
            Date dla = ProfileProxy.getDLA(this);
            if (dla != null && dla.getTime() > System.currentTimeMillis()) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dla);
                calendar.add(Calendar.MINUTE, -5);
                dla = calendar.getTime();

                Intent intent = new Intent(this, Receiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this.getApplicationContext(), 86956675, intent, 0);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, dla.getTime(), pendingIntent);
                //            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pendingIntent);

                Log.i(TAG, "Next alarm at " + dla);
                showToast("Prochaine alarme à " + MhDlaNotifierUtils.formatDate(dla)); // TODO AThimel 19/03/2012 Remove this
            } else {
                Log.w(TAG, "DLA null or expired: " + dla);
            }
        }
    }

    protected Bitmap loadBlason(String blasonUrl) {

        Bitmap result = null;
        if (!Strings.isNullOrEmpty(blasonUrl)) {
            String localFilePath = MhDlaNotifierUtils.md5(blasonUrl);
            Log.i(TAG, "localFilePath: " + localFilePath);
            File filesDir = getExternalCacheDir();
            File localFile = new File(filesDir, localFilePath);
            Log.i(TAG, "localFile: " + localFile);
            if (!localFile.exists()) {

                Log.i(TAG, "Not existing, fetching from " + blasonUrl);
                BufferedInputStream bis = null;
                try {
                    URL url = new URL(blasonUrl);
                    URLConnection conn = url.openConnection();
                    conn.connect();
                    bis = new BufferedInputStream(conn.getInputStream());
                    result = BitmapFactory.decodeStream(bis);

                } catch (Exception eee) {
                    Log.e(TAG, "Exception", eee);
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException ioe) {
                            Log.e(TAG, "IOException", ioe);
                        }
                    }
                }

                if (result != null) {
                    Log.i(TAG, "Save fetched result to " + localFile);
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(localFile);

                        result.compress(Bitmap.CompressFormat.PNG, 90, fos);

                    } catch (Exception eee) {
                        Log.e(TAG, "Exception", eee);
                        return null;
                    } finally {
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException ioe) {
                                Log.e(TAG, "IOException", ioe);
                            }
                        }
                    }
                }
            } else {

                Log.i(TAG, "Existing, loading from cache");
                BufferedInputStream bis = null;
                try {
                    bis = new BufferedInputStream(new FileInputStream(localFile));

                    result = BitmapFactory.decodeStream(bis);

                    bis.close();
                } catch (Exception eee) {
                    Log.e(TAG, "Exception", eee);
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException ioe) {
                            Log.e(TAG, "IOException", ioe);
                        }
                    }
                }
            }
        }

        return result;
    }

}
