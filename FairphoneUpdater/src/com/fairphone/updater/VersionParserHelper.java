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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class VersionParserHelper {

	private static final String TAG = VersionParserHelper.class.getSimpleName();
	
	public static final String RECOVERY_PATH = "sdcard";
	public static final String UPDATER_FOLDER = "/updater/";
	
	private static final String CURRENT_FAIRPHONE_VERSION_NAME = "fairphone.ota.version.name";
	private static final String CURRENT_FAIRPHONE_VERSION = "fairphone.ota.version";
	private static final String CURRENT_ANDROID_VERSION = "fairphone.ota.android";
	
	public static String getNameFromVersion(Version version) {
		return "fp_update_" + version.getNumber() + ".zip";
	}
	
	public static Version getDeviceVersion(Context context) {

		Version version = new Version();
		version.setNumber(getSystemData(context, CURRENT_FAIRPHONE_VERSION));
		version.setName(getSystemData(context, CURRENT_FAIRPHONE_VERSION_NAME));
		version.setAndroid(getSystemData(context, CURRENT_ANDROID_VERSION));

		return version;
	}
	
	public static String getSystemData(Context context, String property) {

		if (property.equals(CURRENT_FAIRPHONE_VERSION)){
			return getprop(CURRENT_FAIRPHONE_VERSION, context.getResources().getString(R.string.defaultVersionNumber));
		}
		if (property.equals(CURRENT_FAIRPHONE_VERSION_NAME)){
			return getprop(CURRENT_FAIRPHONE_VERSION_NAME, context.getResources().getString(R.string.defaultVersionName));
		}
		if (property.equals(CURRENT_ANDROID_VERSION)){
			return getprop(CURRENT_ANDROID_VERSION, context.getResources().getString(R.string.defaultAndroidVersionNumber));
		}

		return null;
	}
	
	public static void removeLatestVersionFile(Context context){
		File file = new File(Environment.getExternalStorageDirectory() + UPDATER_FOLDER + context.getResources().getString(R.string.versionFilename));
		
		if(file.exists()){
			file.delete();
		}
	}
	
	public static Version getLastestVersion(Context context) {

		Version latest = Version.getVersionFromSharedPreferences(context);
        
		if(latest == null){
		    
		    String filePath = Environment.getExternalStorageDirectory()
	                + VersionParserHelper.UPDATER_FOLDER
	                + context.getResources().getString(R.string.versionFilename);
    		// check the /storage/sdcard0/updater/latest.xml
    		File file = new File(filePath + context.getResources().getString(R.string.versionFilename_xml));
    
    		if (file.exists()) {
    			try {
    				latest = parseLatestXML(context, file);
    			} catch (XmlPullParserException e) {
    				Log.e(TAG, "Could not start the XML parser", e);
    			} catch (IOException e) {
    				Log.e(TAG, "Invalid data in File", e);
    				// remove the files
    				removeFiles(context);
    			}
    		}
		}

		return latest;
	}
	
	private static Version parseLatestXML(Context context, File latestFile)
			throws XmlPullParserException, IOException {

		Version version = null;

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);

		FileInputStream fis = new FileInputStream(latestFile);

		XmlPullParser xpp = factory.newPullParser();

		xpp.setInput(new InputStreamReader(fis));

		int eventType = xpp.getEventType();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			String tagName = null;
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				tagName = xpp.getName();

				if (tagName.equalsIgnoreCase("version")) {
					version = new Version();
					version.setNumber(xpp.getAttributeValue(0));
				} else if (version != null) {
					if (tagName.equalsIgnoreCase("name")) {
						version.setName(xpp.nextText());
					} else if (tagName.equalsIgnoreCase("android")) {
						version.setAndroid(xpp.nextText());
					} else if (tagName.equalsIgnoreCase("md5sum")) {
						version.setMd5Sum(xpp.nextText());
					} else if (tagName.equalsIgnoreCase("link")) {
						version.setDownloadLink(xpp.nextText());
					}
				}

				break;
			}
			
			eventType = xpp.next();
		}

		if (version == null 
				|| version.getNumber() == null
				|| version.getName() == null
				|| version.getAndroid() == null
				|| version.getDownloadLink() == null) {
			
			Log.i(TAG, "Invalid data in version file");
			
			version = null;
		}else {
		    version.saveToSharedPreferences(context);
		}

		return version;
	}
	
	private static String getprop(String name, String defaultValue) {
        ProcessBuilder pb = new ProcessBuilder("/system/bin/getprop", name);
        pb.redirectErrorStream(true);
        
        Process p = null;
        InputStream is = null;
        try {
            p = pb.start();
            is = p.getInputStream();
            Scanner scan = new Scanner(is);
            scan.useDelimiter("\n");
            String prop = scan.next();
            if (prop.length() == 0) {
                return defaultValue;
            }
            return prop;
        } catch (NoSuchElementException e) {
            return defaultValue;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try { is.close(); }
                catch (Exception e) { }
            }
        }
        return defaultValue;
    }
	
	public static void removeFiles(Context context) {
        String filePath = Environment.getExternalStorageDirectory()
                + VersionParserHelper.UPDATER_FOLDER
                + context.getResources().getString(R.string.versionFilename);

        removeFile(filePath + context.getResources().getString(R.string.versionFilename_zip));
        removeFile(filePath + context.getResources().getString(R.string.versionFilename_xml));
        removeFile(filePath + context.getResources().getString(R.string.versionFilename_sig));
    }

    private static void removeFile(String filePath) {
        File file = new File(filePath);
        if(file.exists()){
            file.delete();
        }
    }
    
    
    
}
