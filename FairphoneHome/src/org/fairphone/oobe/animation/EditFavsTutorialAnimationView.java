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
package org.fairphone.oobe.animation;

import org.fairphone.launcher.R;
import org.fairphone.oobe.utils.KWAnimation;
import org.fairphone.oobe.utils.KWAnimation.KWValueAnimation;
import org.fairphone.oobe.utils.KWAnimation.KWValueType;
import org.fairphone.oobe.utils.KWAnimationGroup;
import org.fairphone.oobe.utils.KWAnimationGroup.KWAnimationGroupListener;
import org.fairphone.oobe.utils.KWAnimationManager;
import org.fairphone.oobe.utils.KWSprite;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

public class EditFavsTutorialAnimationView extends FrameLayout
{

    public interface EditFavsTutorialAnimationViewListener
    {
        public void OnAnimationFinished(EditFavsTutorialAnimationView view);
    }

    private static final int SCREEN_WIDTH = 540;
    private static final int SCREEN_HEIGHT = 960;

    KWSprite spriteRoot;

    KWSprite spriteArrow;
    KWSprite spriteHand;
    KWSprite spriteHandShadow;
    KWSprite spriteAllApps;
    KWSprite spriteFavApps;
    KWSprite spriteAppIconAll1;
    KWSprite spriteAppIconFav1;
    KWSprite spriteAppIconFav2;
    KWSprite spriteAppIconFav3;
    KWSprite spriteAppIconFavSelected1;
    KWSprite spriteAppIconFavSelected2;
    KWSprite spriteAppIconFavSelected3;
    KWSprite spriteDeleteHighlight;
    KWSprite spriteDeleteX;

    Paint spritePaint;

    KWAnimationGroup animationGroupRemoveFav;
    KWAnimationGroup animationGroupAddFav;
    KWAnimationGroup animationGroupMoveFav;
    KWAnimationManager animationManager;

    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    private AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
    private AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
    long prevFrame = 0;

    EditFavsTutorialAnimationViewListener listener;

    public EditFavsTutorialAnimationView(Context context)
    {
        super(context);
        init();
    }

    public EditFavsTutorialAnimationView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public EditFavsTutorialAnimationView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    public void setEditFavsTutorialAnimationViewListener(EditFavsTutorialAnimationViewListener listener)
    {
        this.listener = listener;
    }

    public void playAddFavAnimation()
    {
        stopAnimations();
        animationGroupAddFav.start();
    }

    public void playRemoveFavAnimation()
    {
        stopAnimations();
        animationGroupRemoveFav.start();
    }

    public void playMoveFavAnimation()
    {
        stopAnimations();
        animationGroupMoveFav.start();
    }

    public void stopAnimations()
    {
        animationGroupRemoveFav.stop();
        animationGroupAddFav.stop();
        animationGroupMoveFav.stop();
    }

