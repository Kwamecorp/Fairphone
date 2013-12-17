/*
 * Copyright (C) 2010 The Android Open Source Project
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

package org.fairphone.launcher;

import java.util.List;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ResolveInfo;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

/**
 * We will likely flesh this out later, to handle allow external apps to place widgets, but for now,
 * we just want to expose the action around for checking elsewhere.
 */
public class InstallWidgetReceiver {
    public static final String ACTION_INSTALL_WIDGET =
            "com.android.launcher.action.INSTALL_WIDGET";
    public static final String ACTION_SUPPORTS_CLIPDATA_MIMETYPE =
            "com.android.launcher.action.SUPPORTS_CLIPDATA_MIMETYPE";

    // Currently not exposed.  Put into Intent when we want to make it public.
    // TEMP: Should we call this "EXTRA_APPWIDGET_PROVIDER"?
    public static final String EXTRA_APPWIDGET_COMPONENT =
        "com.android.launcher.extra.widget.COMPONENT";
    public static final String EXTRA_APPWIDGET_CONFIGURATION_DATA_MIME_TYPE =
        "com.android.launcher.extra.widget.CONFIGURATION_DATA_MIME_TYPE";
    public static final String EXTRA_APPWIDGET_CONFIGURATION_DATA =
        "com.android.launcher.extra.widget.CONFIGURATION_DATA";

    /**
     * A simple data class that contains per-item information that the adapter below can reference.
     */
    public static class WidgetMimeTypeHandlerData {
        public ResolveInfo resolveInfo;
        public AppWidgetProviderInfo widgetInfo;

        public WidgetMimeTypeHandlerData(ResolveInfo rInfo, AppWidgetProviderInfo wInfo) {
            resolveInfo = rInfo;
            widgetInfo = wInfo;
        }
    }

    /**
     * The ListAdapter which presents all the valid widgets that can be created for a given drop.
     */
    public static class WidgetListAdapter implements ListAdapter, DialogInterface.OnClickListener {
        private LayoutInflater mInflater;
        private Launcher mLauncher;
        private String mMimeType;
        private ClipData mClipData;
        private List<WidgetMimeTypeHandlerData> mActivities;
        private CellLayout mTargetLayout;
        private int mTargetLayoutScreen;
        private int[] mTargetLayoutPos;

        public WidgetListAdapter(Launcher l, String mimeType, ClipData data,
                List<WidgetMimeTypeHandlerData> list, CellLayout target,
                int targetScreen, int[] targetPos) {
            mLauncher = l;
            mMimeType = mimeType;
            mClipData = data;
            mActivities = list;
            mTargetLayout = target;
            mTargetLayoutScreen = targetScreen;
            mTargetLayoutPos = targetPos;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
        }

        @Override
        public int getCount() {
            return mActivities.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Context context = parent.getContext();

            // Lazy-create inflater
            if (mInflater == null) {
                mInflater = LayoutInflater.from(context);
            }

            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return mActivities.isEmpty();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            final AppWidgetProviderInfo widgetInfo = mActivities.get(which).widgetInfo;

            final PendingAddWidgetInfo createInfo = new PendingAddWidgetInfo(widgetInfo, mMimeType,
                    mClipData);
            mLauncher.addAppWidgetFromDrop(createInfo, LauncherSettings.Favorites.CONTAINER_DESKTOP,
                    mTargetLayoutScreen, null, null, mTargetLayoutPos);
        }
    }
}
