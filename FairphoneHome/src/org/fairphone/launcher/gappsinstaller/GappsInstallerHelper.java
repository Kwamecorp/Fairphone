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
package org.fairphone.launcher.gappsinstaller;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

import org.fairphone.launcher.R;
import org.fairphone.launcher.rsa.utils.RSAUtils;
import org.fairphone.widgets.gapps.GoogleAppsInstallerWidget;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
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
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GappsInstallerHelper {

    private static final String GOOGLE_APPS_DOWNLOAD_ID = "org.fairphone.launcher.gapps.DOWNLOAD_ID";

	public static final String GAPPS_ACTION_DISCLAIMER = "org.fairphone.launcher.gapps.DISCLAIMER";
	public static final String GAPPS_ACTION_DOWNLOAD_CONFIGURATION_FILE = "org.fairphone.launcher.gapps.START_DONWLOAD_CONFIGURATION";
	public static final String GOOGLE_APPS_INSTALL_DOWNLOAD_CANCEL = "org.fairphone.launcher.gapps.START_DOWNLOAD_CANCEL";
	public static final String GOOGLE_APPS_INSTALL_REBOOT = "org.fairphone.launcher.gapps.REBOOT";

	public static final String PREFS_GOOGLE_APPS_INSTALLER_DATA = "FAIRPHONE_GOOGLE_APPS_INSTALLER_DATA";
	public static final String GOOGLE_APPS_INSTALLER_STATE = "org.fairphone.launcher.gapps.WIDGET_STATE";
	public static final String GOOGLE_APPS_INSTALLER_PROGRESS = "org.fairphone.launcher.gapps.WIDGET_SEEKBAR_PROGRESS";
	public static final String GOOGLE_APPS_INSTALLER_PROGRESS_MAX = "org.fairphone.launcher.gapps.WIDGET_SEEKBAR_PROGRESS_MAX";
	public static final String GAPPS_ACTION_GO_PERMISSIONS = "org.fairphone.launcher.gaps.GAPPS_ACTION_GO_PERMISSIONS";
	public static final String GAPPS_REINSTALATION = "GAPPS_REINSTALATION_REQUEST";
    public static final String GAPPS_REINSTALL_FLAG = "GAPPS_REINSTALL_FLAG";

	public static final int GAPPS_STATES_INITIAL = 0;
	public static final int GAPPS_STATES_DOWNLOAD_CONFIGURATION_FILE = 1;
	public static final int GAPPS_STATES_DOWNLOAD_GOOGLE_APPS_FILE = 2;
	public static final int GAPPS_STATES_EXTRACT_FILES = 3;
	public static final int GAPPS_STATES_PERMISSION_CHECK = 4;
	public static final int GAPPS_STATE_INSTALLATION = 5;

	public static final int GAPPS_REBOOT_STATE = 6;
	public static final int GAPPS_INSTALLED_STATE = 7;
	public static final int GAPPS_INSTALLATION_FAILED_STATE = 8;
	public static final int GAPPS_DOWNLOAD_FAILED_STATE = 9;
	protected static final String TAG = GappsInstallerHelper.class
			.getSimpleName();

	private static String DOWNLOAD_PATH = Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
			.getAbsolutePath();

	private static String RECOVERY_PATH = "sdcard/Download/";

	private static String ZIP_CONTENT_PATH = "/googleapps/";

	private DownloadManager mDownloadManager;
	private Context mContext;
	private SharedPreferences mSharedPrefs;

	private DownloadBroadCastReceiver mDownloadBroacastReceiver;
	private long mConfigFileDownloadId;
	private long mGappsFileDownloadId;
	private String mMD5hash;

	public GappsInstallerHelper(Context context) {
		mContext = context;

		mSharedPrefs = mContext.getSharedPreferences(
				PREFS_GOOGLE_APPS_INSTALLER_DATA, Context.MODE_PRIVATE);

		resume();

		int currentState = getInstallerState();

		if (currentState == GAPPS_REBOOT_STATE) {
			updateWidgetState(GAPPS_STATES_PERMISSION_CHECK);
		}

		if (currentState != GAPPS_STATE_INSTALLATION &&
			currentState != GAPPS_INSTALLED_STATE) {

			// clean files that must be rechecked
			forceCleanUnzipDirectory();
			forceCleanConfigurationFile();

			updateInstallerState(GAPPS_STATES_INITIAL);
		}
		
		if(!checkGappsAreInstalled()){
		    updateInstallerState(GAPPS_STATES_INITIAL);
		}
	}

	private boolean checkGappsAreInstalled() {

	    boolean retVal = false;
	    
	    if (mSharedPrefs.getBoolean(GAPPS_REINSTALL_FLAG, false)) {
	        showReinstallAlert();
        }
        
		File f = new File("/system/app/OneTimeInitializer.apk");

		if (f.exists()) {
		    updateWidgetState(GAPPS_INSTALLED_STATE);
	        return true;
		}
		
		updateInstallerState(GAPPS_STATES_INITIAL);
		return false;
	}

    public void showReinstallAlert() {
        Resources resources = mContext.getResources();

        AlertDialog reinstallDialog = new AlertDialog.Builder(mContext)
                .create();

        reinstallDialog.setTitle(resources
                .getText(R.string.google_apps_reinstall_request_title));

        // Setting Dialog Message
        reinstallDialog.setMessage(resources
                .getText(R.string.google_apps_reinstall_description));

        reinstallDialog
                .setButton(
                        AlertDialog.BUTTON_POSITIVE,
                        resources
                                .getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {

                                SharedPreferences.Editor editor = mSharedPrefs.edit();
                                editor.putBoolean(GAPPS_REINSTALL_FLAG, false);
                                editor.commit();
                            }
                        });

        reinstallDialog.show();
    }

	public void resume() {
		// setup the download manager
		setupDownloadManager();

		// setup the states broadcasts receivers
		setupTheStatesBroadCasts();
	}

	public void pause() {
		// clear the download manager
		clearDownloadManager();

		// clean the broadcast receivers
		clearBroadcastReceivers();
	}

	private int getCurrentState() {
		return mSharedPrefs.getInt(GOOGLE_APPS_INSTALLER_STATE,
				GAPPS_STATES_INITIAL);
	}

	private void setupDownloadManager() {
		mDownloadManager = (DownloadManager) mContext
				.getSystemService(Context.DOWNLOAD_SERVICE);

		mDownloadBroacastReceiver = new DownloadBroadCastReceiver();

		mContext.registerReceiver(mDownloadBroacastReceiver, new IntentFilter(
				DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}

	private void clearDownloadManager() {
		mContext.unregisterReceiver(mDownloadBroacastReceiver);

		mDownloadBroacastReceiver = null;
	}

	private boolean isWiFiEnabled() {

		ConnectivityManager manager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.isConnectedOrConnecting();

		return isWifi;
	}

	private boolean hasAlreadyDownloadedZipFile(String mMD5hash, String filename) {

		File file = new File(DOWNLOAD_PATH + "/" + filename);
		return GappsInstallerHelper.checkMD5(mMD5hash, file);
	}

	private String[] getGappsUrlFromConfigFile(String filePath) {

		String[] result = new String[2];

		File configFile = new File(filePath);

		try {
			BufferedReader br = new BufferedReader(new FileReader(configFile));

			result[0] = br.readLine();
			result[1] = br.readLine();

			br.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Configuration file not find", e);
			result = null;
		} catch (IOException e) {
			Log.e(TAG, "Configuration file could not be read", e);
			result = null;
		}

		return result;
	}

	private void deleteFile(String file, String location) {
		File f = new File(location + file);

		if (f.exists()) {
			deleteRecursive(f);
		}
	}

	private void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()) {
			for (File child : fileOrDirectory.listFiles()) {
				deleteRecursive(child);
			}
		}

		fileOrDirectory.delete();
	}

	private void forceCleanConfigurationFile() {

		if (mConfigFileDownloadId != 0) {
			mDownloadManager.remove(mConfigFileDownloadId);
		}

		String configFileName = mContext.getResources().getString(
				R.string.gapps_installer_config_file);
		String configFileZip = mContext.getResources().getString(
                R.string.gapps_installer_zip);
		String configFileCfg = mContext.getResources().getString(
                R.string.gapps_installer_cfg);
		String configFileSig = mContext.getResources().getString(
                R.string.gapps_installer_sig);

		deleteFile("/" + configFileName + configFileZip, DOWNLOAD_PATH);
		deleteFile("/" + configFileName + configFileCfg, DOWNLOAD_PATH);
		deleteFile("/" + configFileName + configFileSig, DOWNLOAD_PATH);
	}

	private void forceCleanGappsZipFile() {

		long downloadID = mSharedPrefs.getLong(GOOGLE_APPS_DOWNLOAD_ID, 0);

		if (downloadID != 0) {
			mDownloadManager.remove(downloadID);
		}

		String gappsFileName = mContext.getResources().getString(
				R.string.gapps_installer_filename);

		deleteFile("/" + gappsFileName, DOWNLOAD_PATH);
	}

	private void forceCleanUnzipDirectory() {
		deleteFile(ZIP_CONTENT_PATH, DOWNLOAD_PATH);
	}

	private BroadcastReceiver mBCastDisclaimer;
	private BroadcastReceiver mBCastDownloadConfiguration;
	private BroadcastReceiver mBCastInstallDownloadCancel;
	private BroadcastReceiver mBCastGoPermissions;
	private BroadcastReceiver mBCastGappsInstallReboot;
	private BroadcastReceiver mBCastReinstallGapps;

	private void setupTheStatesBroadCasts() {
		// launching the application

		mBCastDisclaimer = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Resources resources = mContext.getResources();

				AlertDialog disclaimerDialog = new AlertDialog.Builder(mContext)
						.create();

				disclaimerDialog.setTitle(resources
						.getText(R.string.google_apps_disclaimer_title));

				// Setting Dialog Message
				disclaimerDialog.setMessage(resources
						.getText(R.string.google_apps_disclaimer_description));

				disclaimerDialog
						.setButton(
								AlertDialog.BUTTON_POSITIVE,
								resources
										.getString(R.string.google_apps_disclaimer_agree),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

										Intent startDownloadOkIntent = new Intent();
										startDownloadOkIntent
												.setAction(GappsInstallerHelper.GAPPS_ACTION_DOWNLOAD_CONFIGURATION_FILE);

										mContext.sendBroadcast(startDownloadOkIntent);
									}
								});

				disclaimerDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
						resources.getString(android.R.string.cancel),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								updateWidgetState(GAPPS_STATES_INITIAL);
							}
						});

				disclaimerDialog.show();

			}
		};

		mContext.registerReceiver(mBCastDisclaimer, new IntentFilter(
				GAPPS_ACTION_DISCLAIMER));

		mBCastDownloadConfiguration = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// clean the configuration files
				forceCleanConfigurationFile();

				if (isWiFiEnabled()) {
					String url = mContext.getResources().getString(
							R.string.gapps_installer_download_url);

					String configFileName = mContext.getResources().getString(
							R.string.gapps_installer_config_file);
					String configFileZip = mContext.getResources().getString(
			                R.string.gapps_installer_zip);

					Request request = createDownloadRequest(url, configFileName + configFileZip);
					mConfigFileDownloadId = mDownloadManager.enqueue(request);

					updateWidgetState(GAPPS_STATES_DOWNLOAD_CONFIGURATION_FILE);
				} else {

					AlertDialog disclaimerDialog = new AlertDialog.Builder(
							mContext).create();

					Resources resources = mContext.getResources();

					disclaimerDialog.setTitle(resources
							.getText(R.string.google_apps_connection_title));

					// Setting Dialog Message
					disclaimerDialog
							.setMessage(resources
									.getText(R.string.google_apps_connection_description));

					disclaimerDialog.setButton(AlertDialog.BUTTON_POSITIVE,
							resources.getString(android.R.string.ok),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									updateWidgetState(GAPPS_STATES_INITIAL);
								}
							});

					disclaimerDialog.show();

				}
			}
		};

		mContext.registerReceiver(mBCastDownloadConfiguration,
				new IntentFilter(GAPPS_ACTION_DOWNLOAD_CONFIGURATION_FILE));

		mBCastInstallDownloadCancel = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				updateWidgetState(GAPPS_STATES_INITIAL);
			}

		};

		mContext.registerReceiver(mBCastInstallDownloadCancel,
				new IntentFilter(GOOGLE_APPS_INSTALL_DOWNLOAD_CANCEL));

		mBCastGoPermissions = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				String filename = mContext.getResources().getString(
						R.string.gapps_installer_filename);

				pushFileToRecovery(filename);
			}
		};

		mContext.registerReceiver(mBCastGoPermissions, new IntentFilter(
				GAPPS_ACTION_GO_PERMISSIONS));

		mBCastGappsInstallReboot = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				