    private void init()
    {
        if (getBackground() == null)
        {
            setBackgroundColor(0x00000000);
        }

        spritePaint = new Paint();
        spritePaint.setColor(0xffffffff);

        spriteRoot = new KWSprite();
        spriteArrow = new KWSprite();
        spriteHand = new KWSprite();
        spriteHandShadow = new KWSprite();
        spriteAllApps = new KWSprite();
        spriteFavApps = new KWSprite();
        spriteAppIconAll1 = new KWSprite();
        spriteAppIconFav1 = new KWSprite();
        spriteAppIconFav2 = new KWSprite();
        spriteAppIconFav3 = new KWSprite();
        spriteAppIconFavSelected1 = new KWSprite();
        spriteAppIconFavSelected2 = new KWSprite();
        spriteAppIconFavSelected3 = new KWSprite();
        spriteDeleteHighlight = new KWSprite();
        spriteDeleteX = new KWSprite();

        spriteArrow.drawable = getResources().getDrawable(R.drawable.oobe_arrow_big);
        spriteArrow.applySizeFromDrawable();
        spriteHand.drawable = getResources().getDrawable(R.drawable.oobe_hand);
        spriteHand.applySizeFromDrawable();
        spriteHandShadow.drawable = getResources().getDrawable(R.drawable.oobe_hand_shadow);
        spriteHandShadow.applySizeFromDrawable();
        spriteAllApps.drawable = getResources().getDrawable(R.drawable.oobe_edit_all_apps);
        spriteAllApps.applySizeFromDrawable();
        spriteFavApps.drawable = getResources().getDrawable(R.drawable.oobe_edit_favourite_apps);
        spriteFavApps.applySizeFromDrawable();
        spriteAppIconAll1.drawable = getResources().getDrawable(R.drawable.oobe_app_icon_small);
        spriteAppIconAll1.applySizeFromDrawable();
        spriteAppIconFav1.drawable = getResources().getDrawable(R.drawable.oobe_app_icon);
        spriteAppIconFav1.applySizeFromDrawable();
        spriteAppIconFav2.drawable = getResources().getDrawable(R.drawable.oobe_app_icon);
        spriteAppIconFav2.applySizeFromDrawable();
        spriteAppIconFav3.drawable = getResources().getDrawable(R.drawable.oobe_app_icon);
        spriteAppIconFav3.applySizeFromDrawable();
        spriteAppIconFavSelected1.drawable = getResources().getDrawable(R.drawable.oobe_icon_select);
        spriteAppIconFavSelected1.applySizeFromDrawable();
        spriteAppIconFavSelected2.drawable = getResources().getDrawable(R.drawable.oobe_icon_select);
        spriteAppIconFavSelected2.applySizeFromDrawable();
        spriteAppIconFavSelected3.drawable = getResources().getDrawable(R.drawable.oobe_icon_select);
        spriteAppIconFavSelected3.applySizeFromDrawable();
        spriteDeleteHighlight.drawable = getResources().getDrawable(R.drawable.oobe_delete_higlight);
        spriteDeleteHighlight.applySizeFromDrawable();
        spriteDeleteX.drawable = getResources().getDrawable(R.drawable.oobe_delete_x);
        spriteDeleteX.applySizeFromDrawable();

        spriteRoot.addChild(spriteArrow);
        spriteRoot.addChild(spriteHand);
        spriteRoot.addChild(spriteHandShadow);
        spriteRoot.addChild(spriteAllApps);
        spriteRoot.addChild(spriteFavApps);
        spriteAllApps.addChild(spriteAppIconAll1);
        spriteFavApps.addChild(spriteAppIconFav1);
        spriteFavApps.addChild(spriteAppIconFavSelected1);
        spriteFavApps.addChild(spriteAppIconFav2);
        spriteFavApps.addChild(spriteAppIconFavSelected2);
        spriteFavApps.addChild(spriteAppIconFav3);
        spriteFavApps.addChild(spriteAppIconFavSelected3);
        spriteAllApps.addChild(spriteDeleteHighlight);
        spriteDeleteHighlight.addChild(spriteDeleteX);

        spriteRoot.alpha = 0;

        // Animation setup

        animationManager = new KWAnimationManager();
        animationGroupAddFav = new KWAnimationGroup(animationManager);
        animationGroupRemoveFav = new KWAnimationGroup(animationManager);
        animationGroupMoveFav = new KWAnimationGroup(animationManager);

        setupRemoveFavAnimation();
        setupAddFavAnimation();
        setupMoveFavAnimation();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        spriteRoot.resetMatrix();
        spriteArrow.draw(canvas, spritePaint);
        spriteAllApps.draw(canvas, spritePaint);
        spriteFavApps.draw(canvas, spritePaint);
        spriteAppIconAll1.draw(canvas, spritePaint);
        spriteDeleteHighlight.draw(canvas, spritePaint);
        spriteDeleteX.draw(canvas, spritePaint);
        spriteAppIconFav3.draw(canvas, spritePaint);
        spriteAppIconFavSelected3.draw(canvas, spritePaint);
        spriteAppIconFav2.draw(canvas, spritePaint);
        spriteAppIconFavSelected2.draw(canvas, spritePaint);
        spriteAppIconFav1.draw(canvas, spritePaint);
        spriteAppIconFavSelected1.draw(canvas, spritePaint);
        spriteHandShadow.draw(canvas, spritePaint);
        spriteHand.draw(canvas, spritePaint);
        animationManager.update();
        postInvalidate();
    }

