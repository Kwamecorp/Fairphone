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

import java.util.Date;

import android.content.ComponentName;

/**
 * Represents the run data for a specific application in the system.
 * It contains data for fast access like the component name and the icon.
 * All the info can still be obtained via the Application info.
 * 
 */
public class ApplicationRunInformation
{

	private static final String COMPONENT_NAME_SEPARATOR = ";";
	private ComponentName 	mComponentName;
	private int 			mRunCount;
    private Date			mLastExecution;
    
    /**
     * Create a base count zero Application Run information.
     * 
     * @param component The ComponentName of the application
     */
    public ApplicationRunInformation(ComponentName component){
    	this(component, 0);
    }
    
    /**
     * Create a application run information with a specific value for the count
     * the count value must be zero or above. 
     * 
     * @param component The ComponentName of the application
     * @param count the number of run times (used when starting)
     */
    public ApplicationRunInformation(ComponentName component, int count) {
    	if(component == null){
    		throw new IllegalArgumentException("Invalid value for ComponentName");
    	}
    	
    	setComponentName(component);
    	
    	if(count < 0){
    		throw new IllegalArgumentException("Run count cannot be negative");
    	}
    	
    	mRunCount = count;
	}

	public int getCount(){
    	return mRunCount;
    }
    
    public void incrementCount(){
    	mRunCount++;
    }
    
    public void decrementCount(){
    	if(mRunCount > 0){
    		mRunCount--;
    	}
    }

	public ComponentName getComponentName() {
		return mComponentName;
	}

	private void setComponentName(ComponentName component) {
		this.mComponentName = new ComponentName(component.getPackageName(), component.getClassName());
	}

	public Date getLastExecution() {
		return mLastExecution;
	}

	public void setLastExecution(Date lastExecution) {
		this.mLastExecution = lastExecution;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mComponentName == null) ? 0 : mComponentName.hashCode());
		result = prime * result
				+ ((mLastExecution == null) ? 0 : mLastExecution.hashCode());
		result = prime * result + mRunCount;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApplicationRunInformation other = (ApplicationRunInformation) obj;
		if (mComponentName == null) {
			if (other.mComponentName != null)
				return false;
		} else if (!mComponentName.equals(other.mComponentName))
			return false;
		if (mLastExecution == null) {
			if (other.mLastExecution != null)
				return false;
		} else if (!mLastExecution.equals(other.mLastExecution))
			return false;
		if (mRunCount != other.mRunCount)
			return false;
		return true;
	}

	public void resetCount() {
		mRunCount = 0;
	}
	
	/**
	 * Serializes a component in order to be used has a map key
	 * @param mComponentName component to serialize
	 * @return the serialized component
	 */
	public static String serializeComponentName(ComponentName mComponentName) {
		StringBuffer sb = new StringBuffer();
    	
    	sb.append(mComponentName.getPackageName()).append(COMPONENT_NAME_SEPARATOR).append(mComponentName.getClassName());
    	
		return sb.toString();
	}
	
	/**
	 * Transforms a string into a ComponentName
	 * @param componentNameString serialized component
	 * @return the ComponentName object
	 */
	public static ComponentName deserializeComponentName(String componentNameString) {
		String[] strings = componentNameString.split(COMPONENT_NAME_SEPARATOR);
    	
		return strings.length == 2 ? new ComponentName(strings[0],strings[1]): null;
	}

}
