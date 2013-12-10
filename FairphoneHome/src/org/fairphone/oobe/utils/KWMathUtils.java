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

/**
 * FIXME - Needs cleanup and comments!
 */
public class KWMathUtils
{
    public static double degToRad(double angleDeg)
    {
        return angleDeg/180*Math.PI;
    }
    
    public static double radToDeg(double angleRad)
    {
        return angleRad/Math.PI*180;
    }

    public static float speedUp(float ratio)
    {
        return ratio * ratio;
    }

    public static float slowDown(float ratio)
    {
        return 1.0f - ((ratio - 1.0f) * (ratio - 1.0f));
    }

    public static float smoothStep(float ratio)
    {
        return ratio * ratio * (3 - 2 * ratio);
    }

    public static float blend(float start, float end, float ratio)
    {
        return start + (end - start) * ratio;
    }
    
    public static float clamp(float start,float end, float value)
    {
        if(value<start)
            return start;
        if(value>end)
            return end;
        return value;
    }
    
    public static float getLongRatio(long startTime, long endTime, long time)
    {
        long duration = endTime - startTime;
        if (duration > 0)
        {
            float ratio = (float) (time-startTime) / (float) duration;
            if(ratio<=0)
                return 0;
            if(ratio>=1.0f)
                return 1.0f;
            return ratio;
        }
        return 1.0f;
    }
    
    public static float getFloatRatio(float min, float max, float value)
    {
        float dif = max - min;
        if (dif > 0)
        {
            float ratio = (float) (value-min) / (float) dif;
            if(ratio<=0)
                return 0;
            if(ratio>=1.0f)
                return 1.0f;
            return ratio;
        }
        return 1.0f;
    }

    public static float getSpeedFromPositionAndTimeI(float start, float end, long duration)
    {
        return (end-start)/((float)duration/1000.0f);
    }
    
    public static float getSpeedFromPositionAndTimeF(float start, float end, float duration)
    {
        return (end-start)/duration;
    }
    
    public static double getDistance(float x1, float y1, float x2, float y2)
    {
        float xd=x1-x2;
        float yd=y1-y2;
        
        return Math.sqrt(xd*xd+yd*yd);
    }
    
    public static long calcTimeFromVelocityAndLength(float velocity, float length)
    {
        return (long) (Math.abs(2 * length / velocity) * 1000);
    }

    public static float calcLengthFromTimeAndVelocity(long time, float velocity)
    {
        float timef = (float) time / 1000.0f;
        return (timef * velocity / 2.0f);
    }
}