    /*****************************************
     * 
     * setupRemoveFavAnimation()
     * 
     *****************************************/

    private void setupRemoveFavAnimation()
    {
        long startTime = 400;
        long endTime = 1700;

        KWValueAnimation alphaAnim;
        KWValueAnimation xAnim;
        KWValueAnimation yAnim;
        KWValueAnimation scaleAnim;

        KWAnimation deleteHighlightAnimation = new KWAnimation(spriteDeleteHighlight);
        alphaAnim = deleteHighlightAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 1000, null);
        alphaAnim.addKeyframe(1, 1200, accelerateInterpolator);
        alphaAnim.addKeyframe(1, endTime, null);
        alphaAnim.addKeyframe(0, endTime + 500, accelerateInterpolator);

        KWAnimation fingerAnimation = new KWAnimation(spriteHand);
        xAnim = fingerAnimation.addValueAnimation(KWValueType.X);
        yAnim = fingerAnimation.addValueAnimation(KWValueType.Y);
        xAnim.addKeyframe(441, startTime + 200, null);
        yAnim.addKeyframe(388, startTime + 200, null);
        xAnim.addKeyframe(206, startTime + 1200, accelerateDecelerateInterpolator);
        yAnim.addKeyframe(388, startTime + 1200, accelerateDecelerateInterpolator);

