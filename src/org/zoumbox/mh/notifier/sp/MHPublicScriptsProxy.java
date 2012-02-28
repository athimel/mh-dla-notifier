package org.zoumbox.mh.notifier.sp;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class MHPublicScriptsProxy {

//    protected static String query(String url) {
//
//        String responseContent = "";
//        BufferedReader in = null;
//        try {
//            HttpClient client = new DefaultHttpClient();
//            HttpGet request = new HttpGet(url);
//            HttpResponse response = client.execute(request);
//            InputStream content = response.getEntity().getContent();
//            in = new BufferedReader(new InputStreamReader(content));
//            String line;
//            while ((line = in.readLine()) != null) {
//                responseContent += line;
//            }
//            in.close();
//        } catch (Exception eee) {
//            eee.printStackTrace();
//        } finally {
//            if (in != null) {
//                try {
//                    in.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        System.out.println("'" + responseContent + "'");
//        return responseContent;
//

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
        return 3;
    }

    protected static void saveFetch(PublicScript script) {

        // TODO AThimel 27/02/2012 Save to database

    }

    public static Map<String, String> fetch(PublicScript script, String trollNumber, String trollPassword, boolean force) throws QuotaExceededException {

        System.out.println("Fetch " + script.name() + " for troll " + trollNumber);
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
