package org.zoumbox.mh_dla_notifier.profile;

import java.util.Date;
import java.util.Set;

import org.zoumbox.mh_dla_notifier.sp.NetworkUnavailableException;
import org.zoumbox.mh_dla_notifier.sp.PublicScriptException;
import org.zoumbox.mh_dla_notifier.sp.QuotaExceededException;
import org.zoumbox.mh_dla_notifier.sp.ScriptCategory;
import org.zoumbox.mh_dla_notifier.troll.Troll;

import com.google.common.base.Function;

import android.content.Context;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public interface ProfileProxy {

    public static final Function<ScriptCategory, Integer> GET_USABLE_QUOTA = new Function<ScriptCategory, Integer>() {
        @Override
        public Integer apply(ScriptCategory input) {
            if (input == null) {
                return 1;
            }
            int dailyQuota = input.getQuota();
            int result = dailyQuota / 3; //FIXME AThimel 29/03/2012 divide by 3 for the moment to avoid mistakes
            return result;
        }
    };

    Set<String> getTrollIds(Context context);

    boolean saveIdPassword(Context context, String trollId, String trollPassword);

    boolean areTrollIdentifiersUndefined(Context context);

    Troll fetchTrollWithoutUpdate(Context context, String trollId) throws MissingLoginPasswordException;

    Troll fetchTroll(Context context, String trollId, UpdateRequestType updateRequestType)
            throws QuotaExceededException, MissingLoginPasswordException, PublicScriptException,
            NetworkUnavailableException;

    String getLastUpdateResult(final Context context);

    Date getLastUpdateSuccess(Context context, String trollId);

    Troll refreshDLA(Context context, String trollId) throws MissingLoginPasswordException;

    Long getElapsedSinceLastRestartCheck(final Context context);

    Long getElapsedSinceLastUpdateSuccess(final Context context);

    void restartCheckDone(final Context context);
}
