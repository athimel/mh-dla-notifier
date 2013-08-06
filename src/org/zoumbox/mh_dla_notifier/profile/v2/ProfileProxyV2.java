package org.zoumbox.mh_dla_notifier.profile.v2;

import java.util.Date;
import java.util.Set;

import org.zoumbox.mh_dla_notifier.profile.AbstractProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.UpdateRequestType;
import org.zoumbox.mh_dla_notifier.sp.NetworkUnavailableException;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;
import org.zoumbox.mh_dla_notifier.sp.QuotaExceededException;
import org.zoumbox.mh_dla_notifier.troll.Troll;

import android.content.Context;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class ProfileProxyV2 extends AbstractProfileProxy implements ProfileProxy {

    @Override
    public Set<String> getTrollIds(Context context) {
        return null;
    }

    @Override
    public boolean saveIdPassword(Context context, String trollId, String trollPassword) {
        return false;
    }

    @Override
    public boolean areTrollIdentifiersUndefined(Context context) {
        return false;
    }

    @Override
    public Troll fetchTroll(Context context, String trollId, UpdateRequestType updateRequestType) throws QuotaExceededException, MissingLoginPasswordException, PublicScriptException, NetworkUnavailableException {
        return null;
    }

    @Override
    public String getLastUpdateResult(Context context) {
        return null;
    }

    @Override
    public Date getLastUpdateSuccess(Context context, String trollId) {
        return null;
    }

    @Override
    public Troll refreshDLA(Context context, String trollId) throws MissingLoginPasswordException {
        return null;
    }

    @Override
    public Long getElapsedSinceLastRestartCheck(Context context) {
        return null;
    }

    @Override
    public Long getElapsedSinceLastUpdateSuccess(Context context) {
        return null;
    }

    @Override
    public void restartCheckDone(Context context) {
    }
}