        alphaAnim = fingerAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 100, null);
        alphaAnim.addKeyframe(1, startTime, null);
        alphaAnim.addKeyframe(1, endTime, null);
        alphaAnim.addKeyframe(0, endTime + 500, accelerateInterpolator);

        KWAnimation arrowAnimation = new KWAnimation(spriteArrow);
        alphaAnim = arrowAnimation.addValueAnimation(KWValueType.Alpha);
        xAnim = arrowAnimation.addValueAnimation(KWValueType.X);
        alphaAnim.addKeyframe(0, startTime + 400, null);
        xAnim.addKeyframe(332, startTime + 400, null);
        alphaAnim.addKeyframe(1, startTime + 1300, accelerateDecelerateInterpolator);
        xAnim.addKeyframe(266, startTime + 1300, accelerateDecelerateInterpolator);
        alphaAnim.addKeyframe(0, endTime + 350, accelerateInterpolator);

        KWAnimation favIconSelected2Animation = new KWAnimation(spriteAppIconFavSelected2);
        xAnim = favIconSelected2Animation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe(0, startTime + 200, null);
        xAnim.addKeyframe(-238, startTime + 1200, accelerateDecelerateInterpolator);

        alphaAnim = favIconSelected2Animation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 100, null);
        alphaAnim.addKeyframe(1, startTime + 100, decelerateInterpolator);
        alphaAnim.addKeyframe(1, endTime - 100, null);
        alphaAnim.addKeyframe(0, endTime + 400, decelerateInterpolator);

        animationGroupRemoveFav.addAnimation(fingerAnimation);
        animationGroupRemoveFav.addAnimation(arrowAnimation);
        animationGroupRemoveFav.addAnimation(deleteHighlightAnimation);
        animationGroupRemoveFav.addAnimation(favIconSelected2Animation);

        animationGroupRemoveFav.setAnimationGroupListener(new KWAnimationGroupListener()
        {
            @Override
            public void onAnimationGroupStarted(KWAnimationGroup group)
            {
                spriteRoot.clearTransform(true);
                spriteRoot.alpha = 1;
                spriteArrow.alpha = 0;
                spriteHand.alpha = 0;
                spriteHandShadow.alpha = 0;
                spriteAllApps.alpha = 1;
                spriteFavApps.alpha = 1;
                spriteAppIconAll1.alpha = 1;
                spriteAppIconFav1.alpha = 1;
                spriteAppIconFavSelected1.alpha = 0;
                spriteAppIconFav2.alpha = 0;
                spriteAppIconFavSelected2.alpha = 0;
                spriteAppIconFav3.alpha = 1;
                spriteAppIconFavSelected3.alpha = 0;
                spriteDeleteHighlight.alpha = 0;
                spriteDeleteX.alpha = 1;

                spriteArrow.y = 388;
                spriteArrow.pivotX = 0;
                spriteArrow.pivotY = 0.5f;
                spriteArrow.scaleX = 0.8f;
                spriteArrow.scaleY = 0.8f;

                spriteHand.pivotX = 18.0f / spriteHand.width;
                spriteHand.pivotY = 21.0f / spriteHand.height;

                spriteHandShadow.pivotX = 54.0f / spriteHandShadow.width;
                spriteHandShadow.pivotY = 53.0f / spriteHandShadow.height;

                spriteAllApps.x = 60;
                spriteAllApps.y = (SCREEN_HEIGHT - spriteAllApps.height) / 2;
                spriteAppIconAll1.x = 110;
                spriteAppIconAll1.y = 97;

                spriteFavApps.x = SCREEN_WIDTH - spriteFavApps.width - 50;
                spriteFavApps.y = (SCREEN_HEIGHT - spriteFavApps.height) / 2;
                spriteAppIconFav1.x = 0;
                spriteAppIconFav1.y = 0;
                spriteAppIconFavSelected1.x = 0;
                spriteAppIconFavSelected1.y = 0;

                spriteAppIconFav2.x = 0;
                spriteAppIconFav2.y = 96;
                spriteAppIconFavSelected2.x = 0;
                spriteAppIconFavSelected2.y = 96;

                spriteAppIconFav3.x = 0;
                spriteAppIconFav3.y = 286;
                spriteAppIconFavSelected3.x = 0;
                spriteAppIconFavSelected3.y = 286;

                spriteDeleteHighlight.pivotX = 0.5f;
                spriteDeleteHighlight.pivotY = 0.5f;
                spriteDeleteHighlight.x = spriteAllApps.width / 2;
                spriteDeleteHighlight.y = spriteAllApps.height / 2;

                spriteDeleteX.pivotX = 0.5f;
                spriteDeleteX.pivotY = 0.5f;
                spriteDeleteX.x = 0;
                spriteDeleteX.y = 0;
            }

            @Override
            public void onAnimationGroupFinished(KWAnimationGroup group)
            {
                if (listener != null)
                {
                    listener.OnAnimationFinished(EditFavsTutorialAnimationView.this);
                }
            }
        });
    }

    /*****************************************
     * 
     * setupAddFavAnimation()
     * 
     *****************************************/

    private void setupAddFavAnimation()
    {
        long startTime = 500;
        long endTime = 1800;

        KWValueAnimation alphaAnim;
        KWValueAnimation xAnim;
        KWValueAnimation yAnim;
        KWValueAnimation scaleAnim;

        KWAnimation fingerAnimation = new KWAnimation(spriteHand);
        xAnim = fingerAnimation.addValueAnimation(KWValueType.X);
        yAnim = fingerAnimation.addValueAnimation(KWValueType.Y);
        xAnim.addKeyframe(206, startTime + 200, null);
        yAnim.addKeyframe(388, startTime + 200, null);
        xAnim.addKeyframe(441, startTime + 1200, accelerateDecelerateInterpolator);
        yAnim.addKeyframe(388, startTime + 1200, accelerateDecelerateInterpolator);

        alphaAnim = fingerAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 100, null);
        alphaAnim.addKeyframe(1, startTime, null);
        alphaAnim.addKeyframe(1, endTime, null);
        alphaAnim.addKeyframe(0, endTime + 500, accelerateInterpolator);

        KWAnimation arrowAnimation = new KWAnimation(spriteArrow);
        alphaAnim = arrowAnimation.addValueAnimation(KWValueType.Alpha);
        xAnim = arrowAnimation.addValueAnimation(KWValueType.X);
        alphaAnim.addKeyframe(0, startTime + 400, null);
        xAnim.addKeyframe(156, startTime + 400, null);
        alphaAnim.addKeyframe(1, startTime + 1300, accelerateDecelerateInterpolator);
        xAnim.addKeyframe(232, startTime + 1300, accelerateDecelerateInterpolator);
        alphaAnim.addKeyframe(0, endTime + 350, accelerateInterpolator);

        KWAnimation favIconSelected2Animation = new KWAnimation(spriteAppIconFavSelected2);
        xAnim = favIconSelected2Animation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe(-238, startTime + 200, null);
        xAnim.addKeyframe(0, startTime + 1200, accelerateDecelerateInterpolator);

        alphaAnim = favIconSelected2Animation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 100, null);
        alphaAnim.addKeyframe(1, startTime + 100, decelerateInterpolator);
        alphaAnim.addKeyframe(1, endTime - 100, null);
        alphaAnim.addKeyframe(0, endTime + 400, decelerateInterpolator);

        animationGroupAddFav.addAnimation(fingerAnimation);
        animationGroupAddFav.addAnimation(arrowAnimation);
        animationGroupAddFav.addAnimation(favIconSelected2Animation);

        animationGroupAddFav.setAnimationGroupListener(new KWAnimationGroupListener()
        {
            @Override
            public void onAnimationGroupStarted(KWAnimationGroup group)
            {
                spriteRoot.clearTransform(true);
                spriteRoot.alpha = 1;
                spriteArrow.alpha = 0;
                spriteHand.alpha = 0;
                spriteHandShadow.alpha = 0;
                spriteAllApps.alpha = 1;
                spriteFavApps.alpha = 1;
                spriteAppIconAll1.alpha = 0;
                spriteAppIconFav1.alpha = 1;
                spriteAppIconFavSelected1.alpha = 0;
                spriteAppIconFav2.alpha = 0;
                spriteAppIconFavSelected2.alpha = 0;
                spriteAppIconFav3.alpha = 1;
                spriteAppIconFavSelected3.alpha = 0;
                spriteDeleteHighlight.alpha = 0;
                spriteDeleteX.alpha = 1;

                spriteArrow.y = 388;
                spriteArrow.pivotX = 1;
                spriteArrow.pivotY = 0.5f;
                spriteArrow.scaleX = -0.8f;
                spriteArrow.scaleY = 0.8f;

                spriteHand.pivotX = 18.0f / spriteHand.width;
                spriteHand.pivotY = 21.0f / spriteHand.height;

                spriteHandShadow.pivotX = 54.0f / spriteHandShadow.width;
                spriteHandShadow.pivotY = 53.0f / spriteHandShadow.height;

                spriteAllApps.x = 60;
                spriteAllApps.y = (SCREEN_HEIGHT - spriteAllApps.height) / 2;
                spriteAppIconAll1.x = 110;
                spriteAppIconAll1.y = 97;

                spriteFavApps.x = SCREEN_WIDTH - spriteFavApps.width - 50;
                spriteFavApps.y = (SCREEN_HEIGHT - spriteFavApps.height) / 2;
                spriteAppIconFav1.x = 0;
                spriteAppIconFav1.y = 0;
                spriteAppIconFavSelected1.x = 0;
                spriteAppIconFavSelected1.y = 0;

                spriteAppIconFav2.x = 0;
                spriteAppIconFav2.y = 96;
                spriteAppIconFavSelected2.x = 0;
                spriteAppIconFavSelected2.y = 96;

                spriteAppIconFav3.x = 0;
                spriteAppIconFav3.y = 286;
                spriteAppIconFavSelected3.x = 0;
                spriteAppIconFavSelected3.y = 286;

                spriteDeleteHighlight.pivotX = 0.5f;
                spriteDeleteHighlight.pivotY = 0.5f;
                spriteDeleteHighlight.x = spriteAllApps.width / 2;
                spriteDeleteHighlight.y = spriteAllApps.height / 2;

                spriteDeleteX.pivotX = 0.5f;
                spriteDeleteX.pivotY = 0.5f;
                spriteDeleteX.x = 0;
                spriteDeleteX.y = 0;
            }

            @Override
            public void onAnimationGroupFinished(KWAnimationGroup group)
            {
                if (listener != null)
                {
                    listener.OnAnimationFinished(EditFavsTutorialAnimationView.this);
                }
            }
        });
    }

    /*****************************************
     * 
     * setupMoveFavAnimation()
     * 
     *****************************************/

    private void setupMoveFavAnimation()
    {
        long startTime = 500;
        long endTime = 2000;

        KWValueAnimation alphaAnim;
        KWValueAnimation xAnim;
        KWValueAnimation yAnim;
        KWValueAnimation scaleAnim;

        KWAnimation fingerAnimation = new KWAnimation(spriteHand);
        xAnim = fingerAnimation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe(441, startTime + 200, null);
        xAnim.addKeyframe(411, startTime + 700, accelerateDecelerateInterpolator);
        xAnim.addKeyframe(441, startTime + 1200, accelerateDecelerateInterpolator);
        yAnim = fingerAnimation.addValueAnimation(KWValueType.Y);
        yAnim.addKeyframe(292, startTime + 200, null);
        yAnim.addKeyframe(578, startTime + 1200, accelerateDecelerateInterpolator);

        alphaAnim = fingerAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 100, null);
        alphaAnim.addKeyframe(1, startTime, null);
        alphaAnim.addKeyframe(1, endTime, null);
        alphaAnim.addKeyframe(0, endTime + 500, accelerateInterpolator);

        KWAnimation arrowAnimation = new KWAnimation(spriteArrow);
        yAnim = arrowAnimation.addValueAnimation(KWValueType.Y);
        yAnim.addKeyframe(300, startTime + 400, null);
        yAnim.addKeyframe(420, startTime + 1300, accelerateDecelerateInterpolator);
        alphaAnim = arrowAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, startTime + 400, null);
        alphaAnim.addKeyframe(1, startTime + 1300, accelerateDecelerateInterpolator);
        alphaAnim.addKeyframe(0, endTime + 350, accelerateInterpolator);

        KWAnimation favIconSelected1Animation = new KWAnimation(spriteAppIconFavSelected1);
        xAnim = favIconSelected1Animation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe(0, startTime + 200, null);
        xAnim.addKeyframe(-30, startTime + 700, accelerateDecelerateInterpolator);
        xAnim.addKeyframe(0, startTime + 1200, accelerateDecelerateInterpolator);

        yAnim = favIconSelected1Animation.addValueAnimation(KWValueType.Y);
        yAnim.addKeyframe(0, startTime + 200, null);
        yAnim.addKeyframe(286, startTime + 1200, accelerateDecelerateInterpolator);

        alphaAnim = favIconSelected1Animation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 100, null);
        alphaAnim.addKeyframe(1, startTime + 100, decelerateInterpolator);
        alphaAnim.addKeyframe(1, endTime - 100, null);
        alphaAnim.addKeyframe(0, endTime + 400, decelerateInterpolator);

        KWAnimation favIcon3Animation = new KWAnimation(spriteAppIconFav3);
        xAnim = favIcon3Animation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe(0, startTime + 900, null);
        xAnim.addKeyframe(+30, startTime + 1050, accelerateDecelerateInterpolator);
        xAnim.addKeyframe(0, startTime + 1200, accelerateDecelerateInterpolator);

        yAnim = favIcon3Animation.addValueAnimation(KWValueType.Y);
        yAnim.addKeyframe(286, startTime + 900, null);
        yAnim.addKeyframe(0, startTime + 1200, accelerateDecelerateInterpolator);

        alphaAnim = favIcon3Animation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 100, null);
        alphaAnim.addKeyframe(1, startTime + 100, decelerateInterpolator);
        alphaAnim.addKeyframe(1, endTime - 100, null);
        alphaAnim.addKeyframe(0, endTime + 400, decelerateInterpolator);

        animationGroupMoveFav.addAnimation(fingerAnimation);
        animationGroupMoveFav.addAnimation(arrowAnimation);
        animationGroupMoveFav.addAnimation(favIconSelected1Animation);
        animationGroupMoveFav.addAnimation(favIcon3Animation);

        animationGroupMoveFav.setAnimationGroupListener(new KWAnimationGroupListener()
        {
            @Override
            public void onAnimationGroupStarted(KWAnimationGroup group)
            {
                spriteRoot.clearTransform(true);
                spriteRoot.alpha = 1;
                spriteArrow.alpha = 0;
                spriteHand.alpha = 0;
                spriteHandShadow.alpha = 0;
                spriteAllApps.alpha = 1;
                spriteFavApps.alpha = 1;
                spriteAppIconAll1.alpha = 1;
                spriteAppIconFav1.alpha = 0;
                spriteAppIconFavSelected1.alpha = 0;
                spriteAppIconFav2.alpha = 1;
                spriteAppIconFavSelected2.alpha = 0;
                spriteAppIconFav3.alpha = 0;
                spriteAppIconFavSelected3.alpha = 0;
                spriteDeleteHighlight.alpha = 0;
                spriteDeleteX.alpha = 1;

                spriteArrow.x = 350;
                spriteArrow.pivotX = 0.5f;
                spriteArrow.pivotY = 0.5f;
                spriteArrow.scaleX = 1.0f;
                spriteArrow.scaleY = 0.8f;
                spriteArrow.rotation = -90;

                spriteHand.pivotX = 18.0f / spriteHand.width;
                spriteHand.pivotY = 21.0f / spriteHand.height;

                spriteHandShadow.pivotX = 54.0f / spriteHandShadow.width;
                spriteHandShadow.pivotY = 53.0f / spriteHandShadow.height;

                spriteAllApps.x = 60;
                spriteAllApps.y = (SCREEN_HEIGHT - spriteAllApps.height) / 2;
                spriteAppIconAll1.x = 110;
                spriteAppIconAll1.y = 97;

                spriteFavApps.x = SCREEN_WIDTH - spriteFavApps.width - 50;
                spriteFavApps.y = (SCREEN_HEIGHT - spriteFavApps.height) / 2;
                spriteAppIconFav1.x = 0;
                spriteAppIconFav1.y = 0;
                spriteAppIconFavSelected1.x = 0;
                spriteAppIconFavSelected1.y = 0;

                spriteAppIconFav2.x = 0;
                spriteAppIconFav2.y = 96;
                spriteAppIconFavSelected2.x = 0;
                spriteAppIconFavSelected2.y = 96;

                spriteAppIconFav3.x = 0;
                spriteAppIconFav3.y = 286;
                spriteAppIconFavSelected3.x = 0;
                spriteAppIconFavSelected3.y = 286;

                spriteDeleteHighlight.pivotX = 0.5f;
                spriteDeleteHighlight.pivotY = 0.5f;
                spriteDeleteHighlight.x = spriteAllApps.width / 2;
                spriteDeleteHighlight.y = spriteAllApps.height / 2;

                spriteDeleteX.pivotX = 0.5f;
                spriteDeleteX.pivotY = 0.5f;
                spriteDeleteX.x = 0;
                spriteDeleteX.y = 0;
            }

            @Override
            public void onAnimationGroupFinished(KWAnimationGroup group)
            {
                if (listener != null)
                {
                    listener.OnAnimationFinished(EditFavsTutorialAnimationView.this);
                }
            }
        });
    }
}
