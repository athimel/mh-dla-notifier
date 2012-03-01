package org.zoumbox.mh_dla_notifier;

import android.util.Log;
import com.google.common.base.Strings;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class MhDlaNotifierUtils {

    private static final String TAG = "MhDlaNotifier-" + MhDlaNotifierUtils.class.getSimpleName();

    public static String md5(String text) {
        if (!Strings.isNullOrEmpty(text)) {
            try {
                // Create MD5 Hash
                MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                digest.update(text.getBytes());
                byte messageDigest[] = digest.digest();

                // Create Hex String
                StringBuffer hexString = new StringBuffer();
                for (int i = 0; i < messageDigest.length; i++)
                    hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
                return hexString.toString();

            } catch (NoSuchAlgorithmException nsae) {
                Log.e(TAG, "Algo MD5 non trouvé", nsae);
            }
        }
        return "";
    }

}
