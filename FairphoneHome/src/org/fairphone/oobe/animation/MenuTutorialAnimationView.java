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

public class MenuTutorialAnimationView extends FrameLayout
{

    public interface MenuTutorialAnimationViewListener
    {
        public void OnAnimationFinished(MenuTutorialAnimationView view);
    }

    private static final int SCREEN_WIDTH = 540;
    private static final int SCREEN_HEIGHT = 960;

    KWSprite spriteRoot;

    KWSprite spriteArrow;
    KWSprite spriteHand;
    KWSprite spriteHandShadow;
    KWSprite spriteMenu;
    KWSprite spriteAppIcon;
    KWSprite spriteAppIconSelected;

    Paint spritePaint;

    KWAnimationGroup animationGroupSwipe;
    KWAnimationGroup animationGroupSelectApp;
    KWAnimationManager animationManager;

    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    private AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
    private AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
    long prevFrame = 0;

    MenuTutorialAnimationViewListener listener;

    public MenuTutorialAnimationView(Context context)
    {
        super(context);
        init();
    }

    public MenuTutorialAnimationView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public MenuTutorialAnimationView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    void setMenuTutorialAnimationViewListener(MenuTutorialAnimationViewListener listener)
    {
        this.listener = listener;
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
        spriteMenu = new KWSprite();
        spriteAppIcon = new KWSprite();
        spriteAppIconSelected = new KWSprite();

        spriteArrow.drawable = getResources().getDrawable(R.drawable.oobe_arrow_big);
        spriteArrow.applySizeFromDrawable();
        spriteHand.drawable = getResources().getDrawable(R.drawable.oobe_hand);
        spriteHand.applySizeFromDrawable();
        spriteHandShadow.drawable = getResources().getDrawable(R.drawable.oobe_hand_shadow);
        spriteHandShadow.applySizeFromDrawable();
        spriteMenu.drawable = getResources().getDrawable(R.drawable.oobe_menu);
        spriteMenu.applySizeFromDrawable();
        spriteAppIcon.drawable = getResources().getDrawable(R.drawable.oobe_app_icon);
        spriteAppIcon.applySizeFromDrawable();
        spriteAppIconSelected.drawable = getResources().getDrawable(R.drawable.oobe_icon_select);
        spriteAppIconSelected.applySizeFromDrawable();

        spriteRoot.addChild(spriteArrow);
        spriteRoot.addChild(spriteHand);
        spriteRoot.addChild(spriteHandShadow);
        spriteRoot.addChild(spriteMenu);
        spriteMenu.addChild(spriteAppIcon);
        spriteMenu.addChild(spriteAppIconSelected);

        spriteRoot.alpha = 0;

        spriteAppIconSelected.alpha = 0;

        // Animation setup

        animationManager = new KWAnimationManager();
        animationGroupSwipe = new KWAnimationGroup(animationManager);
        animationGroupSelectApp = new KWAnimationGroup(animationManager);

        setupSwipeAnimation();
        setupAppOpenAnimation();
    }

    public void playSwipeAnimation()
    {
        animationGroupSwipe.start();
    }

    public void playAppOpenAnimation()
    {
        animationGroupSelectApp.start();
    }

    public void stopAnimations()
    {
        animationGroupSwipe.stop();
        animationGroupSelectApp.stop();
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
        spriteMenu.draw(canvas, spritePaint);
        spriteAppIcon.draw(canvas, spritePaint);
        spriteAppIconSelected.draw(canvas, spritePaint);
        spriteHandShadow.draw(canvas, spritePaint);
        spriteHand.draw(canvas, spritePaint);
        animationManager.update();
        postInvalidate();
    }

    private void setupSwipeAnimation()
    {
        KWAnimation rootSwipeAnimation = new KWAnimation(spriteRoot);
        KWValueAnimation alphaAnim = rootSwipeAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(1, 1700, null);
        alphaAnim.addKeyframe(0, 2000, accelerateInterpolator);

        KWAnimation fingerSwipeAnimation = new KWAnimation(spriteHand);
        KWValueAnimation xAnim = fingerSwipeAnimation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe(SCREEN_WIDTH, 350, null);
        xAnim.addKeyframe(SCREEN_WIDTH - 200, 700, decelerateInterpolator);
        xAnim.addKeyframe(SCREEN_WIDTH - 200, 1700, null);

        KWAnimation arrowSwipeAnimation = new KWAnimation(spriteArrow);
        xAnim = arrowSwipeAnimation.addValueAnimation(KWValueType.X);
        xAnim.addKeyframe(SCREEN_WIDTH, 0, null);
        xAnim.addKeyframe(SCREEN_WIDTH - 220, 500, decelerateInterpolator);

        KWAnimation menuSwipeAnimation = new KWAnimation(spriteMenu);
        KWValueAnimation scaleAnim = menuSwipeAnimation.addValueAnimation(KWValueType.Scale);
        scaleAnim.addKeyframe(0.8f, 600, null);
        scaleAnim.addKeyframe(1.0f, 800, decelerateInterpolator);
        alphaAnim = menuSwipeAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 600, null);
        alphaAnim.addKeyframe(1.0f, 750, decelerateInterpolator);

