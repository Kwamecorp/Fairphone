/*
 * Copyright (C) 2013 Fairphone Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fairphone.peaceofmind.widget;

import org.fairphone.fairphonepeaceofmindapp.R;
import org.fairphone.peaceofmind.data.PeaceOfMindStats;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider
{
    private static final int MINUTE = 60 * 1000;
    private static final int HOUR = 60 * MINUTE;

    private static final String TAG = WidgetProvider.class.getSimpleName();
    private PeaceOfMindStats mCurrentStats;

    @Override
    public void onEnabled(Context context)
    {
        Log.d(TAG, "Fairphone - WidgetProvider Context is " + context);
    }

    @Override
    public void onDisabled(Context context)
    {
        // Called once the last instance of your widget is removed from the
        // homescreen
        super.onDisabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        // Widget instance is removed from the homescreen
        Log.d(TAG, "onDeleted - " + appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
    {
        updateUI(context, appWidgetManager, appWidgetId);

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    private void loadCurrentStats(Context context)
    {
        mCurrentStats = PeaceOfMindStats.getStatsFromSharedPreferences(PreferenceManager.getDefaultSharedPreferences(context));
    }

    private void updateUI(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        loadCurrentStats(context);

        // get the widgets
        RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.widget);

        if (mCurrentStats.mIsOnPeaceOfMind)
        {
            updateWidgetForPeaceOfMind(context, widget);
        }
        else
        {
            updateWidgetForOffPeaceOfMind(context, widget);
        }

        // set the the app link
        Intent intent = new Intent(context, org.fairphone.peaceofmind.PeaceOfMindActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        widget.setOnClickPendingIntent(R.id.peaceOfMindWidgetLayout, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, widget);

    }

    private void updateWidgetForOffPeaceOfMind(Context context, RemoteViews widget)
    {
        // disable off peace of mind text
        widget.setViewVisibility(R.id.onGroup, View.GONE);
        widget.setViewVisibility(R.id.offGroup, View.VISIBLE);

        Drawable background = context.getResources().getDrawable(R.drawable.widget_progressbar_background_off);
        widget.setImageViewBitmap(R.id.progressBarBackground, ((BitmapDrawable) background).getBitmap());

        int maxTime = (int) PeaceOfMindStats.MAX_TIME / 1000;

        // set progress bar
        widget.setProgressBar(R.id.progressBar, maxTime, 0, false);

        widget.setViewPadding(R.id.timerTexts, 0, 0, 0, 0);
        widget.setViewPadding(R.id.peaceOfMindText, 0, 0, 0, 0);

        widget.setProgressBar(R.id.secondaryProgressBar, maxTime, 0, false);
    }

    private void setTimeText(Context context, long time, int hoursId, RemoteViews widgets)
    {
        int hours = (int) (time / HOUR);
        int timeInMinutes = (int) (time - hours * HOUR);
        int minutes = 0;
        
        if(hours == 0){
        	minutes = timeInMinutes - MINUTE > 0 ? timeInMinutes / MINUTE : 1;
        }else{
        	minutes = timeInMinutes / MINUTE;
        }

        String timeStr = String.format("%d%s%02d", hours, context.getResources().getString(R.string.hour_separator), minutes);
        if(hoursId == R.id.timeText){
	        if(hours == 0){
	        	widgets.setTextViewText(R.id.toText, context.getResources().getString(R.string.to_m));
	        }else{
	        	widgets.setTextViewText(R.id.toText, context.getResources().getString(R.string.to_h));
	        }
        }
        widgets.setTextViewText(hoursId, timeStr);
        
    }

    private void updateWidgetForPeaceOfMind(Context context, RemoteViews widget)
    {
        // disable off peace of mind text
        widget.setViewVisibility(R.id.onGroup, View.VISIBLE);
        widget.setViewVisibility(R.id.offGroup, View.GONE);

        Drawable background = context.getResources().getDrawable(R.drawable.widget_progressbar_background_on);
        widget.setImageViewBitmap(R.id.progressBarBackground, ((BitmapDrawable) background).getBitmap());

        // set the time
        long timeUntilTarget = mCurrentStats.mCurrentRun.mTargetTime - mCurrentStats.mCurrentRun.mPastTime;
        setTimeText(context, timeUntilTarget, R.id.timeText, widget);
        setTimeText(context, mCurrentStats.mCurrentRun.mTargetTime, R.id.totalTimeText, widget);

        // set progress bar
        int maxTime = (int) PeaceOfMindStats.MAX_TIME / 1000;
        int progress = (int) mCurrentStats.mCurrentRun.mPastTime / 1000;
        widget.setProgressBar(R.id.progressBar, maxTime, progress, false);

        //225 is the progress bar size in pixels
        //TODO: Put the magical number in resources using dp if possible 
        int progressText = (int) (225 * (mCurrentStats.mCurrentRun.mPastTime / 1000)) / maxTime;

        int ajustedProgress = getajustedTextProgress(progress, progressText);
        if (ajustedProgress > 0 && ajustedProgress < 215)
        {
            widget.setViewPadding(R.id.timerTexts, 0, 0, 0, ajustedProgress);
            widget.setViewPadding(R.id.peaceOfMindText, 0, 0, 0, ajustedProgress);
        }

        int secondaryProgress = (int) mCurrentStats.mCurrentRun.mTargetTime / 1000;
        widget.setProgressBar(R.id.secondaryProgressBar, maxTime, secondaryProgress, false);
    }

    //this is used to make the text go up aligned with progress bar position
    //TODO:Change the magical numbers to dp if possible
	private int getajustedTextProgress(int progress, int progressText) {
		long maxTimeSeconds = PeaceOfMindStats.MAX_TIME/1000;
		int ajustedProgress = 0;
		if(progress <= (maxTimeSeconds/4)){
			ajustedProgress = progressText - 40;
		}
		else if(progress <= (maxTimeSeconds/2)){
			ajustedProgress = progressText - 35;
		}else if(progress <= (maxTimeSeconds/1.5)){
			ajustedProgress = progressText - 25;
		}else{
			ajustedProgress = progressText - 15;
		}
		return ajustedProgress;
	}

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.appwidget.AppWidgetProvider#onUpdate(android.content.Context,
     * android.appwidget.AppWidgetManager, int[])
     * 
     * OnUpdate ==============================================================
     * context The Context in which this receiver is running. appWidgetManager A
     * AppWidgetManager object you can call updateAppWidget(ComponentName,
     * RemoteViews) on. appWidgetIds The appWidgetIds for which an update is
     * needed. Note that this may be all of the AppWidget instances for this
     * provider, or just a subset of them.
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // Called in response to the ACTION_APPWIDGET_UPDATE broadcast when this
        // AppWidget provider
        // is being asked to provide RemoteViews for a set of AppWidgets.
        // Override this method to implement your own AppWidget functionality.

        // iterate through every instance of this widget
        // remember that it can have more than one widget of the same type.
        for (int i = 0; i < appWidgetIds.length; i++)
        { 
            updateUI(context, appWidgetManager, appWidgetIds[i]);
        }

    }
}
