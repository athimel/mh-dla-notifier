/*
 * #%L
 * MountyHall DLA Notifier
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2012 - 2014 Zoumbox.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package org.zoumbox.mh_dla_notifier.sp;

import android.util.Log;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.zoumbox.mh_dla_notifier.MhDlaNotifierConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

/**
 * @author Arno <arno@zoumbox.org>
 */
public class AsyncHttpFetcher {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + AsyncHttpFetcher.class.getSimpleName();

    private static class Content {
        String content;
        NetworkUnavailableException networkUnavailableException;
        PublicScriptException publicScriptException;
    }

    public static PublicScriptResponse doHttpGET(String url) throws NetworkUnavailableException, PublicScriptException {

        long start = System.currentTimeMillis();

        if (url.contains("?Numero=" + MhDlaNotifierConstants.MOCK_TROLL_ID)) {
            return PublicScriptsProxyMock.doMockHttpGET(url);
        }

        String responseContent = asyncHttpGET(url);

        long end = System.currentTimeMillis();
        PublicScriptResponse result = new PublicScriptResponse(responseContent, end - start);
        return result;
    }

    private static String syncHttpGET(String url) throws NetworkUnavailableException, PublicScriptException {
        String responseContent = "";
        BufferedReader in = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            InputStream content = response.getEntity().getContent();
            in = new BufferedReader(new InputStreamReader(content, Charsets.ISO_8859_1));
            String line;
            while ((line = in.readLine()) != null) {
                if (!Strings.isNullOrEmpty(responseContent)) {
                    responseContent += "\n";
                }
                responseContent += line;
            }
            in.close();
        } catch (UnknownHostException uhe) {
            Log.e(TAG, "Network error", uhe);
            throw new NetworkUnavailableException(uhe);
        } catch (Exception eee) {
            Log.e(TAG, "Exception", eee);
            throw new PublicScriptException(eee);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    Log.e(TAG, "IOException", ioe);
                }
            }
        }
        return responseContent;
    }

    private static String asyncHttpGET(final String url) throws NetworkUnavailableException, PublicScriptException {

        final Content handler = new Content();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String result = syncHttpGET(url);
                    handler.content = result;
                } catch (NetworkUnavailableException nue) {
                    handler.networkUnavailableException = nue;
                } catch (PublicScriptException pse) {
                    handler.publicScriptException = pse;
                }
            }
        };


        Log.i(TAG, "On passe en asynchrone pour appeler l'url " + url);

        Thread thread = new Thread(runnable);
        thread.start();
        try {
            thread.join(10000);
        } catch (InterruptedException e) {
            throw new NetworkUnavailableException(e);
        }

        Log.i(TAG, "Fin de l'appel asynchrone pour appeler l'url " + url);

        if (handler.networkUnavailableException != null) {
            throw new NetworkUnavailableException(handler.networkUnavailableException);
        }

        if (handler.publicScriptException != null) {
            throw new PublicScriptException(handler.publicScriptException);
        }

        String result = handler.content;
        if (Strings.isNullOrEmpty(result)) {
            Log.e(TAG, "Pas de contenu pour " + url);
        }

        return result;
    }

}
