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
package org.zoumbox.mh.notifier;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.common.base.Strings;
import org.zoumbox.mh.notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh.notifier.profile.ProfileProxy;
import org.zoumbox.mh.notifier.sp.QuotaExceededException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Activité principale
 */
public class Main extends AbstractActivity {

    public static final int REGISTER = 0;
    protected static final int CREDIT_DIALOG = 0;

    public static final String INTPUT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DISPLAY_DATE_FORMAT = "dd MMM yyyy - HH:mm:ss";

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
        // TODO AThimel 24/02/2012 Get the DLA from MH

        try {
            Map<String, String> properties = ProfileProxy.fetchProperties(this,
                    "nom", "race", "niveau", "pv", "pvMax", "posX", "posY", "posN", "dla", "paRestant", "blason", "nbKills", "nbMorts");
            String blasonUri = properties.get("blason");
            Bitmap blason = loadBlason(blasonUri);
            if (blason != null) {
                this.blason.setImageBitmap(blason);
            }
            String nom = String.format("%s - %s (%s)", properties.get("nom"), properties.get("race"), properties.get("niveau"));
            name.setText(nom);
            kd.setText(String.format("%s / %s", properties.get("nbKills"), properties.get("nbMorts")));
            pvs.setText(String.format("%s / %s", properties.get("pv"), properties.get("pvMax")));
            position.setText(String.format("X=%s | Y=%s | N=%s", properties.get("posX"), properties.get("posY"), properties.get("posN")));
            dla.setText(formatDate(properties.get("dla")));
            remainingPAs.setText(properties.get("paRestant"));
        } catch (MissingLoginPasswordException mlpe) {
            showToast("Vous devez saisir vos identifiants");
            System.out.println("Login or password are missing, calling Register");
            Intent intent = new Intent(this, Register.class);
            startActivityForResult(intent, REGISTER);
        } catch (QuotaExceededException e) {
            e.printStackTrace();
        }

    }

    protected Bitmap loadBlason(String blasonUrl) {

        Bitmap result = null;
        if (!Strings.isNullOrEmpty(blasonUrl)) {
            String localFilePath = md5(blasonUrl);
            System.out.println("localFilePath: " + localFilePath);
//            File filesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File filesDir = getExternalCacheDir();
            System.out.println(filesDir);
            File localFile = new File(filesDir, localFilePath);
            System.out.println("localFile: " + localFile);
            if (!localFile.exists()) {

                System.out.println("Not existing, fetching from " + blasonUrl);
                BufferedInputStream bis = null;
                try {
                    URL url = new URL(blasonUrl);
                    URLConnection conn = url.openConnection();
                    conn.connect();
                    bis = new BufferedInputStream(conn.getInputStream());
                    result = BitmapFactory.decodeStream(bis);

                } catch (Exception eee) {
                    eee.printStackTrace();
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                }

//                if (result != null) {
//                    System.out.println("Save fetched result to " + localFile);
//                    FileOutputStream fos = null;
//                    try {
//                        fos = new FileOutputStream(localFile);
//
//                        result.compress(Bitmap.CompressFormat.PNG, 90, fos);
//
//                    } catch (Exception eee) {
//                        eee.printStackTrace();
//                        return null;
//                    } finally {
//                        if (fos != null) {
//                            try {
//                                fos.close();
//                            } catch (IOException ioe) {
//                                ioe.printStackTrace();
//                            }
//                        }
//                    }
//                }
            } else {

                System.out.println("Existing, loading from cache");
                BufferedInputStream bis = null;
                try {
                    bis = new BufferedInputStream(new FileInputStream(localFile));

                    result = BitmapFactory.decodeStream(bis);

                    bis.close();
                } catch (Exception eee) {
                    eee.printStackTrace();
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                }
            }
        }

        return result;
    }

    protected String formatDate(String input) {
        DateFormat inputDF = new SimpleDateFormat(INTPUT_DATE_FORMAT);
        DateFormat outputDF = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.FRENCH);

        try {
            Date date = inputDF.parse(input);
            String result = outputDF.format(date);
            return result;
        } catch (ParseException e) {
            e.printStackTrace();
            return "n/c";
        }
    }

}
