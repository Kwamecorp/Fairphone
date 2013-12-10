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

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class KWSprite
{
    public float x = 0, y = 0;
    public float rotation = 0;
    public float width = 100, height = 100;
    public float pivotX = 0.5f, pivotY = 0.5f;
    public float scaleX = 1, scaleY = 1;
    public float alpha = 1;
    public Drawable drawable = null;
    
    private KWSprite parent=null;
    private LinkedList<KWSprite> children = new LinkedList<KWSprite>();
    boolean matrixUpdated = false;
    Matrix matrix = new Matrix();
    float finalAlpha;
    
    public KWSprite()
    {
        x = 0;
        y = 0;
        rotation = 0;
        width = 100;
        height = 100;
        pivotX = 0.0f;
        pivotY = 0.0f;
        scaleX = 1;
        scaleY = 1;
        alpha = 1;
        drawable = null;
        matrixUpdated = false;
    }
    
    public void clearTransform(boolean clearChildrenTransform)
    {
        x=0;
        y=0;
        rotation=0;
        pivotX=0;
        pivotX=y;
        scaleX = 1;
        scaleY = 1;
        alpha=1;
        if(clearChildrenTransform)
        {
            for(KWSprite sprite: children)
            {
                sprite.clearTransform(true);
            }
        }
    }
    
    public KWSprite(KWSprite src)
    {
        copy(src);
    }
    
    public void copy(KWSprite src)
    {
        x=src.x;
        y=src.y;
        rotation=src.rotation;
        width=src.width;
        height=src.height;
        pivotX=src.pivotX;
        pivotY=src.pivotY;
        scaleX=src.scaleX;
        scaleY=src.scaleY;
        alpha=src.alpha;
        drawable=src.drawable;
        matrixUpdated = false;
    }
    

    public void applySizeFromDrawable()
    {
        if(drawable!=null)
        {
            int widthI = drawable.getIntrinsicWidth();
            int heightI = drawable.getIntrinsicHeight();
            if(widthI>=0)
            {
                width = widthI;
            }
            if(heightI>=0)
            {
                height = heightI;
            }
        }
    }
    
    
    public void addChild(KWSprite sprite)
    {
        if(sprite.parent!=null)
        {
            sprite.parent.removeChild(sprite);
        }
        
        children.add(sprite);
        sprite.parent = this;
    }
    
    public void removeChild(KWSprite sprite)
    {
        if(sprite.parent==this)
        {
            sprite.parent = null;
            children.remove(sprite);
        }
    }
    
    public void resetMatrix()//only needed to call in parent
    {
        matrixUpdated = false;
        for(KWSprite sprite: children)
        {
            sprite.resetMatrix();
        }
    }
    
    public void updateMatrix()
    {
        if(!matrixUpdated)
        {
            finalAlpha = alpha;
            matrixUpdated = true;
            matrix.reset();
            matrix.postScale(scaleX, scaleY);
            matrix.postRotate(rotation);
            matrix.postTranslate(x, y);
            if(parent!=null)
            {
                parent.updateMatrix();
                matrix.postConcat(parent.matrix);
                finalAlpha*=parent.finalAlpha;
            }
        }
    }
    
    public void draw(Canvas canvas, Paint paint)
    {
        updateMatrix();
        if (drawable != null)
        {
            if(finalAlpha>0)
            {
                canvas.save(Canvas.MATRIX_SAVE_FLAG);
                canvas.concat(matrix);
                int left = (int) (-width * pivotX);
                int right = (int) (width * (1 - pivotX));
                int top = (int) (-height * pivotY);
                int bottom = (int) (height * (1 - pivotY));
                drawable.setBounds(left, top, right, bottom);
                drawable.setAlpha((int) (255 * finalAlpha));
                drawable.draw(canvas);
                canvas.restore();
            }
        }
    }
}
