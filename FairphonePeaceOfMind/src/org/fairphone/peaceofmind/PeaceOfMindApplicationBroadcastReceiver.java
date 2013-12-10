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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PeaceOfMindApplicationBroadcastReceiver extends BroadcastReceiver {

	public static String PEACE_OF_MIND_PAST_TIME = "PEACE_OF_MIND_PAST_TIME";
	public static String PEACE_OF_MIND_TARGET_TIME = "PEACE_OF_MIND_TARGET_TIME";

	public static String PEACE_OF_MIND_STARTED = "PEACE_OF_MIND_STARTED";
	public static String PEACE_OF_MIND_UPDATED = "PEACE_OF_MIND_UPDATED";
	public static String PEACE_OF_MIND_ENDED = "PEACE_OF_MIND_ENDED";
	public static String PEACE_OF_MIND_TICK = "PEACE_OF_MIND_TICK";

	public interface Listener {
		void peaceOfMindTick(long pastTime, long targetTime);

		void peaceOfMindStarted(long targetTime);

		void peaceOfMindEnded();

		void peaceOfMindUpdated(long currentTime, long newTargetTime);
	}

	private Listener mListener;

	public PeaceOfMindApplicationBroadcastReceiver(Listener listener) {
		mListener = listener;
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();

		if (PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_TICK.equals(action)) {
			mListener.peaceOfMindTick(intent.getExtras().getLong(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_PAST_TIME),
									  intent.getExtras().getLong(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_TARGET_TIME));
		} else if (PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_STARTED.equals(action)) {
			mListener.peaceOfMindStarted(intent.getExtras().getLong(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_TARGET_TIME));
		} else if (PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_UPDATED.equals(action)) {
			mListener.peaceOfMindUpdated(intent.getExtras().getLong(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_PAST_TIME), 
					intent.getExtras().getLong(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_TARGET_TIME));
		} else if (PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_ENDED.equals(action)) {
			mListener.peaceOfMindEnded();
		}
	}

}