//				String filename = mContext.getResources().getString(
//						R.string.gapps_installer_filename);
//
//				pushFileToRecovery(filename);
				
				// alter State
				updateWidgetState(GAPPS_INSTALLED_STATE);
				
				// reboot
				rebootToRecovery();
			}
		};

		mContext.registerReceiver(mBCastGappsInstallReboot, new IntentFilter(
				GOOGLE_APPS_INSTALL_REBOOT));

		mBCastReinstallGapps = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                SharedPreferences.Editor editor = mSharedPrefs.edit();
                if(checkGappsAreInstalled()){
                    editor.putBoolean(GAPPS_REINSTALL_FLAG, true);
                } else {
                    editor.putBoolean(GAPPS_REINSTALL_FLAG, false);
                }
                editor.commit();
            }
        };

        mContext.registerReceiver(mBCastReinstallGapps, new IntentFilter(
                GAPPS_REINSTALATION));
	}

	public void pushFileToRecovery(String fileName) {
		if (RootTools.isAccessGiven()) {
			// set the command for the recovery
			
			try {
			     Shell.runRootCommand(new CommandCapture(0,
			            "rm -f /cache/recovery/command"));
			    
	             Shell.runRootCommand(new CommandCapture(0,
	                        "rm -f /cache/recovery/extendedcommand"));
	             
	             Shell.runRootCommand(new CommandCapture(0,
	                     "echo '--wipe_cache' >> /cache/recovery/command"));
	             
	             Shell.runRootCommand(new CommandCapture(0,
	                     "echo '--update_package=/" + RECOVERY_PATH
	                        + fileName + "' >> /cache/recovery/command"));
				
	             /*p = Runtime.getRuntime().exec("su");

				DataOutputStream os = new DataOutputStream(p.getOutputStream());
				os.writeBytes("rm -f /cache/recovery/command\n");
				os.writeBytes("rm -f /cache/recovery/extendedcommand\n");

				os.writeBytes("echo '--wipe_cache' >> /cache/recovery/command\n");

				os.writeBytes("echo '--update_package=/" + RECOVERY_PATH
						+ fileName + "' >> /cache/recovery/command\n");

				os.writeBytes("sync\n");
				os.writeBytes("exit\n");
				os.flush();
				p.waitFor();*/
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
			} catch (TimeoutException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (RootDeniedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		}

		updateWidgetState(GAPPS_REBOOT_STATE);
	}

	public void rebootToRecovery() {
		if (RootTools.isAccessGiven()) {
			updateWidgetState(GAPPS_INSTALLED_STATE);

			try
            {
                Shell.runRootCommand(new CommandCapture(0,
                        "reboot recovery"));
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (TimeoutException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (RootDeniedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
			// reboot
//			try {
//				((PowerManager) mContext
//						.getSystemService(Context.POWER_SERVICE))
//						.reboot("recovery");
//			} catch (Throwable t) {
//				Log.e(TAG, "Could not access files", t);
//			}
		} else {
			Resources resources = mContext.getResources();

			AlertDialog permissionsDialog = new AlertDialog.Builder(mContext)
					.create();

			permissionsDialog.setTitle(resources
					.getText(R.string.google_apps_denied_permissions_title));

			// Setting Dialog Message
			permissionsDialog
					.setMessage(resources
							.getText(R.string.google_apps_denied_permissions_description));

			permissionsDialog.setButton(AlertDialog.BUTTON_POSITIVE,
					resources.getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							forceCleanConfigurationFile();
							// forceCleanUnzipDirectory();

							updateWidgetState(GAPPS_STATES_INITIAL);

						}
					});

			permissionsDialog.show();
		}
	}

	private void clearBroadcastReceivers() {
		mContext.unregisterReceiver(mBCastDisclaimer);
		mContext.unregisterReceiver(mBCastDownloadConfiguration);
		mContext.unregisterReceiver(mBCastGoPermissions);
		mContext.unregisterReceiver(mBCastGappsInstallReboot);
		mContext.unregisterReceiver(mBCastInstallDownloadCancel);
		mContext.unregisterReceiver(mBCastReinstallGapps);
	}

	private void updateGoogleAppsIntallerWidgets() {
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(mContext);
		int[] appWidgetIds = appWidgetManager
				.getAppWidgetIds(new ComponentName(mContext,
						GoogleAppsInstallerWidget.class));
		if (appWidgetIds.length > 0) {
			new GoogleAppsInstallerWidget().onUpdate(mContext,
					appWidgetManager, appWidgetIds);
		}
	}

	private Request createDownloadRequest(String url, String fileName) {

		Request request = new Request(Uri.parse(url));
		Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS).mkdirs();

		request.setDestinationInExternalPublicDir(
				Environment.DIRECTORY_DOWNLOADS, fileName);
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
		request.setAllowedOverRoaming(false);

		String download = mContext.getResources().getString(
				R.string.google_apps_download_title);
		request.setTitle(download);

		return request;
	}

	private void startDownloadProgressUpdateThread(final long download_id) {
		new Thread(new Runnable() {

			@Override
			public void run() {

				boolean downloading = true;

				while (downloading) {

					DownloadManager.Query q = new DownloadManager.Query();
					q.setFilterById(download_id);

					Cursor cursor = mDownloadManager.query(q);
					if (cursor != null) {
						cursor.moveToFirst();
						int bytes_downloaded = cursor.getInt(cursor
								.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
						int bytes_total = cursor.getInt(cursor
								.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

						if (cursor.getInt(cursor
								.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
							downloading = false;

							bytes_downloaded = 0;
							bytes_total = 0;
						}

						SharedPreferences.Editor prefEdit = mSharedPrefs.edit();
						prefEdit.putInt(
								GappsInstallerHelper.GOOGLE_APPS_INSTALLER_PROGRESS,
								bytes_downloaded);
						prefEdit.putInt(
								GappsInstallerHelper.GOOGLE_APPS_INSTALLER_PROGRESS_MAX,
								bytes_total);
						prefEdit.commit();

						updateGoogleAppsIntallerWidgets();

						cursor.close();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();
	}

	boolean unzipped = false;

	private void installAppsToLocations() {

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					updateInstallerState(GAPPS_STATE_INSTALLATION);

					runCommandsAsRoot(GappsInstallationAssets.install_files);

				} catch (Throwable t) {
					Log.e(TAG, "Error while installing the files", t);
				}
			}
		});

		thread.start();
	}

	private void runCommandsAsRoot(String[] files) throws IOException,
			TimeoutException, RootDeniedException {

		String downloadPath = DOWNLOAD_PATH + ZIP_CONTENT_PATH;

		int maxFiles = files.length;
		int currentCount = 0;

		Shell.runRootCommand(new CommandCapture(0,
				GappsInstallationAssets.MOUNT_SYSTEM_RW));

		for (String filePath : files) {
			Log.d(this.getClass().getSimpleName(), "[INST]installing file :"
					+ downloadPath + filePath + " to " + filePath);

			Shell.runRootCommand(new CommandCapture(0, "cat " + downloadPath
					+ filePath + " > /" + filePath));
			currentCount++;

			// update progress bar
			Editor editor = mSharedPrefs.edit();

			editor.putInt(GappsInstallerHelper.GOOGLE_APPS_INSTALLER_PROGRESS,
					currentCount);
			editor.putInt(
					GappsInstallerHelper.GOOGLE_APPS_INSTALLER_PROGRESS_MAX,
					maxFiles);

			editor.commit();

			updateGoogleAppsIntallerWidgets();
		}

		// update the status of the files

		for (String filePath : files) {
			Log.d(this.getClass().getSimpleName(),
					"[CHMOD]changing permissions for file :" + filePath);

			Shell.runRootCommand(new CommandCapture(0, "chmod 644 /" + filePath));
		}

		Shell.runRootCommand(new CommandCapture(0,
				GappsInstallationAssets.MOUNT_SYSTEM_RO));
	}

	private void updateInstallerState(int state) {
		// alter State
		SharedPreferences.Editor prefEdit = mSharedPrefs.edit();
		prefEdit.putInt(GOOGLE_APPS_INSTALLER_STATE, state);
		prefEdit.commit();
	}

	private int getInstallerState() {
		return mSharedPrefs.getInt(GOOGLE_APPS_INSTALLER_STATE,
				GAPPS_STATES_INITIAL);
	}

	private void updateWidgetState(int state) {
		updateInstallerState(state);

		updateGoogleAppsIntallerWidgets();
	}

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

		Log.i(TAG, "Calculated digest: " + calculatedDigest);
		Log.i(TAG, "Provided digest: " + md5);

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

	private class DownloadBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			DownloadManager.Query query = new DownloadManager.Query();

			long downloadID = 0;

			switch (getCurrentState()) {
			case GAPPS_STATES_DOWNLOAD_CONFIGURATION_FILE:
				downloadID = mConfigFileDownloadId;
				break;
			default:
				downloadID = mSharedPrefs.getLong(GOOGLE_APPS_DOWNLOAD_ID, 0);
			}

			Cursor cursor = mDownloadManager.query(query);
			query.setFilterById(downloadID);

			if (cursor.moveToFirst()) {
				int columnIndex = cursor
						.getColumnIndex(DownloadManager.COLUMN_STATUS);
				int status = cursor.getInt(columnIndex);
				int columnReason = cursor
						.getColumnIndex(DownloadManager.COLUMN_REASON);
				int reason = cursor.getInt(columnReason);

				if (status == DownloadManager.STATUS_SUCCESSFUL) {
					// file to where the download happened
					String filePath = mDownloadManager.getUriForDownloadedFile(
							downloadID).getPath();

					// Retrieve the saved download id
					if (downloadID == mConfigFileDownloadId) {
					    
					    String targetPath = DOWNLOAD_PATH + ZIP_CONTENT_PATH;
					    String cfgFilename = mContext.getResources().getString(R.string.gapps_installer_config_file);
                        String fileCfgExt = mContext.getResources().getString(R.string.gapps_installer_cfg);
					    
                        String cfgFile = targetPath + cfgFilename + fileCfgExt;
					    
                        if(!checkFileSignature(filePath, targetPath)){
                            Toast.makeText(mContext,
                                    R.string.google_apps_download_error,
                                    Toast.LENGTH_LONG).show();

                            updateWidgetState(GAPPS_STATES_INITIAL);
                            return;
                        }

						// read the gapps url
						String[] downloadData = getGappsUrlFromConfigFile(cfgFile);

						if (downloadData == null) {
							Toast.makeText(mContext,
									R.string.google_apps_download_error,
									Toast.LENGTH_LONG).show();

							updateWidgetState(GAPPS_STATES_INITIAL);

							return;
						}
                        

						// read the md5
						mMD5hash = downloadData[1];

						String filename = mContext.getResources().getString(
								R.string.gapps_installer_filename);

						if (hasAlreadyDownloadedZipFile(mMD5hash, filename)) {
							updateWidgetState(GAPPS_STATES_PERMISSION_CHECK);
//							updateWidgetState(GAPPS_REBOOT_STATE);
						} else {
							Log.d(TAG, "GAPPS> file does not match");

							forceCleanGappsZipFile();

							// enqueue of gapps request
							Request request = createDownloadRequest(
									downloadData[0], filename);

							mGappsFileDownloadId = mDownloadManager
									.enqueue(request);

							SharedPreferences.Editor prefEdit = mSharedPrefs
									.edit();
							// Save the download id
							prefEdit.putLong(GOOGLE_APPS_DOWNLOAD_ID,
									mGappsFileDownloadId);

							startDownloadProgressUpdateThread(mGappsFileDownloadId);

							prefEdit.putInt(
									GappsInstallerHelper.GOOGLE_APPS_INSTALLER_PROGRESS,
									0);
							prefEdit.putInt(
									GappsInstallerHelper.GOOGLE_APPS_INSTALLER_PROGRESS_MAX,
									0);

							prefEdit.commit();

							// alter Widget State
							updateGoogleAppsIntallerWidgets();
							updateWidgetState(GAPPS_STATES_DOWNLOAD_GOOGLE_APPS_FILE);
						}
					} else {
						updateWidgetState(GAPPS_STATES_PERMISSION_CHECK);
//						updateWidgetState(GAPPS_REBOOT_STATE);
					}
				} else if (status == DownloadManager.STATUS_FAILED) {
					Toast.makeText(mContext,
							"FAILED!\n" + "reason of " + reason,
							Toast.LENGTH_LONG).show();

					forceCleanConfigurationFile();

					forceCleanUnzipDirectory();

					updateWidgetState(GAPPS_STATES_INITIAL);

				} else if (status == DownloadManager.STATUS_PAUSED) {
					Toast.makeText(mContext,
							"PAUSED!\n" + "reason of " + reason,
							Toast.LENGTH_LONG).show();
				} else if (status == DownloadManager.STATUS_PENDING) {
					Toast.makeText(mContext, "PENDING!", Toast.LENGTH_LONG)
							.show();
				} else if (status == DownloadManager.STATUS_RUNNING) {
					Toast.makeText(mContext, "RUNNING!", Toast.LENGTH_LONG)
							.show();
				}
			}
		}
	}
	
    private boolean  checkFileSignature(String filePath, String targetPath){
        boolean valid = false;

        unzip(filePath, targetPath);
        
        try {
            String cfgFilename = mContext.getResources().getString(R.string.gapps_installer_config_file);
            String fileCfgExt = mContext.getResources().getString(R.string.gapps_installer_cfg);
            String fileSigExt = mContext.getResources().getString(R.string.gapps_installer_sig);
            
            PublicKey pubKey = RSAUtils.readPublicKeyFromPemFormat(mContext, R.raw.public_key);
            byte[] sign = RSAUtils.readSignature(targetPath + cfgFilename + fileSigExt);
            valid =  RSAUtils.verifySignature(targetPath + cfgFilename + fileCfgExt, RSAUtils.SIGNATURE_ALGORITHM, sign, pubKey);
        } catch (CertificateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return valid;
    }
    
    public void unzip(String filePath, String targetPath) {
        new File(targetPath).mkdirs();
        try {
            FileInputStream fin = new FileInputStream(filePath);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;

            while ((ze = zin.getNextEntry()) != null) {
                Log.d(TAG, "Unzipping " + ze.getName());

                if (ze.isDirectory()) {
                    _dirChecker(ze.getName(), targetPath);
                } else {
                    FileOutputStream fout = new FileOutputStream(targetPath + ze.getName());
                    byte buffer[] = new byte[2048];

                    int count = 0;

                    while ((count = zin.read(buffer)) != -1) {
                        fout.write(buffer, 0, count);
                    }

                    zin.closeEntry();
                    fout.close();
                }
            }
            zin.close();
            fin.close();
        } catch (Exception e) {
            Log.e("Decompress", "unzip", e);
        }
    }
    
    private void _dirChecker(String dir, String location) {
        File f = new File(location + dir);

        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }
}
