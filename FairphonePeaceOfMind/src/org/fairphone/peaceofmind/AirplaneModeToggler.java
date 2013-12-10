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
package org.fairphone.peaceofmind;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

public class AirplaneModeToggler {
	
	public static final String PEACE_OF_MIND_TOGGLE = "PEACE_OF_MIND_TOGGLE";

	public static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(), 
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;          
        } else {
            return Settings.Global.getInt(context.getContentResolver(), 
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }       
    }
	
	public static void setAirplaneModeOn(Context context) {
		setAirplaneModeSettings(context, 1);
		sendAirplaneModeIntent(context, true);
	}
	
	public static void setAirplaneModeOff(Context context) {
		setAirplaneModeSettings(context, 0);
		sendAirplaneModeIntent(context, false);
	}
	
	public static void toggleAirplaneMode(Context context) {
        boolean isEnabled = isAirplaneModeOn(context);
        // Toggle airplane mode.
        setAirplaneModeSettings(context, isEnabled?0:1);
        // Post an intent to reload.
        sendAirplaneModeIntent(context, !isEnabled);
    }
    
    private static void setAirplaneModeSettings(Context context, int value) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Settings.System.putInt(
                      context.getContentResolver(),
                      Settings.System.AIRPLANE_MODE_ON, value);
        } else {
            Settings.Global.putInt(
                      context.getContentResolver(),
                      Settings.Global.AIRPLANE_MODE_ON, value);
        }       
    }
    
    private static void sendAirplaneModeIntent(Context context,
			boolean isEnabled) {
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", isEnabled);
        intent.putExtra(PEACE_OF_MIND_TOGGLE, true);
        context.sendBroadcast(intent);
	}
}
