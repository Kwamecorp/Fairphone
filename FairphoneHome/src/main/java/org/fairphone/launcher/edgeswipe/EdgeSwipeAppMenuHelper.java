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
package org.fairphone.launcher.edgeswipe;

import org.fairphone.launcher.ApplicationInfo;
import org.fairphone.launcher.DragController;
import org.fairphone.launcher.Launcher;
import org.fairphone.launcher.R;
import org.fairphone.launcher.edgeswipe.edit.FavoritesStorageHelper;
import org.fairphone.launcher.edgeswipe.ui.EdgeSwipeInterceptorViewListener;
import org.fairphone.launcher.util.KWMathUtils;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EdgeSwipeAppMenuHelper implements EdgeSwipeInterceptorViewListener
{

    private static final String TAG = EdgeSwipeAppMenuHelper.class.getSimpleName();

    private static int MAX_FAVORITE_APPS = 4;
    private static int ITEM_COUNT = 5;

    public static enum Side
    {
        LEFT, RIGHT
    };

    enum IconEndPositions
    {
        Icon1(0, 0), Icon2(0, 0), Icon3(0, 0), Icon4(0, 0), Icon5(0, 0);

        private int x;
        private int y;

        private IconEndPositions(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public int getX()
        {
            return x;
        }

        public int getY()
        {
            return y;
        }
    }

    private int mMenuWidth;
    private int mMenuHeight;

    private int mIconWidth;
    private int mIconHeight;

    private float mCenterX;
    private float mCenterY;

    private Side mCurrentSide;

    private float innerDeadzone;
    private float outerDeadzone;

    private ViewGroup menuContainerView;
    private View menuRoot;
    private View menuContent;
    private View menuBackground;
    private View editGroup;
    private View swipeMenuTopShadow;
    private View swipeMenuBottomShadow;

    private Context mContext;

    private ItemIcon[] icons;

    private int prevVisibleIcon;
    private Launcher mLauncher;
    private DisplayMetrics mDisplayMetrics;

    private boolean mIsInitialized;
    private boolean isInSelection;

	private long mEditMenuButtonStartTime;

    public class ItemIcon
    {
        TextView selectedViewName;
        View rootView;
        View iconRingView;
        Intent intent;

        private ItemIcon(View rootView, Drawable iconSelected, String iconSelectedName, Intent intent)
        {
            this.intent = intent;
            this.rootView = rootView;

            selectedViewName = (TextView) rootView.findViewById(R.id.iconSelectedName);
            iconRingView = rootView.findViewById(R.id.iconPressRing);

            Resources r = mContext.getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, r.getDisplayMetrics());
            iconSelected.setBounds(0, 0, Math.round(px), Math.round(px));
            selectedViewName.setCompoundDrawables(null, iconSelected, null, null);

            // App drawer doesn't have a text description
            if (selectedViewName != null)
            {
                selectedViewName.setText(iconSelectedName);
            }
        }
    }

    public EdgeSwipeAppMenuHelper(Context context, DragController detectorView, ViewGroup menuContainerView, Launcher launcher)
    {

        mContext = context;
        mIsInitialized = false;
        prevVisibleIcon = -1;
        isInSelection = false;

        icons = new ItemIcon[ITEM_COUNT];

        this.setMenuContainerView(menuContainerView);
        this.mLauncher = launcher;

        detectorView.addOnSelectionListener(this);

        mDisplayMetrics = new DisplayMetrics();

        launcher.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

        setCurrentSide(null);

        setupLayout();

        setDeadzones();
    }

    /**
     * Set the inner and outer deadzones to control icon selection
     */
    private void setDeadzones()
    {
        innerDeadzone = convertDpToPixels(mLauncher.getResources().getDimensionPixelSize(R.dimen.edge_swipe_inner_deadzone));
        outerDeadzone = convertDpToPixels(mLauncher.getResources().getDimensionPixelSize(R.dimen.edge_swipe_outer_deadzone));
    }

    private int convertDpToPixels(float dpToConvert)
    {

        // Get the screen's density scale
        final float scale = mLauncher.getResources().getDisplayMetrics().density;

        // Convert the dps to pixels, based on density scale
        return (int) (dpToConvert * scale + 0.5f);
    }

    public ItemIcon[] getIcons()
    {
        return icons;
    }
    

    @Override
    public void onSelectionStarted(float pointerX, float pointerY)
    {

        // update the icons
        if (!mIsInitialized)
        {
            updateIcons();
            mIsInitialized = true;
        }

        isInSelection = true;
        
        // set the Y coords
        float newPointerY = pointerY;
        
        setupCurrentDisplay(pointerX, newPointerY);

        // Animate the menu
        // setup the side animation
        Animation animation = setTheSwipeSideAnimation(pointerX);

        // show the menu and background views
        getMenuContainerView().setVisibility(View.VISIBLE);
        menuRoot.setVisibility(View.VISIBLE);
        menuBackground.setVisibility(View.VISIBLE);
        menuContent.setVisibility(View.VISIBLE);
        menuContent.startAnimation(animation);
        
        swipeMenuTopShadow.setVisibility(View.VISIBLE);
        swipeMenuBottomShadow.setVisibility(View.VISIBLE);

        // Animate the background
        Animation backAnimation = AnimationUtils.loadAnimation(menuRoot.getContext(), R.anim.menu_background_fade_in);
        menuBackground.startAnimation(backAnimation);

        // define the side of the swipe
        int halfMenuWidth = menuRoot.getWidth() / 2;
        int halfContentWidth = menuContent.getWidth() / 2;
        
        // set the X coords
        switch (getCurrentSide())
        {
            case LEFT:
                menuRoot.setX(-halfMenuWidth + (halfContentWidth - halfMenuWidth) / 2);
                break;
            case RIGHT:
                menuRoot.setX((int) (mDisplayMetrics.widthPixels - halfContentWidth));
                break;
            default:
                break;
        }
        
        
        setupEditButtonPositionAndTimer(newPointerY);
        
        float menuYCoord = newPointerY - halfMenuWidth;
        
        menuRoot.setY(menuYCoord);

        // update the icons position
        updateIconPosition();
    }

	private boolean startEditButtonAnimation() {
		boolean isTimeToShow = mEditMenuButtonStartTime < System.currentTimeMillis();
		if(isTimeToShow && (editGroup.getVisibility() != View.VISIBLE) && menuRoot.getVisibility() == View.VISIBLE){
			Animation editMenuAnimation = getEditButtonAnimation();        
	        if(editMenuAnimation != null){
		        //set the button visible and animate
		        editGroup.setVisibility(View.VISIBLE);
		        editGroup.startAnimation(editMenuAnimation);
	        }
		}else{
			Log.d(TAG, "Edit button can't be shown");
		}
		return isTimeToShow;
	}

	private void setupEditButtonPositionAndTimer(float pointerY) {
		
		//set edit menu button timer
        mEditMenuButtonStartTime = System.currentTimeMillis() + mLauncher.getApplicationContext().getResources().getInteger(R.integer.config_edgeMenuEditButtonTime);
        
		// set the X coords
		switch (getCurrentSide())
        {
            case LEFT:
                editGroup.setX((int) (mDisplayMetrics.widthPixels - editGroup.getWidth()));
                break;
            case RIGHT:
                editGroup.setX(0);
                break;
            default:
                break;
        }
        
        //set Y coords
        editGroup.setY(pointerY-(editGroup.getHeight()/2));
	}
	
	private Animation getEditButtonAnimation() {	
		Animation editMenuAnimation = AnimationUtils.loadAnimation(editGroup.getContext(), R.anim.menu_edge_edit_button_fade_in);

		return editMenuAnimation;
	}

    private Animation setTheSwipeSideAnimation(float pointerX)
    {
        Animation animation;
        if (pointerX < mDisplayMetrics.widthPixels / 2)
        {
            setCurrentSide(Side.LEFT);
            menuContent.setX(-menuContent.getWidth() + menuRoot.getWidth());
            animation = AnimationUtils.loadAnimation(menuRoot.getContext(), R.anim.menu_appear_from_left_animation);
        }
        else
        {
            setCurrentSide(Side.RIGHT);
            menuContent.setX(0);
            animation = AnimationUtils.loadAnimation(menuRoot.getContext(), R.anim.menu_appear_from_right_animation);
        }
        return animation;
    }

    private void setupCurrentDisplay(float pointerX, float pointerY)
    {
    	// setup the size
        mMenuWidth = menuContent.getWidth();
        mMenuHeight = menuContent.getHeight();
        
        // setup the center of the menu
        mCenterX = pointerX;
        mCenterY = pointerY;

        // setup the icon size
        RelativeLayout v = (RelativeLayout) menuContent.findViewById(R.id.icon1);
        mIconHeight = v.getHeight();
        mIconWidth = v.getWidth();
    }

    @Override
    public void onSelectionUpdate(float pointerX, float pointerY)
    {   
    	startEditButtonAnimation();
    			
    	//select the edit zone menu with a circle
    	selectEditFavoritesMenuZone(pointerX, pointerY);
    	
        // check to see if the user is above the icon zone
        if (isInActiveZone(pointerX, pointerY))
        {
            // get the icon
            int iconIndex = getIconIndex(pointerX, pointerY);

            // check to see if the user is changing the icon
            if (prevVisibleIcon != iconIndex)
            {          
                //Animate the selected icon ring
                startIconRingAppearAnimation(icons[iconIndex].iconRingView);
                
                //Animate the previous icon aka remove the ring
                if(prevVisibleIcon != -1){
                	startIconRingDisappearAnimation(icons[prevVisibleIcon].iconRingView);
                }
                
                
                prevVisibleIcon = iconIndex;
            }
        }
        else
        {
        	if(prevVisibleIcon != -1){
        		startIconRingDisappearAnimation(icons[prevVisibleIcon].iconRingView);
        	}
        	
            prevVisibleIcon = -1;
        }

    }

	private void selectEditFavoritesMenuZone(float pointerX, float pointerY) {
		if(editGroup.getVisibility() == View.VISIBLE){
			if(isInEditZone(pointerX, pointerY)){
	    		View editZoneCircle = editGroup.findViewById(R.id.editRing);
	    		if(editZoneCircle != null && editZoneCircle.getVisibility() != View.VISIBLE){
	    			startIconRingAppearAnimation(editZoneCircle);
	    		}
	    	}
	    	else{
	    		View editZoneCircle = editGroup.findViewById(R.id.editRing);
	    		if(editZoneCircle != null && editZoneCircle.getVisibility() == View.VISIBLE){
	    			startIconRingDisappearAnimation(editZoneCircle);
	    		}
	    	}
		}
	}

    private void startIconRingAppearAnimation(View viewToAnimate)
    {
    	menuRoot.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    	
    	Animation iconRingAnimation = AnimationUtils.loadAnimation(menuRoot.getContext(), R.anim.menu_icon_ring_fade_in);
    	viewToAnimate.setVisibility(View.VISIBLE);
    	viewToAnimate.startAnimation(iconRingAnimation);
    }
    
	private void startIconRingDisappearAnimation(View viewToAnimate) {
	    Animation iconRingFadeOutAnimation = AnimationUtils.loadAnimation(menuRoot.getContext(), R.anim.menu_icon_ring_fade_out);    
	    viewToAnimate.startAnimation(iconRingFadeOutAnimation);
	    viewToAnimate.setVisibility(View.INVISIBLE);
	}

    private int getIconIndex(float pointerX, float pointerY)
    {
        double deg = KWMathUtils.radToDeg(Math.atan2(-(pointerY - mCenterY), pointerX - mCenterX));

        if (deg < 0)
        {
            deg += 360;
        }

        if (deg > 360)
        {
            deg -= 360;
        }

        float ratio = 0;

        if (getCurrentSide() == Side.RIGHT)
        {
            ratio = KWMathUtils.getFloatRatio(90, 270, (float) deg);
        }
        else
        {
            if (deg <= 90)
            {
                ratio = 0.5f - KWMathUtils.getFloatRatio(0, 180, (float) deg);
            }
            else
            {
                ratio = 1.5f - KWMathUtils.getFloatRatio(180, 360, (float) deg);
            }
        }

        float dr = 1.0f / ITEM_COUNT;

        int iconIndex = (int) (ratio / dr);

        if (iconIndex >= ITEM_COUNT)
        {
            iconIndex = ITEM_COUNT - 1;
        }

        return iconIndex;
    }

    private boolean isInActiveZone(float pointerX, float pointerY)
    {

        double distance = KWMathUtils.getDistance(pointerX, pointerY, mCenterX, mCenterY);
        return (distance > innerDeadzone && distance < outerDeadzone);
    }
    
    private boolean isInEditZone(float pointerX, float pointerY)
    {   
    	boolean validX = false;
        boolean validY = false;
        
        if(getCurrentSide() == null){
        	return false;
        }
        
    	switch (getCurrentSide())
        {
            case LEFT:
                validX = pointerX >= editGroup.getX();
                break;
            case RIGHT:
            	validX = pointerX <= (editGroup.getX() + editGroup.getWidth());
                break;
            default:
                break;
        }
    	validY = pointerY >= editGroup.getY();
    	validY &= pointerY <= editGroup.getY() + editGroup.getHeight();
        
        return validX && validY;
    }

    boolean contentVisible = false;
    boolean backVisible = false;

    @Override
    public void onSelectionFinished(float pointerX, float pointerY)
    {
    	//launch the edit menu
    	if(editGroup.getVisibility() == View.VISIBLE && isInEditZone(pointerX, pointerY)){	    	
	    	View editZoneCircle = editGroup.findViewById(R.id.editRing);
    		if(editZoneCircle != null && editZoneCircle.getVisibility() == View.VISIBLE){
    			startIconRingDisappearAnimation(editZoneCircle);
    		}
    		
    		//to avoid multiple open actions:
    		//open edit favorites and then the apps drawer
    		if (prevVisibleIcon >= 0)
            {
            	startIconRingDisappearAnimation(icons[prevVisibleIcon].iconRingView);
            }
            
            prevVisibleIcon = -1;
            
	    	mLauncher.startEditFavorites();
    	}
    	
        if (!isInSelection)
        {
            return;
        }
        isInSelection = false;

        // verify if the menu is showing
        if (menuContent.getVisibility() == View.VISIBLE)
        {
            setMenuSideExitAnimation();
        }
        else
        {
            contentVisible = false;
            backVisible = false;
            menuRoot.setVisibility(View.INVISIBLE);
            contentVisible = false;
            getMenuContainerView().setVisibility(View.GONE);
        }

        setBackgroundExitAnimation();

        if (prevVisibleIcon >= 0)
        {
        	startIconRingDisappearAnimation(icons[prevVisibleIcon].iconRingView);
            launchMenuItem();
        }
        
        prevVisibleIcon = -1;

        setCurrentSide(null);

        editGroup.setVisibility(View.GONE);
        View editZoneCircle = editGroup.findViewById(R.id.editRing);
        editZoneCircle.setVisibility(View.GONE);
        
        swipeMenuTopShadow.setVisibility(View.GONE);
        swipeMenuBottomShadow.setVisibility(View.GONE);
    }

    private void launchMenuItem()
    {
        try
        {
            if (prevVisibleIcon == 2)
            {
                mLauncher.showAllApps(true);
            }
            else
            {
				if (icons[prevVisibleIcon] != null && icons[prevVisibleIcon].intent != null) {
					mLauncher.startActivity(this.getMenuContainerView(), icons[prevVisibleIcon].intent, null);
				} else {
					//to avoid the addition of fairphone home launcher to appSwitcher
					mLauncher.startEditFavorites();
				}
            }

        } catch (ActivityNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    private void setBackgroundExitAnimation()
    {
        Animation backAnimation = AnimationUtils.loadAnimation(menuRoot.getContext(), R.anim.menu_background_fade_out);

        backAnimation.setAnimationListener(new AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                backVisible = false;
                menuBackground.setVisibility(View.INVISIBLE);
                menuRoot.setVisibility(View.INVISIBLE);
            }
        });

        menuBackground.startAnimation(backAnimation);
    }

    private void setMenuSideExitAnimation()
    {
        // draw back the menu
        // load the correct side for the animation
        Animation animation = null;
        if (getCurrentSide() == Side.LEFT)
        {
            animation = AnimationUtils.loadAnimation(menuRoot.getContext(), R.anim.menu_disappear_to_left_animation);
        }
        else
        {
            animation = AnimationUtils.loadAnimation(menuRoot.getContext(), R.anim.menu_disappear_to_right_animation);
        }

        getMenuContainerView().setVisibility(View.VISIBLE);
        contentVisible = true;
        backVisible = true;
        animation.setAnimationListener(new AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                menuRoot.setVisibility(View.INVISIBLE);
                contentVisible = false;
                if (!backVisible)
                {
                    getMenuContainerView().setVisibility(View.GONE);
                }
            }
        });

        menuContent.startAnimation(animation);
    }

    private void setupLayout()
    {

        LayoutInflater.from(getMenuContainerView().getContext()).inflate(R.layout.fp_fav_apps_layout, getMenuContainerView());

        menuRoot = getMenuContainerView().findViewById(R.id.menuRoot);
        getMenuContainerView().setVisibility(View.INVISIBLE);
        getMenuContainerView().post(new Runnable()
        {
            @Override
            public void run()
            {
                getMenuContainerView().setVisibility(View.GONE);
            }
        });

        menuContent = menuRoot.findViewById(R.id.menuContent);
        menuBackground = getMenuContainerView().findViewById(R.id.menuBackground);

        editGroup = getMenuContainerView().findViewById(R.id.editGroup);
        swipeMenuTopShadow = getMenuContainerView().findViewById(R.id.swipeMenuTopShadow);
        swipeMenuBottomShadow = getMenuContainerView().findViewById(R.id.swipeMenuBottomShadow);
    }

    public void updateIcons()
    {
        ApplicationInfo[] selectedApps = FavoritesStorageHelper.loadSelectedApps(getMenuContainerView().getContext(), MAX_FAVORITE_APPS);
        // set apps
        icons[0] = generateItemForMenu(selectedApps[0], R.id.icon1);
        icons[1] = generateItemForMenu(selectedApps[1], R.id.icon2);
        icons[2] = generateAllAppsMenuItem(R.id.icon3);
        icons[3] = generateItemForMenu(selectedApps[2], R.id.icon4);
        icons[4] = generateItemForMenu(selectedApps[3], R.id.icon5);

        updateIconPosition();
    }

    private ItemIcon generateAllAppsMenuItem(int iconId)
    {
        return new ItemIcon(menuContent.findViewById(iconId), mLauncher.getResources().getDrawable(R.drawable.ic_allapps), "", null);
    }

    private ItemIcon generateItemForMenu(ApplicationInfo applicationInfo, int iconId)
    {
        Intent launchIntent = null;
        ComponentName componentName = null;
        Drawable icon = null;
        String label = "";

        if (applicationInfo == null)
        {
            icon = mLauncher.getResources().getDrawable(R.drawable.edit_holder);
            label = mLauncher.getString(R.string.edit);
        }
        else
        {
            componentName = applicationInfo.componentName;
            icon = new BitmapDrawable(mContext.getResources(), applicationInfo.iconBitmap);
            label = applicationInfo.getApplicationTitle();
            PackageManager pacManager = getMenuContainerView().getContext().getPackageManager();
            launchIntent = pacManager.getLaunchIntentForPackage(componentName.getPackageName());
            launchIntent.setComponent(componentName);
        }

        // Set the right ComponentName in order to launch Phone
        // or Contacts correctly

        return new ItemIcon(menuContent.findViewById(iconId), icon, label, launchIntent);
    }

    private void updateIconPosition()
    {
        float radius = mLauncher.getResources().getDimensionPixelSize(R.dimen.edge_swipe_menu_radius);

        float minAngle = 95;
        float maxAngle = 265;

        float angleDif = (maxAngle - minAngle) / ITEM_COUNT;
        float curAngle = minAngle + angleDif / 2;
        float centerX = (mMenuWidth / 2);
        float centerY = mMenuHeight / 2;
        for (int i = 0; i < ITEM_COUNT; i++)
        {
            double posX =
                    (getCurrentSide() == Side.RIGHT ? centerX + 25 : centerX - 25) + (getCurrentSide() == Side.RIGHT ? 1 : -1)
                            * Math.cos(KWMathUtils.degToRad(curAngle)) * radius;
            double posY = centerY - Math.sin(KWMathUtils.degToRad(curAngle)) * radius;
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) icons[i].rootView.getLayoutParams();

            lp.leftMargin = (int) posX - mIconWidth / 2;
            lp.topMargin = (int) posY - mIconHeight / 2;
            icons[i].rootView.setLayoutParams(lp);

            curAngle += angleDif;
        }
    }

    public boolean isMenuVisible()
    {
        return this.contentVisible;
    }

    public ViewGroup getMenuContainerView()
    {
        return menuContainerView;
    }

    protected void setMenuContainerView(ViewGroup menuContainerView)
    {
        this.menuContainerView = menuContainerView;
    }

    public Side getCurrentSide()
    {
        return mCurrentSide;
    }

    protected void setCurrentSide(Side mCurrentSide)
    {
        this.mCurrentSide = mCurrentSide;
    }

}
