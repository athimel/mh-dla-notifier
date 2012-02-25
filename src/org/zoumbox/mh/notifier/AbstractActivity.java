package org.zoumbox.mh.notifier;

import android.app.Activity;
import android.widget.Toast;

public abstract class AbstractActivity extends Activity {

    protected void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

}
