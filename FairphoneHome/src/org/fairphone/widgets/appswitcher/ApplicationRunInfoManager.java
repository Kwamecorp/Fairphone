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
package org.fairphone.widgets.appswitcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.ComponentName;
import android.util.Log;

/**
 * This class processes the count for the most used apps and the most recent.
 * 
 * @author Tiago Costa
 *
 */
public class ApplicationRunInfoManager {

	public static final int RECENT_APP_MAX_COUNT_LIMIT = 5;
	public static final int MOST_APP_MAX_COUNT_LIMIT = 6;
	public static final int MINIMAL_COUNT = 2;
	private static final String TAG = null;

	private LimitedQueue<ApplicationRunInformation> _mostUsed;
	private LimitedQueue<ApplicationRunInformation> _recentApps;
	private Map<String, ApplicationRunInformation> _appRunInfos;
	
	private int _mostUsedAppsLimit;
	private int _recentAppsLimit;

	public ApplicationRunInfoManager() {
		
		setUpLimits(MOST_APP_MAX_COUNT_LIMIT, RECENT_APP_MAX_COUNT_LIMIT);
		
		_appRunInfos = new HashMap<String, ApplicationRunInformation>();
	}
	
	public void setUpLimits(int maxMostUsed, int maxRecentApps){
		_mostUsedAppsLimit = maxMostUsed;
		_recentAppsLimit = maxRecentApps;
		
		// refactor the limits
		setUpNewLimits();
	}

	private void setUpNewLimits() {
		_mostUsed    = new LimitedQueue<ApplicationRunInformation>(_mostUsedAppsLimit);
		_recentApps  = new LimitedQueue<ApplicationRunInformation>(_recentAppsLimit);
		
		// update the information
		if(_appRunInfos != null){
			updateAppInformation();
		}
	}
	
	public void loadNewRunInformation(List<ApplicationRunInformation> allApps){
		// clear the current state
		resetState();
		
		// add application to the bag
		for(ApplicationRunInformation appInfo : allApps){
			_appRunInfos.put(ApplicationRunInformation.serializeComponentName(appInfo.getComponentName()), appInfo);
		}
		
		// update the information
		updateAppInformation();
	}
	
	public void resetState() {
		_mostUsed.clear();
		_recentApps.clear();
		_appRunInfos.clear();
	}

	public void applicationStarted(ApplicationRunInformation appInfo){
		// obtain the cached app information
		ApplicationRunInformation cachedApp = _appRunInfos.get(ApplicationRunInformation.serializeComponentName(appInfo.getComponentName()));
		// if does not exist, create one
		if(cachedApp == null){
			_appRunInfos.put(ApplicationRunInformation.serializeComponentName(appInfo.getComponentName()), appInfo);
			
			cachedApp = appInfo;
			
			cachedApp.resetCount();
		}
		
		// increment count
		cachedApp.incrementCount();
		
		Log.d( TAG, "Logging application : " + cachedApp.getComponentName() + " : " + cachedApp.getCount() );
		
		// set the current time for the last execution
		cachedApp.setLastExecution(appInfo.getLastExecution());
		
		// update the information
		updateAppInformation();
	}
	
	public void applicationRemoved(ComponentName component){
		// remove data
		ApplicationRunInformation appInfo = _appRunInfos.remove(ApplicationRunInformation.serializeComponentName(component));
		
		// if does not exist return
		if(appInfo == null){
			return;
		}
		
		// if its being used in the lists refactor the lists
		if(_mostUsed.contains(appInfo) || _recentApps.contains(appInfo)){
			updateAppInformation();
		}
	}

	private void updateAppInformation() {
		_mostUsed.clear();
		_recentApps.clear();
		
		// most used
		// calculate the most used
		for(ApplicationRunInformation current : _appRunInfos.values()){
			
			if(current.getCount() >= MINIMAL_COUNT){
				addByCount(current, _mostUsed, _mostUsedAppsLimit);
			}
		}
		
		printMostUsedApps();
		
		// calculate the most recent
		for(ApplicationRunInformation current : _appRunInfos.values()){
			if(!_mostUsed.contains(current)){
				addByDate(current, _recentApps, _recentAppsLimit);
			}
		}
		
		printRecentApps();
	}

	private void printRecentApps() {
		for(ApplicationRunInformation current : _recentApps){
			Log.d(TAG, "Fairphone RecentApps - " + current);
		}
	}

	private void printMostUsedApps() {
		for(ApplicationRunInformation current : _mostUsed){
			Log.d(TAG, "Fairphone MostUsed - " + current);
		}
	}

	private static void addByDate(ApplicationRunInformation info, LimitedQueue<ApplicationRunInformation> queue, int limit) {
		for (int insertIdx = 0; insertIdx < queue.size(); insertIdx++)
        {
            if (queue.get(insertIdx).getLastExecution().before(info.getLastExecution()))
            {
            	queue.add(insertIdx, info);

                return;
            }
        }

		if(queue.size() < limit){
        	queue.addLast(info);
        }
	}

	private static void addByCount(ApplicationRunInformation info, LimitedQueue<ApplicationRunInformation> queue, int limit)
    {
        for (int insertIdx = 0; insertIdx < queue.size(); insertIdx++)
        {
        	Log.d(TAG, "Fairphone - Contacting ... " + queue.get(insertIdx));
        	if(info.getCount() > queue.get(insertIdx).getCount() ){
        		Log.d(TAG, "FairPhone - Qs : " + queue.size() + " : Most Used : Adding " + info.getComponentName() + " to position " + insertIdx);
        		queue.add(insertIdx, info);

        		return;
        	}
        }

        Log.d(TAG, "Fairphone - Qs : " + queue.size() + " : Most Used : Adding " + info.getComponentName() + " to first position ");
        if(queue.size() < limit){
        	queue.addLast(info);
        }
    }

	private static class LimitedQueue<E> extends LinkedList<E> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8174761694444365605L;
		private final int limit;

		public LimitedQueue(int limit) {
			this.limit = limit;
		}

		@Override
		public void add(int idx, E o) {
			super.add(idx, o);
			
			while (size() > limit) {
				super.removeLast();
			}
		}
		
		@Override
		public boolean add(E o) {
			super.addLast(o);
			while (size() > limit) {
				super.removeLast();
			}
			return true;
		}
	}

	public List<ApplicationRunInformation> getRecentApps() {
		
		
		
		Log.d(TAG, "Fairphone - Getting recent apps... " + _recentApps.size() );
		return _recentApps;
	}
	
	public List<ApplicationRunInformation> getMostUsedApps() {
		Log.d(TAG, "Fairphone - Getting most Used apps... " + _mostUsed.size() );
		
		
		
		return _mostUsed;
	}

	public int getMostUsedAppsLimit() {
		return _mostUsedAppsLimit;
	}

	public int getRecentAppsLimit() {
		return _recentAppsLimit;
	}

	public List<ApplicationRunInformation> getAllAppRunInfo() {
		return new ArrayList<ApplicationRunInformation>(_appRunInfos.values());
	}
	
	public void setAllRunInfo(List<ApplicationRunInformation> allApps){
		resetState();
		
		for(ApplicationRunInformation app : allApps){
			_appRunInfos.put(ApplicationRunInformation.serializeComponentName(app.getComponentName()), app);
		}
		
		updateAppInformation();
	}

}
