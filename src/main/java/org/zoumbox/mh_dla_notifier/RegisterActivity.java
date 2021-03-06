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

import java.util.Iterator;

import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.v2.ProfileProxyV2;

import com.google.common.base.Strings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Activité principale
 */
public class RegisterActivity extends Activity {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + RegisterActivity.class.getSimpleName();

    protected EditText troll;
    protected EditText password;

    protected Button saveButton;

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

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        troll = (EditText) findViewById(R.id.troll);
        troll.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        password = (EditText) findViewById(R.id.password);

        // TODO AThimel 20/08/13 Manage several trolls
        Iterator<String> iterator = getProfileProxy().getTrollIds(this).iterator();
        String trollId = null;
        if (iterator.hasNext()) {
            trollId = iterator.next();
        }
        if (!Strings.isNullOrEmpty(trollId)) {
            troll.setText(trollId);
        }

        saveButton = (Button) findViewById(R.id.save);
        saveButton.setEnabled(false);

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                // nothing to do
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                boolean enableSaveButton = false;
                if (charSequence != null && charSequence.length() >= 1) {
                    enableSaveButton = true;
                }
                saveButton.setEnabled(enableSaveButton);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // nothing to do
            }
        });

        displayHelp();
    }

    private void displayHelp() {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setTitle(R.string.password_help_title).setMessage(R.string.password_help_message);

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.setButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // Nothing to do
            }
        });
        dialog.show();
    }

    public void onSaveButtonClicked(View target) {

        String trollNumber = Strings.nullToEmpty(troll.getText().toString());
        String trollPassword = Strings.nullToEmpty(password.getText().toString());

        showToast("Enregistrement. Merci de patienter...");

        getProfileProxy().saveIdPassword(this, trollNumber, trollPassword);

        setResult(RESULT_OK);
        finish();
    }

}
