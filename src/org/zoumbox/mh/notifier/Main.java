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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.zoumbox.mh.notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh.notifier.profile.ProfileProxy;
import org.zoumbox.mh.notifier.sp.QuotaExceededException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Activité principale
 */
public class Main extends AbstractActivity {

    public static final int REGISTER = 0;
    protected static final int CREDIT_DIALOG = 0;

    public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";

    protected TextView name;
    protected TextView dla;
    protected TextView remainingPAs;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        name = (TextView) findViewById(R.id.name);
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

    protected void loadDLAs() {
        // TODO AThimel 24/02/2012 Get the DLA from MH

        SharedPreferences sharedPreferences = getSharedPreferences("org.zoumbox.mh.dla.notifier.preferences", 0);
        ProfileProxy pp = new ProfileProxy();
        try {
            Map<String,String> properties = pp.fetchProperties(sharedPreferences, "nom", "dla", "paRestant");
            name.setText(properties.get("nom"));
            dla.setText(properties.get("dla"));
            remainingPAs.setText(properties.get("paRestant"));
        } catch (MissingLoginPasswordException mlpe) {
            // TODO AThimel 27/02/2012 Go to Register
            mlpe.printStackTrace();
        } catch (QuotaExceededException e) {
            e.printStackTrace();
        }

    }


}
