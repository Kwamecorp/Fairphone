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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

public class Version {
	public static final String FAIRPHONE_UPDATE_VERSION_NUMBER = "FairphoneUpdateVersionNumber";
	public static final String FAIRPHONE_UPDATE_VERSION_NAME = "FairphoneUpdateVersionName";
	public static final String FAIRPHONE_UPDATE_VERSION_ANDROID = "FairphoneUpdateVersionAndroid";
	public static final String FAIRPHONE_UPDATE_VERSION_DOWNLOAD_LINK = "FairphoneUpdateVersionDownloadLink";
	public static final String FAIRPHONE_UPDATE_VERSION_MD5 = "FairphoneUpdateVersionMD5";

	private String mNumber;
	private String mName;
	private String mAndroid;
	private String mDownloadLink;
	private String mMd5Sum;
	private String mChangeLog;

	public static Version getVersionFromSharedPreferences(Context context) {
		Version version = new Version();
		SharedPreferences sharedPrefs = context.getSharedPreferences(
				FairphoneUpdater.FAIRPHONE_UPDATER_PREFERENCES,
				Context.MODE_PRIVATE);
		Resources resources = context.getResources();

		String defaultVersionNumber = resources
				.getString(R.string.defaultVersionNumber);
		version.setNumber(sharedPrefs.getString(
				FAIRPHONE_UPDATE_VERSION_NUMBER, defaultVersionNumber));

		String defaultVersionName = resources
				.getString(R.string.defaultVersionName);
		version.setName(sharedPrefs.getString(FAIRPHONE_UPDATE_VERSION_NAME,
				defaultVersionName));

		String defaultVersionAndroid = resources
				.getString(R.string.defaultAndroidVersionNumber);
		version.setAndroid(sharedPrefs.getString(
				FAIRPHONE_UPDATE_VERSION_ANDROID, defaultVersionAndroid));

		version.setDownloadLink(sharedPrefs.getString(
				FAIRPHONE_UPDATE_VERSION_DOWNLOAD_LINK, ""));
		version.setMd5Sum(sharedPrefs.getString(FAIRPHONE_UPDATE_VERSION_MD5,
				""));

		if (TextUtils.isEmpty(version.getMd5Sum())
				|| TextUtils.isEmpty(version.getMd5Sum())) {
			return null;
		}
		return version;
	}

	public void saveToSharedPreferences(Context context) {
		SharedPreferences sharedPrefs = context.getSharedPreferences(
				FairphoneUpdater.FAIRPHONE_UPDATER_PREFERENCES,
				Context.MODE_PRIVATE);

		Editor editor = sharedPrefs.edit();
		editor.putString(FAIRPHONE_UPDATE_VERSION_NUMBER, getNumber());
		editor.putString(FAIRPHONE_UPDATE_VERSION_NAME, getName());
		editor.putString(FAIRPHONE_UPDATE_VERSION_ANDROID, getAndroid());
		editor.putString(FAIRPHONE_UPDATE_VERSION_DOWNLOAD_LINK,
				getDownloadLink());
		editor.putString(FAIRPHONE_UPDATE_VERSION_MD5, getMd5Sum());
		editor.commit();
	}

	public String getNumber() {
		return mNumber;
	}

	public void setNumber(String number) {
		this.mNumber = number;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public String getDownloadLink() {
		return mDownloadLink;
	}

	public void setDownloadLink(String mDownloadLink) {
		this.mDownloadLink = mDownloadLink;
	}

	public String getMd5Sum() {
		return mMd5Sum;
	}

	public void setMd5Sum(String mMd5Sum) {
		this.mMd5Sum = mMd5Sum;
	}

	public String getChangeLog() {
		return mChangeLog;
	}

	public void setChangeLog(String mChangeLog) {
		this.mChangeLog = mChangeLog;
	}

	public String getAndroid() {
		return mAndroid;
	}

	public void setAndroid(String mAndroid) {
		this.mAndroid = mAndroid;
	}

	public boolean isNewerVersionThan(Version version) {

		boolean result = false;

		try {
			result = Version
					.isNewVersion(version.getNumber(), this.getNumber());
		} catch (Throwable t) {
			Log.e(Version.class.getSimpleName(), "Invalid Number for Version",
					t);
		}

		return result;
	}

	private static boolean isNewVersion(String versionA, String versionB)
			throws IllegalArgumentException {

		int[] versionAints = getVersionInt(versionA);
		int[] versionBints = getVersionInt(versionB);

		if (versionAints[0] == versionBints[0]) {
			return versionAints[1] < versionBints[1];
		}

		return versionAints[0] < versionBints[0];
	}

	private static int[] getVersionInt(String version) {

		if (version == null) {
			throw new IllegalArgumentException("String is null");
		}

		String[] intStrs = version.split("\\.");

		if (intStrs == null || intStrs.length != 2) {
			throw new IllegalArgumentException("String " + version
					+ " not have the correct format [X.Y]");
		}

		int[] ints = new int[2];

		try {
			ints[0] = Integer.parseInt(intStrs[0]);
			ints[1] = Integer.parseInt(intStrs[1]);
		} catch (Throwable t) {
			throw new IllegalArgumentException(
					"String "
							+ version
							+ " should contain numbers separated by a dot [ReleaseNumber.VersionNumber]");
		}

		return ints;
	}

    public void deleteFromSharedPreferences(Context context) {
        setNumber(null);
        setName(null);
        setAndroid(null);
        setDownloadLink(null);
        setMd5Sum(null);
        saveToSharedPreferences(context);
    }
}
