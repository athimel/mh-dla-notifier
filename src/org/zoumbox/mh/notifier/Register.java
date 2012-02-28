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

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import com.google.common.base.Strings;
import org.zoumbox.mh.notifier.profile.ProfileProxy;

/**
 * Activité principale
 */
public class Register extends AbstractActivity {

    protected EditText troll;
    protected EditText password;

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

        String trollNumber = ProfileProxy.loadLogin(this);
        if (!Strings.isNullOrEmpty(trollNumber)) {
            troll.setText(trollNumber);
        }
    }

    public void onSaveButtonClicked(View target) {

        String trollNumber = troll.getText().toString();
        String trollPassword = password.getText().toString();

        String md5Password = md5(trollPassword);
        boolean result = ProfileProxy.saveLoginPassword(this, trollNumber, md5Password);

        if (result) {
            showToast("Identifiants enregistrés");
            setResult(RESULT_OK);
            finish();
        } else {
            System.out.println("Impossible d'enregistrer les identifiants");
        }

    }

}
