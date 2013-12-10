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

import java.util.Collection;
import java.util.LinkedList;

import org.fairphone.oobe.utils.KWAnimation.KWAnimationListener;

import android.util.Log;

public class KWAnimationGroup implements KWAnimationListener
{
    public interface KWAnimationGroupListener
    {
        public void onAnimationGroupStarted(KWAnimationGroup group);
        public void onAnimationGroupFinished(KWAnimationGroup group);
        
    }
    
    int startedAnimNum=0;
    KWAnimationManager manager;
    LinkedList<KWAnimation> animations = new LinkedList<KWAnimation>();
    KWAnimationGroupListener listener=null;
    
    public KWAnimationGroup(KWAnimationManager manager)
    {
        this.manager = manager;
        startedAnimNum = 0;
    }
    
    public void setAnimationGroupListener(KWAnimationGroupListener listener)
    {
        this.listener = listener;
    }
    
    public void addAnimation(KWAnimation anim)
    {
        animations.add(anim);
        anim.setAnimationListener(this);
        manager.addAnimation(anim);
    }
    
    public void removeAnimation(KWAnimation anim)
    {
        animations.remove(anim);
        anim.setAnimationListener(null);
    }
    
    public Collection<KWAnimation> getAnimations()
    {
        return animations;
    }
    
    public void start()
    {
        startedAnimNum=0;
        for(KWAnimation anim: animations)
        {
            anim.start();
            anim.update(0);
        }
        
        if(listener!=null)
        {
            listener.onAnimationGroupStarted(this);
        }
    }
    
    public void stop()
    {
        for(KWAnimation anim: animations)
        {
            anim.stop();
        }
    }
    
    @Override
    public void onAnimationStart(KWAnimation animation)
    {
        startedAnimNum++;
    }

    @Override
    public void onAnimationUpdate(KWAnimation animation, float curAnimationProgress)
    {
    }

    @Override
    public void onAnimationEnd(KWAnimation animation)
    {
        startedAnimNum--;
        
        if(startedAnimNum<=0)
        {
            if(listener!=null)
            {
                listener.onAnimationGroupFinished(this);
            }
        }
    }
    
}
