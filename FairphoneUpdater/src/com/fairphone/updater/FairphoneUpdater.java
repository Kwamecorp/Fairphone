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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FairphoneUpdater extends Activity {

	protected static final String PREFERENCE_NEW_VERSION_NAME = "PREFERENCE_NEW_VERSION_NAME";
	protected static final String PREFERENCE_NEW_VERSION_NUMBER = "PREFERENCE_NEW_VERSION_NUMBER";
	protected static final String PREFERENCE_NEW_VERSION_MD5_SUM = "PREFERENCE_NEW_VERSION_MD5_SUM";
	protected static final String PREFERENCE_NEW_VERSION_URL = "PREFERENCE_NEW_VERSION_URL";
	protected static final String PREFERENCE_NEW_VERSION_ANDROID = "PREFERENCE_NEW_VERSION_ANDROID";

	private static final String ANDROID_LABEL = "Android ";
	private static final String FAIRPHONE_LABEL = "Fairphone ";

	private static final String TAG = FairphoneUpdater.class.getSimpleName();

	private static final String PREFERENCE_CURRENT_UPDATER_STATE = "CurrentUpdaterState";
	private static final String PREFERENCE_DOWNLOAD_ID = "LatestUpdateDownloadId";
	public static final String FAIRPHONE_UPDATER_PREFERENCES = "FairphoneUpdaterPreferences";

	public static enum UpdaterState {
		NORMAL, DOWNLOAD, PREINSTALL
	};

	private Version mDeviceVersion;
	private Version mLatestVersion;

	private UpdaterState mCurrentState;

	private SharedPreferences mSharedPreferences;

	// views
	private TextView mViewCurrentVersionTitle;
	private TextView mViewCurrentVersionText;

	private TextView mViewUpdateVersionTitle;
	private TextView mViewUpdateVersionText;

	private TextView mViewMessageText;
	private Button mViewUpdateButton;

	private LinearLayout mLatestGroupLla;

	private DownloadManager mDownloadManager;
	private DownloadBroadCastReceiver mDownloadBroadCastReceiver;
	private long mLatestUpdateDownloadId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fairphone_updater);

		setupLayout();

		mSharedPreferences = getSharedPreferences(
				FAIRPHONE_UPDATER_PREFERENCES, MODE_PRIVATE);

		// get system data
		mDeviceVersion = VersionParserHelper.getDeviceVersion(this);

		mLatestVersion = getLastestVersion();

		// check current state
		mCurrentState = getCurrentUpdaterState();

		setupInstallationReceivers();

		// TODO : remove this
		Intent i = new Intent(this, UpdaterService.class);
		startService(i);
	}

	private Version getLastestVersion() {
		Version latest = null;

		String newVersionName = mSharedPreferences.getString(
				PREFERENCE_NEW_VERSION_NAME, null);

		String number = mSharedPreferences.getString(
				PREFERENCE_NEW_VERSION_NUMBER, null);
		String url = mSharedPreferences.getString(PREFERENCE_NEW_VERSION_URL,
				null);
		String md5 = mSharedPreferences.getString(
				PREFERENCE_NEW_VERSION_MD5_SUM, null);
		String android = mSharedPreferences.getString(
				PREFERENCE_NEW_VERSION_ANDROID, null);

		if (newVersionName != null && number != null && url != null
				&& md5 != null && android != null) {
			latest = new Version();

			latest.setName(newVersionName);
			latest.setNumber(number);
			latest.setDownloadLink(url);
			latest.setMd5Sum(md5);
			latest.setAndroid(android);
		}

		return latest;
	}

	private void setupLayout() {
		mViewCurrentVersionTitle = (TextView) findViewById(R.id.currentVersionTitleText);
		mViewCurrentVersionTitle.setVisibility(View.VISIBLE);
		mViewCurrentVersionText = (TextView) findViewById(R.id.currentVersionDescriptionText);
		mViewCurrentVersionText.setVisibility(View.VISIBLE);

		mViewUpdateVersionTitle = (TextView) findViewById(R.id.nextVersionTitleText);
		mViewUpdateVersionText = (TextView) findViewById(R.id.nextVersionDescriptionText);

		mViewUpdateButton = (Button) findViewById(R.id.newVersionUpdateButton);

		mViewMessageText = (TextView) findViewById(R.id.messageText);

		mViewUpdateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCurrentState == UpdaterState.NORMAL) {
					startUpdateDownload();
				} else if (mCurrentState == UpdaterState.PREINSTALL) {
					startPreInstall();
				}
			}
		});

		mLatestGroupLla = (LinearLayout) findViewById(R.id.latestVersionGroup);

		mLatestGroupLla.setVisibility(View.GONE);
	}

	public String getStringPreference(String key) {
		return mSharedPreferences.getString(key, null);
	}

	public long getLongPreference(String key) {
		return mSharedPreferences.getLong(key, 0);
	}

	public boolean getBooleanPreference(String key) {
		return mSharedPreferences.getBoolean(key, false);
	}

	public void savePreference(String key, String value) {
		Editor editor = mSharedPreferences.edit();

		editor.putString(key, value);

		editor.commit();
	}

	public void savePreference(String key, boolean value) {
		Editor editor = mSharedPreferences.edit();

		editor.putBoolean(key, value);

		editor.commit();
	}

	public void savePreference(String key, long value) {
		Editor editor = mSharedPreferences.edit();

		editor.putLong(key, value);

		editor.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();

		registerBroadCastReceiver();
		// check current state
		mCurrentState = getCurrentUpdaterState();

		if (mLatestVersion == null) {
			mLatestVersion = VersionParserHelper.getLastestVersion(this);
		}

		mViewCurrentVersionTitle.setText(mDeviceVersion.getName());
		mViewCurrentVersionText.setText(FAIRPHONE_LABEL
				+ mDeviceVersion.getNumber() + "\n" + ANDROID_LABEL
				+ mDeviceVersion.getAndroid());

		setupState(mCurrentState);
	}

	private void setupState(UpdaterState state) {
		switch (state) {
		case NORMAL:
			setupNormalState();
			break;
		case DOWNLOAD:
			setupDownloadState();
			break;
		case PREINSTALL:
			setupPreInstallState();
			break;
		}
	}

	private void changeState(UpdaterState newState) {
		mCurrentState = newState;

		Editor editor = mSharedPreferences.edit();

		editor.putString(PREFERENCE_CURRENT_UPDATER_STATE, mCurrentState.name());

		editor.commit();

		setupState(mCurrentState);
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterBroadCastReceiver();
	}

	private void setupNormalState() {

		if (mLatestUpdateDownloadId != 0) {
			// residue download ID
			mDownloadManager.remove(mLatestUpdateDownloadId);

			mLatestUpdateDownloadId = 0;
			savePreference(PREFERENCE_DOWNLOAD_ID, mLatestUpdateDownloadId);
		}

		// check to see if there is a new version to install
		if (mLatestVersion != null) {
			mLatestGroupLla.setVisibility(View.VISIBLE);
			mViewUpdateButton.setVisibility(View.VISIBLE);

			mViewUpdateVersionTitle.setText(mLatestVersion.getName());
			mViewUpdateVersionText.setText(FAIRPHONE_LABEL
					+ mLatestVersion.getNumber() + "\n" + ANDROID_LABEL
					+ mLatestVersion.getAndroid());

			mViewMessageText.setVisibility(View.GONE);
			mViewUpdateButton.setText(getResources().getString(
					R.string.installVersion));
		} else {
			mLatestGroupLla.setVisibility(View.GONE);
		}
	}

	private UpdaterState getCurrentUpdaterState() {

		String currentState = getStringPreference(PREFERENCE_CURRENT_UPDATER_STATE);

		if (currentState == null || currentState.isEmpty()) {
			currentState = UpdaterState.NORMAL.name();

			Editor editor = mSharedPreferences.edit();

			editor.putString(currentState, currentState);

			editor.commit();
		}

		return UpdaterState.valueOf(currentState);
	}

	private static String getVersionDownloadPath(Version version) {
		return Environment.getExternalStorageDirectory()
				+ VersionParserHelper.UPDATER_FOLDER
				+ VersionParserHelper.getNameFromVersion(version);
	}

	// ************************************************************************************
	// PRE INSTALL
	// ************************************************************************************

	private void setupPreInstallState() {

		// the latest version data must exist
		if (mLatestVersion != null) {

			mViewUpdateVersionTitle.setText(mLatestVersion.getName());
			mViewUpdateVersionText.setText(FAIRPHONE_LABEL
					+ mLatestVersion.getNumber() + "\n" + ANDROID_LABEL
					+ mLatestVersion.getAndroid());

			// check the md5 of the file
			File file = new File(getVersionDownloadPath(mLatestVersion));

			if (file.exists()) {

				if (FairphoneUpdater.checkMD5(mLatestVersion.getMd5Sum(), file)) {
					mLatestGroupLla.setVisibility(View.VISIBLE);

					mViewMessageText.setText(getResources().getString(
							R.string.messageReadyToInstall));

					mViewUpdateButton.setText(getResources().getString(
							R.string.rebootDevice));
					mViewUpdateButton.setVisibility(View.VISIBLE);

					mViewMessageText.setVisibility(View.VISIBLE);
					return;
				} else {
					mDownloadManager.remove(mLatestUpdateDownloadId);
					mLatestUpdateDownloadId = 0;

					savePreference(PREFERENCE_DOWNLOAD_ID,
							mLatestUpdateDownloadId);

					Toast.makeText(
							this,
							getResources().getString(
									R.string.invalidDownloadMessage),
							Toast.LENGTH_SHORT).show();
				}
			}
		}

		// remove the updater directory
		File fileDir = new File(Environment.getExternalStorageDirectory()
				+ VersionParserHelper.UPDATER_FOLDER);
		fileDir.delete();

		// else if the perfect case does not happen, reset the download
		changeState(UpdaterState.NORMAL);
	}

	private void startPreInstall() {
		// set the command for the recovery
		Process p;
		try {
			p = Runtime.getRuntime().exec("su");

			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			os.writeBytes("rm -f /cache/recovery/command\n");
			os.writeBytes("rm -f /cache/recovery/extendedcommand\n");

			os.writeBytes("echo '--wipe_cache' >> /cache/recovery/command\n");

			os.writeBytes("echo '--update_package=/"
					+ VersionParserHelper.RECOVERY_PATH
					+ VersionParserHelper.UPDATER_FOLDER
					+ VersionParserHelper.getNameFromVersion(mLatestVersion)
					+ "' >> /cache/recovery/command\n");

			os.writeBytes("sync\n");
			os.writeBytes("exit\n");
			os.flush();
			p.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// reboot the device into recovery
		((PowerManager) getSystemService(POWER_SERVICE)).reboot("recovery");
	}

	// ************************************************************************************
	// DOWNLOAD UPDATE
	// ************************************************************************************

	private void startUpdateDownload() {
		// use only on WiFi
		if (isWiFiEnabled()) {
			// set the download for the latest version on the download manager
			String fileName = VersionParserHelper
					.getNameFromVersion(mLatestVersion);
			Request request = createDownloadRequest(
					mLatestVersion.getDownloadLink(), fileName,
					mLatestVersion.getName() + " FP Update");
			mLatestUpdateDownloadId = mDownloadManager.enqueue(request);

			// save it on the shared preferences
			savePreference(PREFERENCE_DOWNLOAD_ID, mLatestUpdateDownloadId);

			// change state to download
			changeState(UpdaterState.DOWNLOAD);
		} else {
			Resources resources = this.getResources();

			AlertDialog.Builder disclaimerDialog = new AlertDialog.Builder(this);

			disclaimerDialog.setTitle(resources
					.getString(R.string.wifiDiscaimerTitle));

			// Setting Dialog Message
			disclaimerDialog.setMessage(resources
					.getString(R.string.wifiDiscaimerMessage));
			disclaimerDialog.setPositiveButton(
					resources.getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// do nothing, since the state is still the same
						}
					});
			disclaimerDialog.create();
			disclaimerDialog.show();
		}
	}

	private Request createDownloadRequest(String url, String fileName,
			String downloadTitle) {

		Request request = new Request(Uri.parse(url));
		Environment.getExternalStoragePublicDirectory(
				Environment.getExternalStorageDirectory()
						+ VersionParserHelper.UPDATER_FOLDER).mkdirs();

		request.setDestinationInExternalPublicDir(
				VersionParserHelper.UPDATER_FOLDER, fileName);
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
		request.setAllowedOverRoaming(false);

		request.setTitle(downloadTitle);

		return request;
	}

	private boolean isWiFiEnabled() {

		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.isConnectedOrConnecting();

		return isWifi;
	}

	private void setupDownloadState() {
		// setup the download state views
		if (mLatestVersion == null) {
			// we don't have the lastest.xml so get back to initial state
			File updateDir = new File(Environment.getExternalStorageDirectory()
					+ VersionParserHelper.UPDATER_FOLDER);

			updateDir.delete();

			changeState(UpdaterState.NORMAL);

			return;
		}

		mViewUpdateVersionTitle.setText(mLatestVersion.getName());
		mViewUpdateVersionText.setText(FAIRPHONE_LABEL
				+ mLatestVersion.getNumber() + "\n" + ANDROID_LABEL
				+ mLatestVersion.getAndroid());

		// if there is a download ID on the shared preferences
		if (mLatestUpdateDownloadId == 0) {
			mLatestUpdateDownloadId = getLongPreference(PREFERENCE_DOWNLOAD_ID);

			// invalid download Id
			if (mLatestUpdateDownloadId == 0) {

				changeState(UpdaterState.NORMAL);

				return;
			}
		}

		mLatestGroupLla.setVisibility(View.VISIBLE);
		mViewUpdateButton.setVisibility(View.GONE);
		mViewMessageText.setVisibility(View.VISIBLE);
		mViewMessageText.setText(getResources().getString(
				R.string.downloadMessage));

		updateDownloadFile();

	}

	private void updateDownloadFile() {

		DownloadManager.Query query = new DownloadManager.Query();

		query.setFilterById(mLatestUpdateDownloadId);

		Cursor cursor = mDownloadManager.query(query);

		if (cursor.moveToFirst()) {
			int columnIndex = cursor
					.getColumnIndex(DownloadManager.COLUMN_STATUS);
			int status = cursor.getInt(columnIndex);

			switch (status) {
			case DownloadManager.STATUS_SUCCESSFUL:
				changeState(UpdaterState.PREINSTALL);
				break;
			case DownloadManager.STATUS_RUNNING:
				break;
			case DownloadManager.STATUS_FAILED:
			case DownloadManager.STATUS_PAUSED:
			default:
				changeState(UpdaterState.NORMAL);

				mLatestUpdateDownloadId = 0;
				savePreference(PREFERENCE_DOWNLOAD_ID, mLatestUpdateDownloadId);

				break;
			}
		} else {
			changeState(UpdaterState.NORMAL);
		}
	}

	private void setupInstallationReceivers() {
		mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

		mDownloadBroadCastReceiver = new DownloadBroadCastReceiver();
	}

	private void registerBroadCastReceiver() {
		registerReceiver(mDownloadBroadCastReceiver, new IntentFilter(
				DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}

	private void unregisterBroadCastReceiver() {
		unregisterReceiver(mDownloadBroadCastReceiver);
	}

	private class DownloadBroadCastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (mLatestUpdateDownloadId == 0) {
				mLatestUpdateDownloadId = getLongPreference(PREFERENCE_DOWNLOAD_ID);
			}

			updateDownloadFile();

		}
	}

	// **************************************************************************************************************
	// HELPERS
	// **************************************************************************************************************

	public static boolean checkMD5(String md5, File updateFile) {

		if (!updateFile.exists()) {
			return false;
		}

		if (md5 == null || md5.equals("") || updateFile == null) {
			Log.e(TAG, "MD5 String NULL or UpdateFile NULL");
			return false;
		}

		String calculatedDigest = calculateMD5(updateFile);
		if (calculatedDigest == null) {
			Log.e(TAG, "calculatedDigest NULL");
			return false;
		}

		return calculatedDigest.equalsIgnoreCase(md5);
	}

	public static String calculateMD5(File updateFile) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "Exception while getting Digest", e);
			return null;
		}

		InputStream is;
		try {
			is = new FileInputStream(updateFile);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Exception while getting FileInputStream", e);
			return null;
		}

		byte[] buffer = new byte[8192];
		int read;
		try {
			while ((read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}
			byte[] md5sum = digest.digest();
			BigInteger bigInt = new BigInteger(1, md5sum);
			String output = bigInt.toString(16);
			// Fill to 32 chars
			output = String.format("%32s", output).replace(' ', '0');
			return output;
		} catch (IOException e) {
			throw new RuntimeException("Unable to process file for MD5", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				Log.e(TAG, "Exception on closing MD5 input stream", e);
			}
		}
	}
}
