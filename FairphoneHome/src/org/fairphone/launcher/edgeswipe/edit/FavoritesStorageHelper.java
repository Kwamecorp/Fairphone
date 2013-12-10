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
package org.fairphone.launcher.edgeswipe.edit;

import java.util.ArrayList;

import org.fairphone.launcher.ApplicationInfo;
import org.fairphone.launcher.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Favorites storage helper class implements methods to convert a string array
 * into one string so it can be stored easily in the shared preferences. This is
 * used to store the selected apps for the favorite app launcher.
 */
public abstract class FavoritesStorageHelper
{
    private static final String PACKAGE_AND_CLASS_NAME_DELIMITER = ",";
    private static final String COMPONENT_NAME_DELIMITER = ";";

    private static final String FAVORITES_APPS_SHARED_PREFERENCES_FILE_NAME = "FAVORITES_APPS_SHARED_PREFERENCES_FILE_NAME";

    private static final String FAVORITES_APPS_KEY = "FAVORITES_APPS_KEY";

	private static final String TAG = "FavoritesStorageHelper";

    /**
     * Load the favorites apps from the shared preferences and get the Android
     * application info from each, so we can get the app name and icon.
     */
    public static ApplicationInfo[] loadSelectedApps(Context context, int maxApps)
    {
        ApplicationInfo[] selectedAppArray = new ApplicationInfo[maxApps];
        
        SharedPreferences sharedPreferences = context.getSharedPreferences(FAVORITES_APPS_SHARED_PREFERENCES_FILE_NAME, Activity.MODE_PRIVATE);

        String componentNamesString = sharedPreferences.getString(FAVORITES_APPS_KEY, null);

        if (componentNamesString == null || componentNamesString.length() == 0){
        	componentNamesString = getDefaultEdgeSwipeApps(context);
        }
        
        if (componentNamesString != null)
        {
//        	Log.d(TAG, "loading selected apps - {" + componentNamesString + "}");
        	
            ArrayList<ComponentName> componentNames = FavoritesStorageHelper.stringToComponentNameArray(componentNamesString);
            
            final PackageManager packageManager = context.getApplicationContext().getPackageManager();
            
            for (int i = 0; i < Math.min(componentNames.size(), selectedAppArray.length ); i++)
            {
            	ApplicationInfo applicationInfo = null;
                ComponentName currentComponentName = componentNames.get(i);
                if(currentComponentName != null){
                	applicationInfo = AppDiscoverer.getInstance().getApplicationFromComponentName(currentComponentName);
                }
                
                selectedAppArray[i] = applicationInfo;
            }
        }
        
        return selectedAppArray;
    }

    public static String getDefaultEdgeSwipeApps(Context context) {
    	return context.getResources().getString(R.string.edge_swipe_default_apps);
	}

	/**
     * Store the selected apps in the shared preferences as strings which is the
     * package names of the apps. The package name of an app serves as the
     * unique identifier of the app.
     */
    public static void storeSelectedApps(Context context, ApplicationInfo[] mSelectedApps)
    {
        if ((mSelectedApps != null) && (mSelectedApps.length > 0))
        {
            ArrayList<ComponentName> componentNamesArray = new ArrayList<ComponentName>();
            
            for (int i = 0; i < mSelectedApps.length; i++) {
            	
            	ApplicationInfo applicationInfo = mSelectedApps[i];

            	if(applicationInfo != null) {
            		componentNamesArray.add(applicationInfo.componentName);
            	}
            	else{
            		componentNamesArray.add(null);
            	}
            }

            String componentNamesString = FavoritesStorageHelper.componentNamesArrayToString(componentNamesArray);

            SharedPreferences sharedPreferences = context.getSharedPreferences(FAVORITES_APPS_SHARED_PREFERENCES_FILE_NAME, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            
            editor.putString(FAVORITES_APPS_KEY, componentNamesString);
            editor.commit();
        }
    }

    /**
     * Convert string array into a single string, where each element in the
     * string array is separated by the delimiter string.
     */
    public static String arrayToString(String[] array, String delimiter)
    {
        StringBuilder sb = new StringBuilder();
        for (String s : array)
        {
            if (sb.length() > 0)
            {
                sb.append(delimiter);
            }
            sb.append(s);
        }

        return sb.toString();
    }

    public static String componentNamesArrayToString(ArrayList<ComponentName> array)
    {
    	String[] componentNamesStringArray = new String[array.size()];
        for (int i=0; i < array.size(); i++){
        	String[] componentNameArray = {"",""};
        	if(array.get(i) != null){
        		componentNameArray[0] = array.get(i).getPackageName();
        		componentNameArray[1] = array.get(i).getClassName();
        	}
        	String stringToStore = FavoritesStorageHelper.arrayToString(componentNameArray, PACKAGE_AND_CLASS_NAME_DELIMITER);
        	componentNamesStringArray[i] = stringToStore.isEmpty()? PACKAGE_AND_CLASS_NAME_DELIMITER : stringToStore;
        }

        return FavoritesStorageHelper.arrayToString(componentNamesStringArray, COMPONENT_NAME_DELIMITER);
    }
    
    /**
     * Convert a string into an array of strings, using the delimiter as
     * separator.
     */
    public static String[] stringToArray(String input, String delimiter){
        return input.split(delimiter);
    }
	
	public static ArrayList<ComponentName> stringToComponentNameArray(String input) {
		String[] componentNamesStringArray = FavoritesStorageHelper.stringToArray(input, COMPONENT_NAME_DELIMITER);
		ArrayList<ComponentName> componentNamesArray = new ArrayList<ComponentName>();
		for (int i=0; i< componentNamesStringArray.length; i++) {
			if(!componentNamesStringArray[i].equals(PACKAGE_AND_CLASS_NAME_DELIMITER)){
				String[] componentNameArray = FavoritesStorageHelper.stringToArray(componentNamesStringArray[i], PACKAGE_AND_CLASS_NAME_DELIMITER);
				ComponentName compName = new ComponentName(componentNameArray[0], componentNameArray[1]);
				componentNamesArray.add(compName);
			}else{
				componentNamesArray.add(null);
			}
		}
		return componentNamesArray;
	}

	public static void resetFavoritesToDefault(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(FAVORITES_APPS_SHARED_PREFERENCES_FILE_NAME, Activity.MODE_PRIVATE);
		
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(FAVORITES_APPS_KEY, null);
		editor.commit();
		
		loadSelectedApps(context, 4);
	}
}
