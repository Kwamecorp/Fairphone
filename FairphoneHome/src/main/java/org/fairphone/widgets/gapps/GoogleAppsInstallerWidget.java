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
package org.fairphone.widgets.gapps;

import org.fairphone.launcher.R;
import org.fairphone.launcher.gappsinstaller.GappsInstallerHelper;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class GoogleAppsInstallerWidget extends AppWidgetProvider
{

    private static final String TAG = GoogleAppsInstallerWidget.class.getSimpleName();

    @Override
    public void onEnabled(Context context)
    {
//        Log.d(TAG, "Fairphone - GoogleAppsInstaller Context is " + context);
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
//        Log.d(TAG, "onDeleted - " + appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
    {
        updateUI(context, appWidgetManager, appWidgetId);
        // Obtain appropriate widget and update it.
        // appWidgetManager.updateAppWidget(appWidgetId, new
        // RemoteViews(context.getPackageName(), R.layout.widget));
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    private int setupButtonClickIntents(Context context, int code, RemoteViews widget)
    {
        // set up Disclaimer
        Intent disclaimerIntent = new Intent();
        disclaimerIntent.setAction(GappsInstallerHelper.GAPPS_ACTION_DISCLAIMER);
        PendingIntent disclaimerPendingIntent = PendingIntent.getBroadcast(context, code++, disclaimerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setOnClickPendingIntent(R.id.installButton, disclaimerPendingIntent);

        // set up the start download Ok intent
        Intent startDownloadOkIntent = new Intent();
        startDownloadOkIntent.setAction(GappsInstallerHelper.GAPPS_ACTION_DOWNLOAD_CONFIGURATION_FILE);
        PendingIntent startDownloadOkPendingIntent = PendingIntent.getBroadcast(context, code++, startDownloadOkIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setOnClickPendingIntent(R.id.disclaimerOkButton, startDownloadOkPendingIntent);

        // set up the start download Cancel intent
        Intent startDownloadCancelIntent = new Intent();
        startDownloadCancelIntent.setAction(GappsInstallerHelper.GOOGLE_APPS_INSTALL_DOWNLOAD_CANCEL);
        PendingIntent startDownloadCancelPendingIntent = PendingIntent.getBroadcast(context, code++, startDownloadCancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setOnClickPendingIntent(R.id.disclaimerCancelButton, startDownloadCancelPendingIntent);
        
        // set up the start download Ok intent
        Intent failedDownloadOkIntent = new Intent();
        failedDownloadOkIntent.setAction(GappsInstallerHelper.GAPPS_ACTION_DOWNLOAD_CONFIGURATION_FILE);
        PendingIntent failedDownloadOkPendingIntent = PendingIntent.getBroadcast(context, code++, failedDownloadOkIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setOnClickPendingIntent(R.id.failedDownloadOkButton, failedDownloadOkPendingIntent);

        Intent permissionsOkIntent = new Intent();
        permissionsOkIntent.setAction(GappsInstallerHelper.GAPPS_ACTION_GO_PERMISSIONS);
        PendingIntent permissionsOkPendingIntent = PendingIntent.getBroadcast(context, code++, permissionsOkIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setOnClickPendingIntent(R.id.permissionsOkButton, permissionsOkPendingIntent);
        
        // set up the start download Cancel intent
        Intent failedDownloadCancelIntent = new Intent();
        failedDownloadCancelIntent.setAction(GappsInstallerHelper.GOOGLE_APPS_INSTALL_DOWNLOAD_CANCEL);
        PendingIntent failedDownloadCancelPendingIntent = PendingIntent.getBroadcast(context, code++, failedDownloadCancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setOnClickPendingIntent(R.id.failedDownloadCancelButton, failedDownloadCancelPendingIntent);
        
        // set up the start reboot ok intent
        Intent rebootOkIntent = new Intent();
        rebootOkIntent.setAction(GappsInstallerHelper.GOOGLE_APPS_INSTALL_REBOOT);
        PendingIntent rebootOkPendingIntent = PendingIntent.getBroadcast(context, code++, rebootOkIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setOnClickPendingIntent(R.id.rebootOkButton, rebootOkPendingIntent);
        
        return code;
    }

    private void updateUI(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        int code = 0;
        // get the widgets
        RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.fp_google_apps_installer_widget);

		code = setupButtonClickIntents(context, code, widget);

        SharedPreferences sharedPrefs = context.getSharedPreferences(GappsInstallerHelper.PREFS_GOOGLE_APPS_INSTALLER_DATA, Context.MODE_PRIVATE);
        int widgetCurrentState = sharedPrefs.getInt(GappsInstallerHelper.GOOGLE_APPS_INSTALLER_STATE, 0);
        switch (widgetCurrentState)
        {
            case GappsInstallerHelper.GAPPS_STATES_INITIAL:
                widget.setViewVisibility(R.id.installGroup, View.VISIBLE);
                widget.setViewVisibility(R.id.popupDisclaimerGroup, View.GONE);
                widget.setViewVisibility(R.id.popupFailedDownloadGroup, View.GONE);
                widget.setViewVisibility(R.id.popupProgressGroup, View.GONE);
                widget.setViewVisibility(R.id.popupPermissionsGroup, View.GONE);
                widget.setViewVisibility(R.id.popupRebootGroup, View.GONE);
                widget.setViewVisibility(R.id.uninstallGroup, View.GONE);
                break;
            case GappsInstallerHelper.GAPPS_STATES_DOWNLOAD_CONFIGURATION_FILE:
            	widget.setViewVisibility(R.id.installGroup, View.GONE);
                widget.setViewVisibility(R.id.popupDisclaimerGroup, View.GONE);
                widget.setViewVisibility(R.id.popupFailedDownloadGroup, View.GONE);
                widget.setViewVisibility(R.id.popupProgressGroup, View.VISIBLE);
                widget.setViewVisibility(R.id.popupPermissionsGroup, View.GONE);
                widget.setViewVisibility(R.id.popupRebootGroup, View.GONE);
                widget.setViewVisibility(R.id.uninstallGroup, View.GONE);
                widget.setTextViewText(R.id.progressDialogTitle, context.getResources().getString(R.string.google_apps_download_title));
                
                updateProgress(widget, sharedPrefs);
            	break;
            case GappsInstallerHelper.GAPPS_STATES_DOWNLOAD_GOOGLE_APPS_FILE:
            	widget.setViewVisibility(R.id.installGroup, View.GONE);
                widget.setViewVisibility(R.id.popupDisclaimerGroup, View.GONE);
                widget.setViewVisibility(R.id.popupFailedDownloadGroup, View.GONE);
                widget.setViewVisibility(R.id.popupProgressGroup, View.VISIBLE);
                widget.setViewVisibility(R.id.popupPermissionsGroup, View.GONE);
                widget.setViewVisibility(R.id.popupRebootGroup, View.GONE);
                widget.setViewVisibility(R.id.uninstallGroup, View.GONE);
                widget.setTextViewText(R.id.progressDialogTitle, context.getResources().getString(R.string.google_apps_download_title));
                
                updateProgress(widget, sharedPrefs);
            	break;
            case GappsInstallerHelper.GAPPS_STATES_EXTRACT_FILES:
            	widget.setViewVisibility(R.id.installGroup, View.GONE);
                widget.setViewVisibility(R.id.popupDisclaimerGroup, View.GONE);
                widget.setViewVisibility(R.id.popupFailedDownloadGroup, View.GONE);
                widget.setViewVisibility(R.id.popupProgressGroup, View.VISIBLE);
                widget.setViewVisibility(R.id.popupPermissionsGroup, View.GONE);
                widget.setViewVisibility(R.id.popupRebootGroup, View.GONE);
                widget.setViewVisibility(R.id.uninstallGroup, View.GONE);
                widget.setTextViewText(R.id.progressDialogTitle, context.getResources().getString(R.string.google_apps_unzip_title));
                
                updateProgress(widget, sharedPrefs);
            	break;
            case GappsInstallerHelper.GAPPS_DOWNLOAD_FAILED_STATE:
            	widget.setViewVisibility(R.id.installGroup, View.GONE);
                widget.setViewVisibility(R.id.popupDisclaimerGroup, View.GONE);
                widget.setViewVisibility(R.id.popupFailedDownloadGroup, View.VISIBLE);
                widget.setViewVisibility(R.id.popupProgressGroup, View.GONE);
                widget.setViewVisibility(R.id.popupPermissionsGroup, View.GONE);
                widget.setViewVisibility(R.id.popupRebootGroup, View.GONE);
                widget.setViewVisibility(R.id.uninstallGroup, View.GONE);
            	break;
            case GappsInstallerHelper.GAPPS_STATES_PERMISSION_CHECK:
            	widget.setViewVisibility(R.id.installGroup, View.GONE);
            	widget.setViewVisibility(R.id.popupFailedDownloadGroup, View.GONE);
                widget.setViewVisibility(R.id.popupDisclaimerGroup, View.GONE);
                widget.setViewVisibility(R.id.popupProgressGroup, View.GONE);
                widget.setViewVisibility(R.id.popupPermissionsGroup, View.VISIBLE);
                widget.setViewVisibility(R.id.popupRebootGroup, View.GONE);
                widget.setViewVisibility(R.id.uninstallGroup, View.GONE);
            	break;
            case GappsInstallerHelper.GAPPS_STATE_INSTALLATION:
            	widget.setViewVisibility(R.id.installGroup, View.GONE);
            	widget.setViewVisibility(R.id.popupFailedDownloadGroup, View.GONE);
                widget.setViewVisibility(R.id.popupDisclaimerGroup, View.GONE);
                widget.setViewVisibility(R.id.popupProgressGroup, View.VISIBLE);
                widget.setViewVisibility(R.id.popupPermissionsGroup, View.GONE);
                widget.setViewVisibility(R.id.popupRebootGroup, View.GONE);
                widget.setViewVisibility(R.id.uninstallGroup, View.GONE);
                widget.setTextViewText(R.id.progressDialogTitle, context.getResources().getString(R.string.google_apps_install_title));

                updateProgress(widget, sharedPrefs);
	            break;
            case GappsInstallerHelper.GAPPS_REBOOT_STATE:
            	widget.setViewVisibility(R.id.installGroup, View.GONE);
            	widget.setViewVisibility(R.id.popupFailedDownloadGroup, View.GONE);
                widget.setViewVisibility(R.id.popupDisclaimerGroup, View.GONE);
                widget.setViewVisibility(R.id.popupProgressGroup, View.GONE);
                widget.setViewVisibility(R.id.popupPermissionsGroup, View.GONE);
                widget.setViewVisibility(R.id.popupRebootGroup, View.VISIBLE);
                widget.setViewVisibility(R.id.uninstallGroup, View.GONE);
	            break;
            case GappsInstallerHelper.GAPPS_INSTALLED_STATE:
                widget.setViewVisibility(R.id.installGroup, View.GONE);
                widget.setViewVisibility(R.id.popupFailedDownloadGroup, View.GONE);
                widget.setViewVisibility(R.id.popupDisclaimerGroup, View.GONE);
                widget.setViewVisibility(R.id.popupProgressGroup, View.GONE);
                widget.setViewVisibility(R.id.popupPermissionsGroup, View.GONE);
                widget.setViewVisibility(R.id.popupRebootGroup, View.GONE);
                widget.setViewVisibility(R.id.uninstallGroup, View.VISIBLE);
                break;

            default:
                break;
        }

        // update the widget data
        appWidgetManager.updateAppWidget(appWidgetId, widget);
    }

	private void updateProgress(RemoteViews widget,
			SharedPreferences sharedPrefs) {
		int progress = sharedPrefs.getInt(GappsInstallerHelper.GOOGLE_APPS_INSTALLER_PROGRESS, 0);
		int progressMax = sharedPrefs.getInt(GappsInstallerHelper.GOOGLE_APPS_INSTALLER_PROGRESS_MAX, 0);
		widget.setProgressBar(R.id.progressBar, progressMax != 0 ? progressMax : 100, progress, false);
	}

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
        { // See the dimensions and
            System.out.println("Updating widget #" + i);
            updateUI(context, appWidgetManager, appWidgetIds[i]);
        }

    }
}
