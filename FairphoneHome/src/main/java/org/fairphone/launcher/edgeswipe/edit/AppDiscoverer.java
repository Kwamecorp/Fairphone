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

import android.content.ComponentName;
import android.util.Log;

public class AppDiscoverer {
	private static AppDiscoverer _instance = new AppDiscoverer();
	
	private ArrayList<ApplicationInfo> _allApps;

	public static AppDiscoverer getInstance() {
		return _instance;
	}

	private AppDiscoverer() {
		_allApps = new ArrayList<ApplicationInfo>(0); 
	}

	public void loadAllApps(ArrayList<ApplicationInfo> allApps ) {
		_allApps = new ArrayList<ApplicationInfo>(allApps);
	}

	public ArrayList<ApplicationInfo> getPackages(){
		return _allApps;
	}

	public ApplicationInfo getApplicationFromComponentName(ComponentName componentName) {
		
		//TODO: changes this to O(1) algorithm instead of O(N)
		for(ApplicationInfo appInfo : _allApps){
			if(componentName.getClassName().equals(appInfo.componentName.getClassName()) &&
				componentName.getPackageName().equals(appInfo.componentName.getPackageName())){
				return appInfo;
			}
		}
		
		return null;
	}
	
}
