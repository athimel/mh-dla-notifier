package org.zoumbox.mh_dla_notifier.profile;

import org.zoumbox.mh_dla_notifier.MhDlaNotifierConstants;
import org.zoumbox.mh_dla_notifier.sp.NetworkUnavailableException;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;
import org.zoumbox.mh_dla_notifier.sp.QuotaExceededException;
import org.zoumbox.mh_dla_notifier.troll.Troll;

import android.content.Context;
import android.util.Log;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public abstract class AbstractProfileProxy implements ProfileProxy {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + AbstractProfileProxy.class.getSimpleName();

    public Troll fetchTrollWithoutUpdate(Context context, String trollId) throws MissingLoginPasswordException {
        try {
            Troll result = fetchTroll(context, trollId, UpdateRequestType.NONE);
            return result;
        } catch (QuotaExceededException e) {
            Log.e(TAG, "Should never happen", e);
            throw new RuntimeException("Should never happen", e);
        } catch (PublicScriptException e) {
            Log.e(TAG, "Should never happen", e);
            throw new RuntimeException("Should never happen", e);
        } catch (NetworkUnavailableException e) {
            Log.e(TAG, "Should never happen", e);
            throw new RuntimeException("Should never happen", e);
        }
    }

}
