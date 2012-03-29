package org.zoumbox.mh_dla_notifier;

import android.app.Activity;
import android.widget.Toast;

public abstract class AbstractActivity extends Activity {

    protected void showToast(CharSequence message) {
        MhDlaNotifierUtils.toast(getApplicationContext(), message);
    }

}