        animationGroupSwipe.addAnimation(fingerSwipeAnimation);
        animationGroupSwipe.addAnimation(arrowSwipeAnimation);
        animationGroupSwipe.addAnimation(rootSwipeAnimation);
        animationGroupSwipe.addAnimation(menuSwipeAnimation);

        animationGroupSwipe.setAnimationGroupListener(new KWAnimationGroupListener()
        {
            @Override
            public void onAnimationGroupStarted(KWAnimationGroup group)
            {
                spriteRoot.clearTransform(true);
                spriteRoot.alpha = 0;
                spriteArrow.alpha = 1;
                spriteHand.alpha = 1;
                spriteHandShadow.alpha = 0;
                spriteMenu.alpha = 1;
                spriteAppIcon.alpha = 1;
                spriteAppIconSelected.alpha = 0;

                spriteHand.y = SCREEN_HEIGHT / 2;
                spriteArrow.y = SCREEN_HEIGHT / 2;
                spriteArrow.pivotY = 0.5f;
                spriteMenu.y = SCREEN_HEIGHT / 2;
                spriteMenu.x = SCREEN_WIDTH - 100;
                spriteMenu.pivotX = 1.0f;
                spriteMenu.pivotY = 0.5f;
                spriteAppIcon.x = -spriteMenu.pivotX * spriteMenu.width + 53;
                spriteAppIcon.y = -spriteMenu.pivotY * spriteMenu.height + 68;
                spriteAppIconSelected.x = -spriteMenu.pivotX * spriteMenu.width + 53;
                spriteAppIconSelected.y = -spriteMenu.pivotY * spriteMenu.height + 68;
            }

            @Override
            public void onAnimationGroupFinished(KWAnimationGroup group)
            {
                if (listener != null)
                {
                    listener.OnAnimationFinished(MenuTutorialAnimationView.this);
                }
            }
        });
    }

    private void setupAppOpenAnimation()
    {
        KWValueAnimation alphaAnim;
        KWValueAnimation xAnim;
        KWValueAnimation yAnim;
        KWValueAnimation scaleAnim;

        KWAnimation rootAnimation = new KWAnimation(spriteRoot);
        alphaAnim = rootAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 0, null);
        alphaAnim.addKeyframe(1, 300, decelerateInterpolator);
        alphaAnim.addKeyframe(1, 1950, null);
        alphaAnim.addKeyframe(0, 2250, decelerateInterpolator);

        KWAnimation handAnimation = new KWAnimation(spriteHand);
        xAnim = handAnimation.addValueAnimation(KWValueType.X);
        yAnim = handAnimation.addValueAnimation(KWValueType.Y);
        scaleAnim = handAnimation.addValueAnimation(KWValueType.Scale);
        xAnim.addKeyframe(SCREEN_WIDTH - 200, 300, null);
        yAnim.addKeyframe(SCREEN_HEIGHT / 2, 300, null);

        xAnim.addKeyframe(286, 700, accelerateDecelerateInterpolator);
        yAnim.addKeyframe(350, 700, accelerateDecelerateInterpolator);

        xAnim.addKeyframe(286, 1300, accelerateDecelerateInterpolator);
        yAnim.addKeyframe(350, 1300, accelerateDecelerateInterpolator);
        scaleAnim.addKeyframe(1.0f, 1300, null);

        scaleAnim.addKeyframe(1.3f, 1700, accelerateDecelerateInterpolator);
        xAnim.addKeyframe(311, 1700, accelerateDecelerateInterpolator);
        yAnim.addKeyframe(323, 1700, accelerateDecelerateInterpolator);

        KWAnimation handShadowAnimation = new KWAnimation(spriteHandShadow);
        alphaAnim = handShadowAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0, 1300, null);
        alphaAnim.addKeyframe(1.0f, 1700, accelerateInterpolator);

        KWAnimation appIconAnimation = new KWAnimation(spriteAppIcon);
        alphaAnim = appIconAnimation.addValueAnimation(KWValueType.Alpha);
        scaleAnim = appIconAnimation.addValueAnimation(KWValueType.Scale);
        alphaAnim.addKeyframe(1.0f, 0, null);
        scaleAnim.addKeyframe(1.0f, 0, null);
        alphaAnim.addKeyframe(1.0f, 650, null);
        scaleAnim.addKeyframe(1.0f, 650, null);
        alphaAnim.addKeyframe(0, 650, null);
        scaleAnim.addKeyframe(0.9f, 650, null);
        alphaAnim.addKeyframe(1.0f, 830, decelerateInterpolator);
        scaleAnim.addKeyframe(1.4f, 1000, decelerateInterpolator);
        scaleAnim.addKeyframe(1.4f, 1550, null);
        alphaAnim.addKeyframe(1.0f, 1550, null);
        scaleAnim.addKeyframe(3.5f, 1900, decelerateInterpolator);
        alphaAnim.addKeyframe(0.0f, 1850, accelerateInterpolator);

        KWAnimation appIconSelectedAnimation = new KWAnimation(spriteAppIconSelected);
        alphaAnim = appIconSelectedAnimation.addValueAnimation(KWValueType.Alpha);
        scaleAnim = appIconSelectedAnimation.addValueAnimation(KWValueType.Scale);
        alphaAnim.addKeyframe(0, 650, null);
        scaleAnim.addKeyframe(0.9f, 650, null);
        alphaAnim.addKeyframe(1.0f, 730, accelerateInterpolator);
        scaleAnim.addKeyframe(1.0f, 750, decelerateInterpolator);

        KWAnimation menuAnimation = new KWAnimation(spriteMenu);
        alphaAnim = menuAnimation.addValueAnimation(KWValueType.Alpha);
        alphaAnim.addKeyframe(0.0f, 200, accelerateInterpolator);
        alphaAnim.addKeyframe(1, 400, null);
        alphaAnim.addKeyframe(1, 1900, null);
        alphaAnim.addKeyframe(0.0f, 2000, decelerateInterpolator);

        animationGroupSelectApp.addAnimation(rootAnimation);
        animationGroupSelectApp.addAnimation(handAnimation);
        animationGroupSelectApp.addAnimation(handShadowAnimation);
        animationGroupSelectApp.addAnimation(appIconAnimation);
        animationGroupSelectApp.addAnimation(appIconSelectedAnimation);
        animationGroupSelectApp.addAnimation(menuAnimation);

        animationGroupSelectApp.setAnimationGroupListener(new KWAnimationGroupListener()
        {
            @Override
            public void onAnimationGroupStarted(KWAnimationGroup group)
            {
                spriteRoot.clearTransform(true);
                spriteRoot.alpha = 0;
                spriteArrow.alpha = 0;
                spriteHand.alpha = 1;
                spriteHandShadow.alpha = 0;
                spriteMenu.alpha = 1;
                spriteAppIcon.alpha = 1;
                spriteAppIconSelected.alpha = 0;

                spriteHand.x = SCREEN_WIDTH - 200;
                spriteHand.y = SCREEN_HEIGHT / 2;

                spriteHand.pivotX = 18.0f / spriteHand.width;
                spriteHand.pivotY = 21.0f / spriteHand.height;

                spriteHandShadow.x = 286;
                spriteHandShadow.y = 350;
                spriteHandShadow.pivotX = 54.0f / spriteHandShadow.width;
                spriteHandShadow.pivotY = 53.0f / spriteHandShadow.height;

                spriteArrow.x = SCREEN_WIDTH - 220;
                spriteArrow.y = SCREEN_HEIGHT / 2;

                spriteArrow.pivotX = 0.0f;
                spriteArrow.pivotY = 0.5f;

                spriteMenu.y = SCREEN_HEIGHT / 2;
                spriteMenu.x = SCREEN_WIDTH - 100;

                spriteMenu.pivotX = 1.0f;
                spriteMenu.pivotY = 0.5f;

                spriteAppIcon.pivotX = 0.5f;
                spriteAppIcon.pivotY = 0.5f;
                spriteAppIcon.x = -spriteMenu.pivotX * spriteMenu.width + 53 + spriteAppIcon.pivotX * spriteAppIcon.width;
                spriteAppIcon.y = -spriteMenu.pivotY * spriteMenu.height + 68 + spriteAppIcon.pivotX * spriteAppIcon.width;

                spriteAppIconSelected.pivotX = 0.5f;
                spriteAppIconSelected.pivotY = 0.5f;
                spriteAppIconSelected.x = -spriteMenu.pivotX * spriteMenu.width + 53 + spriteAppIconSelected.pivotX * spriteAppIconSelected.width;
                spriteAppIconSelected.y = -spriteMenu.pivotY * spriteMenu.height + 68 + spriteAppIconSelected.pivotX * spriteAppIconSelected.width;
            }

            @Override
            public void onAnimationGroupFinished(KWAnimationGroup group)
            {
                if (listener != null)
                {
                    listener.OnAnimationFinished(MenuTutorialAnimationView.this);
                }
            }
        });
    }
}
