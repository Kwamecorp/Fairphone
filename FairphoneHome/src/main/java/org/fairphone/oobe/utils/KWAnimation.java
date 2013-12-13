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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;


import android.view.animation.Interpolator;

public class KWAnimation
{  
    public interface KWAnimationListener
    {
        public void onAnimationStart(KWAnimation animation);
        public void onAnimationUpdate(KWAnimation animation, float curAnimationProgress);
        public void onAnimationEnd(KWAnimation animation);
    }
    
    public enum KWValueType
    {
        X,Y,Rotation,Scale,ScaleX,ScaleY,PivotX,PivotY, Alpha, Width,Height
    };
    

    public static class KWValueAnimation
    {
        KWValueType type;
        ArrayList<KWKeyframe> keyframes = new ArrayList<KWAnimation.KWKeyframe>();
        long duration = 0;
        
        KWValueAnimation(KWValueType type)
        {
            this.type = type;
        }
        
        public KWValueAnimation addKeyframe(float value, long time, Interpolator interpolator)
        {
            if(time>duration)
            {
                duration = time;
            }
            keyframes.add(new KWKeyframe(time,value,interpolator));
            Collections.sort(keyframes,new KWKeyComparator());
            return this;
        }
        
        public float getValueAt(long time)
        {
            if(keyframes.isEmpty())
            {
                return 0;
            }
            
            int key=0;
            
            for(int i=0;i<keyframes.size();i++)
            {
                key = i;
                if(keyframes.get(i).time>time)
                {
                    break;
                }
            }
            
            KWKeyframe keyA = keyframes.get(key);
            KWKeyframe keyB = keyframes.get(key);
            
            if(key>0)
            {
                keyA = keyframes.get(key-1);
            }
            
            float ratio = KWMathUtils.getLongRatio(keyA.time, keyB.time, time);
            if(keyB.interpolator!=null)
            {
                ratio = keyB.interpolator.getInterpolation(ratio);
            }
            float val = keyA.value+(keyB.value-keyA.value)*ratio;
            return val;
        }
        
        public void applyValueAt(long time, KWSprite sprite)
        {
            switch(type)
            {
                case X:
                {
                    sprite.x = getValueAt(time);
                }
                break;
                
                case Y:
                {
                    sprite.y = getValueAt(time);
                }
                break;
                
                case Rotation:
                {
                    sprite.rotation = getValueAt(time);
                }
                break;
                
                case Scale:
                {
                    sprite.scaleX = getValueAt(time);
                    sprite.scaleY = sprite.scaleX;
                }
                break;
                
                case ScaleX:
                {
                    sprite.scaleX = getValueAt(time);
                }
                break;
                
                case ScaleY:
                {
                    sprite.scaleY = getValueAt(time);
                }
                break;
                
                case PivotX:
                {
                    sprite.pivotX = getValueAt(time);
                }
                break;
                
                case PivotY:
                {
                    sprite.pivotY = getValueAt(time);
                }
                break;
                
                case Alpha:
                {
                    sprite.alpha = getValueAt(time);
                }
                break;
                
                case Width:
                {
                    sprite.width = getValueAt(time);
                }
                break;
                
                case Height:
                {
                    sprite.height = getValueAt(time);
                }
                break;
            }
        }
    }
    
    private static class KWKeyComparator implements Comparator<KWKeyframe>
    {
        @Override
        public int compare(KWKeyframe lhs, KWKeyframe rhs)
        {
            long dif = (lhs.time-rhs.time);
            return dif>0?1:dif<0?-1:0;
        }
    }
    
    private static class KWKeyframe
    {
        long  time;
        float value;
        Interpolator interpolator;
        private KWKeyframe(long time, float value, Interpolator interpolator)
        {
            this.time = time;
            this.value = value;
            this.interpolator = interpolator;
        }
    }
    
    private long curAnimTime=0;
    private boolean isRunning = false;
    private LinkedList<KWValueAnimation> animations = new LinkedList<KWAnimation.KWValueAnimation>();
    private KWAnimationListener listener;
    private KWSprite sprite;
    
    
    public KWAnimation(KWSprite animatedSprite)
    {
        curAnimTime=0;
        this.sprite = animatedSprite;
    }
    
    public KWValueAnimation addValueAnimation(KWValueType type)
    {
        KWValueAnimation anim = new KWValueAnimation(type);
        animations.add(anim);
        return anim;
    }
    
    public void setAnimationListener(KWAnimationListener listener)
    {
        this.listener = listener;
    }
    
    public KWSprite getSprite()
    {
        return sprite;
    }
    
    public boolean isRunning()
    {
        return isRunning;
    }
    
    public long getCurAnimTime()
    {
        return curAnimTime;
    }
    
    public void start()
    {
        isRunning = true;
        curAnimTime=0;
        
        if(listener!=null)
        {
            listener.onAnimationStart(this);
        }
    }
    
    public void pause()
    {
        isRunning = false;
    }
    
    public void unpause()
    {
        isRunning = true;
    }
    
    public void stop()
    {
        reset();
        isRunning = false;
    }
    
    public void reset()
    {
        curAnimTime = 0;
    }
    
    public void update(long dt)
    {
        if(isRunning)
        {
            curAnimTime+=dt;
            long maxDuration = 0;
            for(KWValueAnimation anim: animations)
            {
                anim.applyValueAt(curAnimTime, sprite);
                if(anim.duration>maxDuration)
                {
                    maxDuration = anim.duration;
                }
            }
            if(curAnimTime>=maxDuration)
            {
                curAnimTime = maxDuration;
                isRunning = false;
                if(listener!=null)
                {
                    listener.onAnimationEnd(this);
                }
            }
            else
            {
                if(listener!=null)
                {
                    float curAnimationProgress = KWMathUtils.getLongRatio(0, maxDuration, curAnimTime);
                    listener.onAnimationUpdate(this, curAnimationProgress);
                }
            }
        }
    }
}
