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
package org.fairphone.launcher.util;

import com.flurry.android.FlurryAgent;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;


public class FlurryHelper
{
    private static final String TAG = FlurryHelper.class.getSimpleName();
    
    private static final String FLURRY_API_KEY = "VJ4PN6YDDC7CCQ2Q5M72";
    
    private static final String EMPTY_ANDROID_ID = "EmptyAndroidId";

    // Flurry Events
    public static final String SHOW_EDGE_MENU = "Edge_Swipe_Menu_Show";
    public static final String LAUNCH_EDGE_MENU_ALL_APPS = "Edge_Swipe_Menu_Launch_All_Apps";
    public static final String LAUNCH_EDGE_MENU_APP = "Edge_Swipe_Menu_Launch_App";
    public static final String EDGE_SWIPE_EMPTY_SLOT = "Empty slot";
    
    public static final String ADD_YOUR_APPS_WIDGET = "Your_Apps_Widget_Added";
    public static final String YOUR_APPS_WIDGET_REMOVED = "Your_Apps_Widget_Removed";
    public static final String YOUR_APPS_LAUNCH_ALL_APPS = "Your_Apps_Launch_All_Apps";
    public static final String YOUR_APPS_LAUNCH_APP = "Your_Apps_Launch_App";
    public static final String YOUR_APPS_RESET_OK = "Your_Apps_Reset_Ok";
    public static final String YOUR_APPS_RESET_CANCEL = "Your_Apps_Reset_Cancel";
    
    public static final String LAUNCH_EDGE_MENU_EDIT_FAVORITES_FROM_ITEM = "Edit_Favorites_Launched_From_Menu_Item";
    public static final String LAUNCH_EDGE_MENU_EDIT_FAVORITES = "Edit_Favorites_Launched_From_Menu_Icon";
    public static final String EDIT_FAVORITES_CONFIG = "Edit_Favorites_Configure";
    public static final String EDIT_FAVORITES_SWAP_ICONS = "Edit_Favorites_Swap_Favorites";
    public static final String EDIT_FAVORITES_REMOVE_ICON = "Edit_Favorites_Remove_Favorite";
    public static final String EDIT_FAVORITES_ADD_ICON = "Edit_Favorites_Add_Favorite";
    
    public static final String PEACE_OF_MIND_WIDGET_ADDED = "Peace_of_Mind_Widget_Added";
    public static final String PEACE_OF_MIND_WIDGET_REMOVED = "Peace_of_Mind_Widget_Removed";
    public static final ComponentName PEACE_OF_MIND_WIDGET_COMPONENT = new ComponentName("org.fairphone.fairphonepeaceofmindapp", "org.fairphone.peaceofmind.widget.WidgetProvider");
    
    private static FlurryHelper _instance;
    private Map<String,String> flurryParams;
    
    private FlurryHelper(){
        flurryParams = new HashMap<String, String>();
    }
    
    public static FlurryHelper getInstance(){
        if(_instance == null){
            _instance = new FlurryHelper();
        }
        return _instance;
    }
    
    public Map<String, String> setFlurryParams(String key, String value, boolean resetMap) {
        if(flurryParams == null){
            flurryParams = new HashMap<String, String>();
        }else if(resetMap){
            flurryParams.clear();
        }
        flurryParams.put(key, value);
        
        return flurryParams;
    }
    
    public static String getAndroidID(Context context){
        
        String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        String hashedId = TextUtils.isEmpty(androidId) ? EMPTY_ANDROID_ID : androidId;
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(hashedId.getBytes("UTF-8"));
            byte[] digest = md.digest();
            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            hashedId = sb.toString();
            
        } catch (NoSuchAlgorithmException e) {
            Log.w(TAG, "Error generating hash: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "Error generating hash: " + e.getMessage());
        }
        
        Log.i(TAG, "Hashed Android Id -> " + hashedId);
        return hashedId;
    }
    
    public static void startFlurrySession(Context context){
        FlurryAgent.setUserId(FlurryHelper.getAndroidID(context));
        FlurryAgent.onStartSession(context, FLURRY_API_KEY);
    }
    
    public static void endFlurrySession(Context context){
        FlurryAgent.onEndSession(context);
    }
}

