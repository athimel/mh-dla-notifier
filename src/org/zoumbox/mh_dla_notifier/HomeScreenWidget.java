package org.zoumbox.mh_dla_notifier;

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

import java.util.Set;

import org.zoumbox.mh_dla_notifier.profile.MissingLoginPasswordException;
import org.zoumbox.mh_dla_notifier.profile.ProfileProxy;
import org.zoumbox.mh_dla_notifier.profile.v2.ProfileProxyV2;
import org.zoumbox.mh_dla_notifier.troll.Troll;
import org.zoumbox.mh_dla_notifier.troll.Trolls;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * @author Arnaud Thimel <thimel@codelutin.com>
 */
public class HomeScreenWidget extends AppWidgetProvider {

    private static final String TAG = MhDlaNotifierConstants.LOG_PREFIX + HomeScreenWidget.class.getSimpleName();

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        String dlaText = null;
        Bitmap blason = null;
        if (appWidgetIds != null && appWidgetIds.length >= 1) {

            // Compute DLA
            ProfileProxyV2 profileProxy = new ProfileProxyV2();
            Pair<String, Bitmap> pair = getDlaText(context, profileProxy);

            dlaText = pair.left();
            blason = pair.right();
        }

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            Log.i(TAG, "onUpdate widget : " + appWidgetId);

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent startActivityIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.home_screen_widget);
            views.setOnClickPendingIntent(R.id.widgetLayout, startActivityIntent);
            views.setTextViewText(R.id.widgetDla, dlaText);

            if (blason == null) {
                views.setImageViewResource(R.id.widgetImage, R.drawable.trarnoll_square_transparent_128);
            } else {
                views.setImageViewBitmap(R.id.widgetImage, blason);
            }

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    protected Pair<String, Bitmap> getDlaText(Context context, ProfileProxy profileProxy) {

        Set<String> trollIds = profileProxy.getTrollIds(context);
        String text = context.getString(R.string.app_name);
        Bitmap blason = null;
        if (trollIds != null && !trollIds.isEmpty()) {
            String firstTrollId = trollIds.iterator().next();
            try {
                Log.w(TAG, "Fetch Troll with id=" + firstTrollId);
                Pair<Troll, Boolean> pair = profileProxy.fetchTrollWithoutUpdate(context, firstTrollId);
                Troll troll = pair.left();

                text = Trolls.getWidgetDlaTextFunction(context).apply(troll);
                blason = MhDlaNotifierUtils.loadBlasonForWidget(troll.getBlason(), context.getCacheDir());
            } catch (MissingLoginPasswordException e) {
                Log.w(TAG, "Unable to get troll's DLA", e);
            }
        }
        return Pair.of(text, blason);
    }

}
