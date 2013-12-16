package org.fairphone.peaceofmind.superuser;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.containers.RootClass;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import org.fairphone.peaceofmind.PeaceOfMindIntents;
import org.fairphone.peaceofmind.R;
import org.fairphone.peaceofmind.data.PeaceOfMindStats;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by mudar on 13/12/13.
 */

@RootClass.Candidate
public class SuperuserHelper {
    private static final String TAG = "SuperuserHelper";
    private static final String[] COMMAND_AIRPLANE_ON = {
            "settings put global airplane_mode_on 1",
            "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true -e " + PeaceOfMindIntents.EXTRA_STATE + " true " + " -e " + PeaceOfMindIntents.EXTRA_TOGGLE + " true"
    };
    private static final String[] COMMAND_AIRPLANE_OFF = {
            "settings put global airplane_mode_on 0",
            "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false -e " + PeaceOfMindIntents.EXTRA_STATE + " false " + " -e " + PeaceOfMindIntents.EXTRA_TOGGLE + " true"
    };

    public static boolean isSuperuserAvailable() {
        return RootTools.isRootAvailable();
    }

    public static boolean isSuperuserAvailable(Context context, boolean showToastMessage) {
        final boolean hasSuperuser = RootTools.isRootAvailable();

        if (!hasSuperuser && showToastMessage) {
            Toast.makeText(context, "Superuser is missing", Toast.LENGTH_SHORT).show();
        }

        return hasSuperuser;
    }

    public static void requestAccess(final Context context) {
        // For JellyBean, request SuperUser access
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    final boolean isAccessGiven = RootTools.isRootAvailable() && RootTools.isAccessGiven();
                    PeaceOfMindStats.setSilentModeOnly(!isAccessGiven, PreferenceManager.getDefaultSharedPreferences(context));
                }
            };
            thread.start();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void setAirplaneModeSettings(final Context context, final int value) {

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (RootTools.isRootAvailable() && RootTools.isAccessGiven()) {
                    runShellCommands(context, value);
                    PeaceOfMindStats.setSilentModeOnly(false, PreferenceManager.getDefaultSharedPreferences(context));
                } else {
                    toggleSilentMode(context, value);
                    PeaceOfMindStats.setSilentModeOnly(true, PreferenceManager.getDefaultSharedPreferences(context));
                }
            }
        });
    }

    private static void runShellCommands(final Context context, final int value) {
        final CommandCapture command = new CommandCapture(0, true,
                (value == 1 ? COMMAND_AIRPLANE_ON : COMMAND_AIRPLANE_OFF)) {

            @Override
            public void commandCompleted(int i, int i2) {
                if (value == 1 && context != null) {
                    Toast.makeText(context, R.string.airplane_mode_enabled, Toast.LENGTH_SHORT).show();
                }
            }
        };

        try {
            RootTools.getShell(true).add(command);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (RootDeniedException e) {
            e.printStackTrace();
        }
    }

    private static void toggleSilentMode(final Context context, final int value) {
        if (context == null) {
            return;
        }

        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (value == 1) {
            if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                Toast.makeText(context, R.string.silent_mode_enabled, Toast.LENGTH_SHORT).show();
            }
        } else {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
    }

}
