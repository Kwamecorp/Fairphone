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

package com.fairphone.updater;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UpdaterService extends Service {

	private static final String TAG = UpdaterService.class.getSimpleName();
	private static final String PREFERENCE_DATE_LAST_TIME_CHECKED = "LastTimeUpdateChecked";
	private DownloadManager mDownloadManager;
	private DownloadBroadCastReceiver mDownloadBroadCastReceiver;

	private long mLatestFileDownloadId;

	private SharedPreferences mSharedPreferences;
	private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
	
	final static long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
	private static final int MAX_DAYS_BEFORE_CHECKING = 8;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		mSharedPreferences = getApplicationContext().getSharedPreferences(FairphoneUpdater.FAIRPHONE_UPDATER_PREFERENCES, MODE_PRIVATE);

	    if(hasInternetConnection() ){
			// remove the old file if its still there for some reason
			removeLatestFile(getApplicationContext());
	
			setupDownloadManager();
	
			// start the download of the latest file
			startDownloadLatest();
		}
		
		return Service.START_NOT_STICKY;
	}

	private void removeLatestFile(Context context) {
        VersionParserHelper.removeFiles(context);
        
		updateLastChecked("2013.01.01 00:00:00");
	}

	private boolean isFileStillValid() {
		Date lastTimeChecked = getLastTimeCheckedDate();
		
		int diffInDays = (int) ((System.currentTimeMillis() - lastTimeChecked.getTime())/ DAY_IN_MILLIS );
		
		return diffInDays < MAX_DAYS_BEFORE_CHECKING;
	}

	private Date getLastTimeCheckedDate() {
		
		String lastTimeDatePreference = mSharedPreferences.getString(PREFERENCE_DATE_LAST_TIME_CHECKED, "2013.01.01 00:00:00");
		
		Date lastTimeDate = null;
		try {
			lastTimeDate = mDateFormat.parse(lastTimeDatePreference);
		} catch (ParseException e) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.YEAR, -1);
			
			lastTimeDate = cal.getTime();
		}
		
		return lastTimeDate;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void startDownloadLatest() {
		if(hasConnection()){
			Resources resources = getApplicationContext().getResources();
	            
			// set the download for the latest version on the download manager
			Request request = createDownloadRequest(resources.getString(R.string.downloadUrl), resources.getString(R.string.versionFilename) + resources.getString(R.string.versionFilename_zip));
			mLatestFileDownloadId = mDownloadManager.enqueue(request);
		}
	}

	private boolean hasConnection() {
		return isWiFiEnabled();
	}
	
	private boolean isWiFiEnabled() {

		ConnectivityManager manager = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.isConnectedOrConnecting();

		return isWifi;
	}

	private void setNotification() {

		Context context = getApplicationContext();

		NotificationManager manager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context)
				.setSmallIcon(R.drawable.fairphone_updater_tray_icon_small)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.fairphone_updater_tray_icon))
				.setContentTitle(
						context.getResources().getString(R.string.app_name))
				.setContentText(getResources().getString(R.string.fairphoneUpdateMessage));

		Intent resultIntent = new Intent(context, FairphoneUpdater.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

		stackBuilder.addParentStack(FairphoneUpdater.class);

		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);

		builder.setContentIntent(resultPendingIntent);

		Notification notificationWhileRunnig = builder.build();
		
		// Add notification
		manager.notify(0, notificationWhileRunnig);
		
		//to update the activity
		Intent updateIntent = new Intent(FairphoneUpdater.FAIRPHONE_UPDATER_NEW_VERSION_RECEIVED);
        sendBroadcast(updateIntent);
	}

	private Request createDownloadRequest(String url, String fileName) {

	    
		Request request = new Request(Uri.parse(url));
		Environment.getExternalStoragePublicDirectory(
				Environment.getExternalStorageDirectory()
						+ VersionParserHelper.UPDATER_FOLDER).mkdirs();

		request.setDestinationInExternalPublicDir(
				VersionParserHelper.UPDATER_FOLDER, fileName);
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
		request.setAllowedOverRoaming(false);
		
		Resources resources = getApplicationContext().getResources();
		request.setTitle(resources.getString(R.string.downloadUpdateTitle));

		return request;
	}

	private boolean hasInternetConnection() {

		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.isConnectedOrConnecting();

		boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.isConnectedOrConnecting();

		return isWifi || is3g;
	}

	private void setupDownloadManager() {
		mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

		mDownloadBroadCastReceiver = new DownloadBroadCastReceiver();

		getApplicationContext().registerReceiver(mDownloadBroadCastReceiver,
				new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}

	private void removeBroadcastReceiver() {
		getApplicationContext().unregisterReceiver(mDownloadBroadCastReceiver);
	}
	
	private void checkVersionValidation(Context context){
	    
		Version latestVersion = VersionParserHelper
				.getLastestVersion(getApplicationContext());
		Version currentVersion = VersionParserHelper
				.getDeviceVersion(getApplicationContext());
		
		if(latestVersion != null){
			
			String versionName = null;
			String versionNumber = null;
			String versionUrl = null;
			String versionMd5 = null;
			String versionAndroid = null;
			
			if(latestVersion.isNewerVersionThan(currentVersion)){
				// save the version in the share preferences
				versionName = latestVersion.getName();
				versionNumber = latestVersion.getNumber();
				versionUrl = latestVersion.getDownloadLink();
				versionMd5 = latestVersion.getMd5Sum();
				versionAndroid = latestVersion.getAndroid();
				
				setNotification();
			} else {
				VersionParserHelper.removeLatestVersionFile(getApplicationContext());
			}
			
			Editor editor = mSharedPreferences.edit();
			
			editor.putString(FairphoneUpdater.PREFERENCE_NEW_VERSION_NAME, versionName);
			editor.putString(FairphoneUpdater.PREFERENCE_NEW_VERSION_NUMBER, versionNumber);
			editor.putString(FairphoneUpdater.PREFERENCE_NEW_VERSION_MD5_SUM, versionMd5);
			editor.putString(FairphoneUpdater.PREFERENCE_NEW_VERSION_URL, versionUrl);
			editor.putString(FairphoneUpdater.PREFERENCE_NEW_VERSION_ANDROID, versionAndroid);
			
			editor.commit();
    		
    		removeLatestFileDownload(context);
		}
	}

    private void removeLatestFileDownload(Context context) {
        if(mLatestFileDownloadId != 0){
        	mDownloadManager.remove(mLatestFileDownloadId);
        	mLatestFileDownloadId = 0;
        }
        VersionParserHelper.removeFiles(context);
    }

	private float parseVersion(String number) {
		String finalNumber = number.replaceAll("\\.", "");
		return Float.parseFloat(finalNumber);
	}

	private void updateLastChecked(String date) {
        Editor editor = mSharedPreferences.edit();
        editor.putString(PREFERENCE_DATE_LAST_TIME_CHECKED, date);
        
        editor.commit();
    }

    private class DownloadBroadCastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			DownloadManager.Query query = new DownloadManager.Query();

			query.setFilterById(mLatestFileDownloadId);
			Cursor cursor = mDownloadManager.query(query);
			
			if (cursor.moveToFirst()) {
				int columnIndex = cursor
						.getColumnIndex(DownloadManager.COLUMN_STATUS);
				int status = cursor.getInt(columnIndex);

				if (status == DownloadManager.STATUS_SUCCESSFUL) {
				    
				    String filePath = mDownloadManager.getUriForDownloadedFile(
				            mLatestFileDownloadId).getPath();
				    
				    String targetPath = Environment.getExternalStorageDirectory()
		                    + VersionParserHelper.UPDATER_FOLDER;
                    
				    if(RSAUtils.checkFileSignature(context, filePath, targetPath)){
    				    updateLastChecked(mDateFormat.format(Calendar.getInstance().getTime()));
    					checkVersionValidation(context);
					}else{
					    //invalid file
					    removeLatestFileDownload(context);
					}
				}
			}

			cursor.close();

			removeBroadcastReceiver();
		}
	}
}