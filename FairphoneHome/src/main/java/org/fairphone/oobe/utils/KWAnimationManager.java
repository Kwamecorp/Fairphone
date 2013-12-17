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

import java.util.LinkedList;

public class KWAnimationManager
{

    LinkedList<KWAnimation> animations = new LinkedList<KWAnimation>();
    long prevFrameTime=0;
    
    
    public KWAnimationManager()
    {
        prevFrameTime = System.currentTimeMillis();
    }
    
    public void addAnimation(KWAnimation animation)
    {
        animations.add(animation);
    }
    
    public void removeAnimation(KWAnimation animation)
    {
        animations.remove(animation);
    }
    
    public boolean update()//note: must be called regularly
    {
        long curTime = System.currentTimeMillis();
        long dt = curTime-prevFrameTime;
        prevFrameTime = curTime;
        
        if(dt>100)
        {
            dt=1000/30;
        }
        
        boolean hasAnimsRunning = false;
        for(KWAnimation anim: animations)
        {
            if(anim.isRunning())
            {
                hasAnimsRunning = true;
                anim.update(dt);
            }
        }
        return hasAnimsRunning;
    }
}
