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
package org.fairphone.peaceofmind.data;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PeaceOfMindStats {
	public static final long MAX_TIME = 3 * 60 * 60 * 1000;
	
	
	private static final String PM_STATS_LAST_TIME_PINGED = "PM_STATS_LAST_TIME_PINGED";
	private static final String PM_STATS_IS_IN_PEACE_OF_MIND = "PM_STATS_IS_IN_PEACE_OF_MIND";
	private static final String PM_STATS_RUN_PAST_TIME = "PM_STATS_RUN_PAST_TIME";
	private static final String PM_STATS_RUN_TARGET_TIME = "PM_STATS_RUN_TARGET_TIME";
	private static final String PM_STATS_RUN_START_TIME = "PM_STATS_RUN_START_TIME";
	
	public boolean mIsOnPeaceOfMind;
	public long mLastTimePinged;
	
	public PeaceOfMindRun mCurrentRun;
	
	public static PeaceOfMindStats getStatsFromSharedPreferences(SharedPreferences preferences){
		PeaceOfMindStats stats = new PeaceOfMindStats();
		
		stats.mIsOnPeaceOfMind = preferences.getBoolean(PM_STATS_IS_IN_PEACE_OF_MIND, false);
		stats.mLastTimePinged = preferences.getLong(PM_STATS_LAST_TIME_PINGED, 0);
		
		if(stats.mIsOnPeaceOfMind){
			stats.mCurrentRun = new PeaceOfMindRun();
			stats.mCurrentRun.mPastTime = preferences.getLong(PM_STATS_RUN_PAST_TIME, 0);
			stats.mCurrentRun.mTargetTime = preferences.getLong(PM_STATS_RUN_TARGET_TIME, 0);
			stats.mCurrentRun.mTimeStarted = preferences.getLong(PM_STATS_RUN_START_TIME, 0);
		}
		
		return stats;
	}

	public static void saveToSharedPreferences(PeaceOfMindStats stats, SharedPreferences preferences) {
		Editor editor = preferences.edit();
		
		editor.putBoolean(PM_STATS_IS_IN_PEACE_OF_MIND, stats.mIsOnPeaceOfMind);
		editor.putLong(PM_STATS_LAST_TIME_PINGED, stats.mLastTimePinged);
		
		if(stats.mIsOnPeaceOfMind){
			editor.putLong(PM_STATS_RUN_PAST_TIME, stats.mCurrentRun.mPastTime);
			editor.putLong(PM_STATS_RUN_TARGET_TIME, stats.mCurrentRun.mTargetTime);
			editor.putLong(PM_STATS_RUN_START_TIME, stats.mCurrentRun.mTimeStarted);
		}
		
		editor.commit();
	}
}
