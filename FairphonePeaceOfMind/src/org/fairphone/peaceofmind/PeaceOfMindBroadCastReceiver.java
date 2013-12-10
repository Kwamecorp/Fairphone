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

import org.fairphone.fairphonepeaceofmindapp.R;
import org.fairphone.peaceofmind.data.PeaceOfMindRun;
import org.fairphone.peaceofmind.data.PeaceOfMindStats;
import org.fairphone.peaceofmind.widget.WidgetProvider;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

public class PeaceOfMindBroadCastReceiver extends BroadcastReceiver {

	private static final String TAG = PeaceOfMindBroadCastReceiver.class.getSimpleName();

	private static final int PEACE_OF_MIND_ON_NOTIFICATION = 0;
	private static final int PEACE_OF_MIND_INTERRUPTED_NOTIFICATION = 1;

	private PeaceOfMindStats mCurrentStats;
	private SharedPreferences mSharedPreferences;

	private Context mContext;
	
	private IDeviceController mDeviceController;

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		setupDeviceController();
		
		String action = intent.getAction();
		if (action != null) {
			// obtains the piece of mind data from shared preferences
			mSharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(context);

			mCurrentStats = PeaceOfMindStats
					.getStatsFromSharedPreferences(mSharedPreferences);

			if (action.equals(PeaceOfMindActivity.UPDATE_PEACE_OF_MIND)) {
				updateTargetTime(intent);
			} else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
				//only react to this if the app is running
				if(mCurrentStats.mIsOnPeaceOfMind){
					Bundle extras = intent.getExtras();
					//if the intent was sent by the system end Peace of mind
					if(!extras.containsKey(AirplaneModeToggler.PEACE_OF_MIND_TOGGLE)){
						endPeaceOfMind(true);
					}
				}
			}  else if (Intent.ACTION_SHUTDOWN.equals(action) && mCurrentStats.mIsOnPeaceOfMind) {
				endPeaceOfMind(true);
			} else {
				performTimeTick();
			}
		} else {
			return;
		}
		// update the widgets
		updateWidget(context);

	}
	
	private void setupDeviceController() {
		mDeviceController = new AirplaneModeDeviceController(mContext);
	}
	
	private void updateWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
        if (appWidgetIds.length > 0)
        {
            new WidgetProvider().onUpdate(context, appWidgetManager, appWidgetIds);
        }
	}

	private void performTimeTick() {
		 long currentTime = System.currentTimeMillis();
		
		 long passedTime = 0;
		
		 if (mCurrentStats.mLastTimePinged != 0) {
			 if(mCurrentStats.mLastTimePinged >= currentTime){
				 endPeaceOfMind(false);
				 return;
			 }else{
				 passedTime += currentTime - mCurrentStats.mLastTimePinged;
			 }
		 }
		
		 mCurrentStats.mLastTimePinged = currentTime;
		 if (mCurrentStats.mIsOnPeaceOfMind) {
			 
			 mCurrentStats.mCurrentRun.mPastTime += passedTime;
			 
			 if(mCurrentStats.mCurrentRun.mPastTime >= mCurrentStats.mCurrentRun.mTargetTime){
				 endPeaceOfMind(false);
				 return;
			 }
			 
			 
			 
		 } 
		
		 PeaceOfMindStats.saveToSharedPreferences(mCurrentStats, mSharedPreferences);
		 
		 // send broadcast to application receiver
		 if (mCurrentStats.mIsOnPeaceOfMind) {
			Intent tickIntent = new Intent(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_TICK);
			tickIntent.putExtra(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_TARGET_TIME, mCurrentStats.mCurrentRun.mTargetTime);
			tickIntent.putExtra(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_PAST_TIME, mCurrentStats.mCurrentRun.mPastTime);
			
			mContext.sendBroadcast(tickIntent);
		 }
	}

	private void startPeaceOfMind(long targetTime) {
		long currentTime = System.currentTimeMillis();
		
		mCurrentStats.mIsOnPeaceOfMind = true;
		mCurrentStats.mLastTimePinged = currentTime;
		
		mCurrentStats.mCurrentRun = new PeaceOfMindRun();
		mCurrentStats.mCurrentRun.mTimeStarted = currentTime;
		mCurrentStats.mCurrentRun.mPastTime = 0;
		mCurrentStats.mCurrentRun.mTargetTime = targetTime;
		
		PeaceOfMindStats.saveToSharedPreferences(mCurrentStats, mSharedPreferences);
		
		mDeviceController.startPeaceOfMind();
		
		// send broadcast to application receiver
		Intent intent = new Intent(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_STARTED);
		intent.putExtra(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_TARGET_TIME, targetTime);
		
		mContext.sendBroadcast(intent);
	}

	private void updateTargetTime(Intent intent) {

		long newTargetTime = intent.getExtras().getLong(
				PeaceOfMindActivity.BROADCAST_TARGET_PEACE_OF_MIND);

		if(newTargetTime == 0){
			if(mCurrentStats.mIsOnPeaceOfMind){
				endPeaceOfMind(false);
			}
		}else if (mCurrentStats.mIsOnPeaceOfMind) {
			if (mCurrentStats.mCurrentRun.mPastTime < newTargetTime) {
				mCurrentStats.mCurrentRun.mTargetTime = newTargetTime;
				
				PeaceOfMindStats.saveToSharedPreferences(mCurrentStats, mSharedPreferences);

				Intent updateIntent = new Intent(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_UPDATED);
				updateIntent.putExtra(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_TARGET_TIME, mCurrentStats.mCurrentRun.mTargetTime);
				updateIntent.putExtra(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_PAST_TIME, mCurrentStats.mCurrentRun.mPastTime);
				
				mContext.sendBroadcast(updateIntent);
			} else {
				endPeaceOfMind(false);
			}
		}else {
			startPeaceOfMind(newTargetTime);
			setPeaceOfMindIconInNotificationBar(true, false); 
		}
	}

	/**
	 * Sets the Peace of mind icon on the notification bar
	 * @param putIcon if true the icon is put otherwise it is removed
	 * @param wasInterrupted when true, an extra notification is sent to inform the user that Peace of mind was ended
	 */
	private void setPeaceOfMindIconInNotificationBar(boolean putIcon, boolean wasInterrupted) {

		NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE); 
		
		if(putIcon){
			
			//just in case the user didn't clear it
			manager.cancel(PEACE_OF_MIND_INTERRUPTED_NOTIFICATION);
			
			NotificationCompat.Builder builder =  
		            new NotificationCompat.Builder(mContext)
		            .setSmallIcon(R.drawable.peace_system_bar_icon)  
		            .setContentTitle(mContext.getResources().getString(R.string.app_name))  
		            .setContentText(mContext.getResources().getString(R.string.peace_on_notification));  
	 
		    Intent resultIntent = new Intent(mContext, PeaceOfMindActivity.class);
	        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
	        // Adds the back stack for the Intent (but not the Intent itself)
	        stackBuilder.addParentStack(PeaceOfMindActivity.class);
	        // Adds the Intent that starts the Activity to the top of the stack
	        stackBuilder.addNextIntent(resultIntent);
	        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
	        
		    builder.setContentIntent(resultPendingIntent);  
	
		    Notification notificationWhileRunnig = builder.build();
		    notificationWhileRunnig.flags |= Notification.FLAG_NO_CLEAR;
		    // Add notification   
		    manager.notify(PEACE_OF_MIND_ON_NOTIFICATION, notificationWhileRunnig);
		    
		}else{
			manager.cancel(PEACE_OF_MIND_ON_NOTIFICATION);
			
			//send a notification saying that the peace was ended 
			if(wasInterrupted){
				NotificationCompat.Builder builder =  
			            new NotificationCompat.Builder(mContext)
			            .setSmallIcon(R.drawable.peace_system_bar_icon)
			            .setAutoCancel(true)
			            .setContentTitle(mContext.getResources().getString(R.string.app_name))  
			            .setContentText(mContext.getResources().getString(R.string.peace_off_notification))
			            .setTicker(mContext.getResources().getString(R.string.peace_off_notification));

				manager.notify(PEACE_OF_MIND_INTERRUPTED_NOTIFICATION, builder.build());
			}
		}
	}

	private void endPeaceOfMind(boolean wasInterrupted) {

		mCurrentStats.mIsOnPeaceOfMind = false;
		mCurrentStats.mLastTimePinged = 0;
		
		if(mCurrentStats.mCurrentRun != null){
			mCurrentStats.mCurrentRun.mTimeStarted = 0;
			mCurrentStats.mCurrentRun.mPastTime = 0;
			mCurrentStats.mCurrentRun.mTargetTime = 0;
			mCurrentStats.mCurrentRun = null;
		}
		
		PeaceOfMindStats.saveToSharedPreferences(mCurrentStats, mSharedPreferences);

		mDeviceController.endPeaceOfMind();
		
		Intent endIntent = new Intent(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_ENDED);
		mContext.sendBroadcast(endIntent);
		
		setPeaceOfMindIconInNotificationBar(false, wasInterrupted);
	}
}
