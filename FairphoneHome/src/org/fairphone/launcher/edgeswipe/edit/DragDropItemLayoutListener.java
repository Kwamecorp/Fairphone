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

import org.fairphone.launcher.ApplicationInfo;

import android.widget.RelativeLayout;

public interface DragDropItemLayoutListener {
	void setupFavoriteIcon(RelativeLayout rla, ApplicationInfo info, int idx, boolean performAnimation);
	void showAllAppsRemoveZone();
	void hideAllAppsRemoveZone();
	void toggleAllAppRemoveZoneRedGlow(float pointerX, float pointerY);
	void showAllAppsRemoveZoneRedGlow();
	void hideAllAppsRemoveZoneRedGlow();
}
