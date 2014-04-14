package org.fairphone.peaceofmind.utils;
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


import com.flurry.android.FlurryAgent;

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
    
    private static final String FLURRY_API_KEY = "ZZM6Q478CTTP2SFNFJQ3";
    
    private static final String EMPTY_ANDROID_ID = "EmptyAndroidId";
    
    // Flurry Events
    public static final String PEACE_OF_MIND_STARTED = "Peace_of_Mind_Started";
    public static final String PEACE_OF_MIND_STOPPED= "Peace_of_Mind_Stopped";
    public static final String PEACE_OF_MIND_SET_TIME = "Peace_of_Mind_Set_Time";
    
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

