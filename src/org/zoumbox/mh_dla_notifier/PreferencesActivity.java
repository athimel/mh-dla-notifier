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

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Activité pour l'affichage/saisie des préférences
 */
public class PreferencesActivity extends AbstractActivity {

    private static final String TAG = Constants.LOG_PREFIX + PreferencesActivity.class.getSimpleName();

    protected EditText notificationDelayEditText;
    protected CheckBox notifyWithoutPaCheckBox;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);

        notificationDelayEditText = (EditText) findViewById(R.id.notification_delay);
        notificationDelayEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        notifyWithoutPaCheckBox = (CheckBox) findViewById(R.id.notify_without_pa);

        PreferencesHolder preferences = PreferencesHolder.load(this);
        notificationDelayEditText.setText(String.format("%d", preferences.notificationDelay));
        notifyWithoutPaCheckBox.setChecked(preferences.notifyWithoutPA);
    }

    public void onSaveButtonClicked(View target) {

        showToast("Enregistrement des préférences");

        PreferencesHolder preferencesHolder = new PreferencesHolder();

        try {
            preferencesHolder.notificationDelay = Integer.parseInt(notificationDelayEditText.getText().toString());
        } catch (NumberFormatException nfe) {
            // Nothing to do
        }
        if (preferencesHolder.notificationDelay <= 0) {
            showToast("Le délai de notification doit être une valeur positive");
        } else {

            preferencesHolder.notifyWithoutPA = notifyWithoutPaCheckBox.isChecked();

            preferencesHolder.save(this);

            setResult(RESULT_OK);
            finish();
        }
    }

}
