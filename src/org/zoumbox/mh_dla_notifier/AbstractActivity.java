package org.zoumbox.mh_dla_notifier;

import android.app.Activity;
import android.widget.Toast;
import com.google.common.base.Strings;

public abstract class AbstractActivity extends Activity {

    protected void showToast(CharSequence message, Object ... args) {
        MhDlaNotifierUtils.toast(getApplicationContext(), message, args);
    }

}
