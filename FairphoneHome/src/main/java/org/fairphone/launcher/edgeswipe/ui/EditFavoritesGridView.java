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
package org.fairphone.launcher.edgeswipe.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

public class EditFavoritesGridView extends GridView
{
    public interface OnEditFavouritesIconDraggedListener
    {
        public void OnEditFavouritesIconDragged(AdapterView<?> parent, View view, int position, long id);
    }
    
    
    private float touchStartX = 0;
    private float touchStartY = 0;
    private int selectedChild = INVALID_POSITION;
    private boolean hasStartedDraggingOut = false;
    private boolean ignoreDragging = false;
    private OnEditFavouritesIconDraggedListener listener=null;
    private float xBias = 2.0f;
    private float minMoveDistance = 15;
    
    
    public EditFavoritesGridView(Context context)
    {
        super(context);
    }

    public EditFavoritesGridView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public EditFavoritesGridView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    
    
    
    
    public void setOnEditFavouritesIconDraggedListener(OnEditFavouritesIconDraggedListener listener) 
    {
        this.listener = listener;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        switch(ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                touchStartX = ev.getX();
                touchStartY = ev.getY();
                selectedChild = pointToPosition((int)touchStartX, (int)touchStartY);
                ignoreDragging = (selectedChild==INVALID_POSITION);
                hasStartedDraggingOut = false;
            }
            break;
            
            case MotionEvent.ACTION_MOVE:
            {
                if(!ignoreDragging && !hasStartedDraggingOut)
                {
                    float xDif = ev.getX()-touchStartX;
                    float yDif = ev.getY()-touchStartY;
                    
                    float absYDif = Math.abs(yDif);
                    float absXDif = Math.abs(xDif)*xBias;
                    
                    
                    if(absXDif>absYDif && xDif>minMoveDistance)//are we dragging mostly to the right?
                    {
                        hasStartedDraggingOut = true;
                        MotionEvent cancelEvent = MotionEvent.obtain(ev.getDownTime(), ev.getEventTime(), MotionEvent.ACTION_CANCEL, ev.getX(), ev.getY(), ev.getMetaState());
                        super.onTouchEvent(cancelEvent);
                        if(listener!=null)
                        {
                            View childView = getChildAt(selectedChild-getFirstVisiblePosition());
                            if(childView!=null)
                            {
                                listener.OnEditFavouritesIconDragged(this, childView, selectedChild, getAdapter().getItemId(selectedChild));
                            }
                        }
                    }
                    else if(absXDif<absYDif && absYDif>minMoveDistance)
                    {
                        ignoreDragging = true;
                    }
                }
            }
            break;
        }
        
        if(!hasStartedDraggingOut)
        {
            return super.onTouchEvent(ev);
        }
        else
        {
            return true;
        }
    }
}
