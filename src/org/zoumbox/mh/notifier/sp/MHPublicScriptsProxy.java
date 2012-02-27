package org.zoumbox.mh.notifier.sp;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class MHPublicScriptsProxy {

    protected static String query(String url) {

        // TODO AThimel 27/02/2012 Implement HTTP GET
        if (url.contains("SP_Profil2.php")) {
            return "104259;57;-75;-41;85;80;6;2012-02-25 01:22:55;8;4;13;4;4;6;359;356;0;3;0;0;0;0;0;585;0;1;0";
        } else if (url.contains("SP_Profil3.php")) {
            return "104259;DevelZimZoum;57;-75;-41;6;2012-02-25 01:22:55;3;0;0;0;2;22;88;6042";
        }
        return "104259;DevelZimZoum;Kastar;18;2011-01-21 14:07:48;;http://zoumbox.org/mh/DevelZimZoumMH.png;17;117;9;1900;20;0";
    }

    protected static int checkQuota(ScriptCategory category) {
        // TODO AThimel 27/02/2012 Use database
        return 12;
    }

    protected static void saveFetch(PublicScript script) {

        // TODO AThimel 27/02/2012 Save to database

    }

    public static Map<String, String> fetch(PublicScript script, String trollNumber, String trollPassword, boolean force) throws QuotaExceededException {

        ScriptCategory category = script.category;
        int count = checkQuota(category);
        if (count >= category.quota) {
            System.out.println("Quota is exceeded for category '" + category + "': " + count + "/" + category.quota + ". Force usage ? " + force);
            if (!force) {
                throw new QuotaExceededException(category, count);
            }
        }

        String url = String.format(script.url, trollNumber, trollPassword);
        String rawResult = query(url);
        saveFetch(script);

        Map<String, String> result = Maps.newLinkedHashMap();

        Iterable<String> iterable = Splitter.on(";").split(rawResult);
        List<String> data = Lists.newArrayList(iterable);

        for (int i=0; i<data.size(); i++) {
            result.put(script.properties.get(i), data.get(i));
        }

        return result;
    }

}
