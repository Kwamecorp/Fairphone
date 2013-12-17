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
import org.fairphone.launcher.R;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class AllAppsListAdapter extends BaseAdapter
{    
    private Activity context;
    private ArrayList<ApplicationInfo> allApps;
    
    static class ViewHolder
    {
        public TextView appName;
        public ImageView appImage;
    }

    public AllAppsListAdapter(Activity context)
    {
        this.context = context;
    }

    public void setAllApps(ArrayList<ApplicationInfo> allApps)
    {
        this.allApps = allApps;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View rowView = convertView;
        if (rowView == null)
        {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.fp_favorites_all_apps_list_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.appName = (TextView) rowView.findViewById(R.id.appText);
            viewHolder.appImage = (ImageView) rowView.findViewById(R.id.appPicture);
            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();
        ApplicationInfo applicationInfo = allApps.get(position);
        holder.appName.setText(applicationInfo.getApplicationTitle());
        holder.appImage.setImageDrawable(new BitmapDrawable(context.getResources(), applicationInfo.iconBitmap));
        
        return rowView;
    }

    @Override
    public int getCount()
    {
        return allApps.size();
    }

    @Override
    public Object getItem(int position)
    {
        return allApps.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }
}
