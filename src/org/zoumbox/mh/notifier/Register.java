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
import android.view.View;
import android.widget.EditText;

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
        password = (EditText) findViewById(R.id.password);
    }

    public void onSaveButtonClicked(View target) {

        String trollNumber = troll.getText().toString();
        String trollPassword = password.getText().toString();

        showToast(trollNumber + "/" + trollPassword);

        // TODO AThimel 24/02/2012 Update save troll/password

        loadDLAs();
    }

    private void loadDLAs() {


    }


}
