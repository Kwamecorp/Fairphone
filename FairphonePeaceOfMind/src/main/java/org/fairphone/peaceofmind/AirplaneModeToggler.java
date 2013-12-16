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

/*
Modifications (MN 2013-12-16):
- Moved intentExtra label to PeaceOfMindIntents
- Added isSilentModeOnly verification in isAirplaneModeOn()
- Removed unused toggleAirplaneMode()
- Replaced Settings.Global.putInt() by SuperuserHelper wrapper in setAirplaneModeSettings()
- Added version verification in sendAirplaneModeIntent()
*/
package org.fairphone.peaceofmind;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;

import org.fairphone.peaceofmind.data.PeaceOfMindStats;
import org.fairphone.peaceofmind.superuser.SuperuserHelper;

public class AirplaneModeToggler {
    public static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            final boolean isSilentModeOnly = PeaceOfMindStats.isSilentModeOnly(PreferenceManager.getDefaultSharedPreferences(context));
            if (isSilentModeOnly) {
                // Without su-access given, the app toggles the silent mode
                final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                return (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT);
            } else {
                return Settings.Global.getInt(context.getContentResolver(),
                        Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
            }
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

    private static void setAirplaneModeSettings(Context context, int value) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Settings.System.putInt(
                    context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, value);
        } else {
            // For API-17, we rely on Superuser. This includes the sendAirplaneModeIntent() call
            SuperuserHelper.setAirplaneModeSettings(context, value);
        }
    }

    private static void sendAirplaneModeIntent(Context context, boolean isEnabled) {
        // For API-17, we rely on Superuser in the sendAirplaneModeIntent() call
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra(PeaceOfMindIntents.EXTRA_STATE, isEnabled);
            intent.putExtra(PeaceOfMindIntents.EXTRA_TOGGLE, true);
            try {
                context.sendBroadcast(intent);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }
}
