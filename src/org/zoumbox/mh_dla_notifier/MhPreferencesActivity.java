package org.zoumbox.mh_dla_notifier;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class MhPreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
