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
package org.fairphone.oobe.utils;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Fonts manager class.
 * 
 * Implemented as a singleton.
 * 
 * Prepares the fonts used within the application.
 */
public class KWFontsManager
{
    private static final String HANNAH_REGULAR = "fonts/YWFT-Hannah-Regular.ttf";

    private static KWFontsManager fontsManager = null;

    private static Typeface hannahRegular;

    /**
     * Get the singleton instance of the fonts manager.
     */
    public static KWFontsManager get()
    {
        if (fontsManager == null)
        {
            fontsManager = new KWFontsManager();
        }

        return fontsManager;
    }

    /**
     * Setup the fonts used within the application.
     */
    public static void prepareFonts(Context context, ViewGroup root)
    {
        hannahRegular = Typeface.createFromAsset(context.getAssets(), HANNAH_REGULAR);

        setFontInTag(root, "fontHannah", hannahRegular);
    }

    public static void setFontInTag(ViewGroup root, String tag, Typeface font)
    {
        ArrayList<View> regularViews = getViewsByTag(root, tag);
        for (View view : regularViews)
        {
            if (view.getClass() == TextView.class)
            {
                TextView text = (TextView) view;
                text.setTypeface(font);
            }
            else if (view.getClass() == Button.class)
            {
                Button text = (Button) view;
                text.setTypeface(font);
            }
            else if (view.getClass() == ToggleButton.class)
            {
                ToggleButton text = (ToggleButton) view;
                text.setTypeface(font);
            }
            else if (view.getClass() == EditText.class)
            {
                EditText text = (EditText) view;
                text.setTypeface(font);
            }
        }
    }

    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag)
    {
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) // recursive search
                views.addAll(getViewsByTag((ViewGroup) child, tag));

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag))
                views.add(child);
        }
        return views;
    }
}
