package org.zoumbox.mh_dla_notifier;

import android.util.Log;
import com.google.common.base.Strings;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class MhDlaNotifierUtils {

    private static final String TAG = "MhDlaNotifier-" + MhDlaNotifierUtils.class.getSimpleName();

    /**
     * Encodes a raw byte[] to an hexadecimal String
     *
     * @param raw the byte[] to encode
     * @return the hexadecimal encoded text
     */
    public static String toHexadecimal(byte[] raw) {
        String result = null;
        if (raw != null) {
            result = new String(Hex.encodeHex(raw));
        }
        return result;
    }

    public static String md5(String text) {
        if (!Strings.isNullOrEmpty(text)) {
            try {
                // Create MD5 Hash
                MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                digest.update(text.getBytes());
                byte messageDigest[] = digest.digest();

                String result = toHexadecimal(messageDigest);
                return result;

            } catch (NoSuchAlgorithmException nsae) {
                Log.e(TAG, "Algo MD5 non trouvé", nsae);
            }
        }
        return "";
    }

}
