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
package org.fairphone.oobe;

import java.util.ArrayList;
import java.util.Locale;

import org.fairphone.launcher.R;
import org.fairphone.oobe.animation.AddFavTutorialAnimationHelper;
import org.fairphone.oobe.animation.EdgeSwipeTutorialAnimationHelper;
import org.fairphone.oobe.animation.MoveFavTutorialAnimationHelper;
import org.fairphone.oobe.animation.OpenAppTutorialAnimationHelper;
import org.fairphone.oobe.animation.RemoveFavTutorialAnimationHelper;
import org.fairphone.oobe.animation.TutorialAnimationHelper;
import org.fairphone.oobe.animation.TutorialAnimationHelper.TutorialAnimationHelperListener;
import org.fairphone.oobe.animation.TutorialAnimationHelper.TutorialState;
import org.fairphone.oobe.utils.KWFontsManager;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.VideoView;

public class OOBEActivity extends Activity implements TutorialAnimationHelperListener
{

    public static enum OOBESteps
    {
        SELECT_LANGUAGE, SETUP_WIFI, DEVICE_INTRO, EDGE_SWIPE_APPEAR, EDGE_SWIPE_SELECTION, EDIT_INTRO, EDIT_DRAG_NEW_FAVORITE, EDIT_DRAG_REMOVE_FAVORITE, EDIT_DRAG_TRADE_FAVORITE
    };

    public static final String OOBE_TUTORIAL = "OOBE_Tutorial";
    public static final int OOBE_FULL_TUTORIAL = 0;
    public static final int OOBE_DEVICE_TUTORIAL = 1;
    public static final int OOBE_EDIT_FAVORITES_TUTORIAL = 2;
    public static final int OOBE_APP_SWITCHER_TUTORIAL = 3;
	private static final int RESULT_WIFI_SETUP = 500;
	private static final int RESULT_LANGUAGE_SETUP = 501;

    private int mCurrentStep;
    private ArrayList<OOBESteps> mAnimationSteps;

    private View mOOBETextGroup1;
    private View mOOBETextGroup2;
    private View mOOBETextGroup3;
    private View mOOBETextGroup4;
    private View mOOBETextGroup5;
    private View mOOBETextGroup6;
    private View mOOBETextGroup7;
    private View mOOBETextGroup8;
    private View mOOBETextGroup9;

    private ImageButton mSelectLanguageButton;
    private ImageButton mSetupWifiButton;

    private Button mStartButton;
    private Button mSkipButton;
    private Button mNextButton;
    private Button mBackButton;
    private int mTutorialToShow;

    private TutorialAnimationHelper mCurAnimHelper;
    private TutorialAnimationHelper mNextAnimHelper;

    private ViewGroup mAnimationHolder;

    private VideoView mVideo;
    private Button mSkipVideoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fp_oobe_activity);

        Bundle extra = getIntent() != null ? getIntent().getExtras() : null;
        if (extra != null)
        {
            mTutorialToShow = extra.getInt(OOBE_TUTORIAL);
        }
        else
        {
            mTutorialToShow = OOBE_FULL_TUTORIAL;
        }

        mAnimationSteps = new ArrayList<OOBEActivity.OOBESteps>();

        mCurrentStep = 0;

        mCurAnimHelper = null;
        mNextAnimHelper = null;

        setupLayout();

        setupListeners();

        setupAnimationStart();

        setupFonts();
    }

    private void setupFonts()
    {
        ViewGroup root = (ViewGroup) ((ViewGroup) getWindow().getDecorView()).getChildAt(0);
        KWFontsManager.prepareFonts(this, root);

    }

    @Override
    public void onBackPressed()
    {

    }

    private void setupLayout()
    {

        mAnimationHolder = (ViewGroup) findViewById(R.id.tutorialAnimationViewContainer);

        mStartButton = (Button) findViewById(R.id.startButton);
        mSkipButton = (Button) findViewById(R.id.skipButton);
        mNextButton = (Button) findViewById(R.id.nextButton);
        mBackButton = (Button) findViewById(R.id.backButton);

        mOOBETextGroup1 = (View) findViewById(R.id.oobeTextGroup1);
        mOOBETextGroup2 = (View) findViewById(R.id.oobeTextGroup2);
        mOOBETextGroup3 = (View) findViewById(R.id.oobeTextGroup3);
        mOOBETextGroup4 = (View) findViewById(R.id.oobeTextGroup4);
        mOOBETextGroup5 = (View) findViewById(R.id.oobeTextGroup5);
        mOOBETextGroup6 = (View) findViewById(R.id.oobeTextGroup6);
        mOOBETextGroup7 = (View) findViewById(R.id.oobeTextGroup7);
        mOOBETextGroup8 = (View) findViewById(R.id.oobeTextGroup8);
        mOOBETextGroup9 = (View) findViewById(R.id.oobeTextGroup9);

        mSelectLanguageButton = (ImageButton) findViewById(R.id.selectLanguageButton);
        mSetupWifiButton = (ImageButton) findViewById(R.id.setupWifiButton);

        mSkipButton.setVisibility(View.GONE);
        mBackButton.setVisibility(View.GONE);
        mNextButton.setVisibility(View.GONE);

        mSelectLanguageButton.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                startActivityForResult(new Intent(Settings.ACTION_LOCALE_SETTINGS), RESULT_LANGUAGE_SETUP);
            }
        });

        mSetupWifiButton.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), RESULT_WIFI_SETUP);
            }
        });
    }
    
    
    @Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
    	
    	switch(requestCode){
    		case RESULT_WIFI_SETUP:
    			jumpToNextSlide();
    			break;
    		case RESULT_LANGUAGE_SETUP:
    			jumpToNextSlide();
    			break;
    	}
    }

    private void setupAnimationStart()
    {
        switch (mTutorialToShow)
        {
            case OOBE_FULL_TUTORIAL:
            {
                setupTheVideo();
                setupFullTutorialSteps();
                break;
            }
            case OOBE_DEVICE_TUTORIAL:
            {
                setupTheVideo();
                setupDefinitionsSteps();
                setupEdgeSwipeTutorialSteps();
                break;
            }
            case OOBE_EDIT_FAVORITES_TUTORIAL:
            {
                hideVideo();
                setupEditFavoritesTutorialSteps();
                startTutorialInitialAnimation();
                break;
            }
            default:
            {
                mTutorialToShow = OOBE_FULL_TUTORIAL;
                setupTheVideo();
                setupFullTutorialSteps();
                break;
            }
        }

        startStepAnimation(mAnimationSteps.get(0));

    }

    private void hideVideo()
    {
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.oobeVideoViewGroup);
        rl.setVisibility(View.GONE);
    }

    private void setupEditFavoritesTutorialSteps()
    {
        mAnimationSteps.add(OOBESteps.EDIT_INTRO);
        mAnimationSteps.add(OOBESteps.EDIT_DRAG_NEW_FAVORITE);
        mAnimationSteps.add(OOBESteps.EDIT_DRAG_REMOVE_FAVORITE);
        mAnimationSteps.add(OOBESteps.EDIT_DRAG_TRADE_FAVORITE);
    }

    private void setupDefinitionsSteps()
    {
        mAnimationSteps.add(OOBESteps.SELECT_LANGUAGE);
        mAnimationSteps.add(OOBESteps.SETUP_WIFI);
    }

    private void setupEdgeSwipeTutorialSteps()
    {
        mAnimationSteps.add(OOBESteps.DEVICE_INTRO);
        mAnimationSteps.add(OOBESteps.EDGE_SWIPE_APPEAR);
        mAnimationSteps.add(OOBESteps.EDGE_SWIPE_SELECTION);
    }

    private void setupTheVideo()
    {

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.oobeVideoViewGroup);
        rl.setVisibility(View.VISIBLE);

        mVideo = (VideoView) findViewById(R.id.fp_oobe_video);

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.fp_buy_a_phone_start_a_movement);

        mVideo.setMediaController(null);
        mVideo.requestFocus();
        mVideo.setVideoURI(uri);

        mVideo.setOnTouchListener(new OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });

        mVideo.start();

        mVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {

            @Override
            public void onCompletion(MediaPlayer mp)
            {
                stopIntroVideo();
            }
        });

        // button
        mSkipVideoButton = (Button) findViewById(R.id.fp_oobe_video_skip_button);

        mSkipVideoButton.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                stopIntroVideo();
            }
        });
    }

    private void stopIntroVideo()
    {
        mVideo.stopPlayback();

        final RelativeLayout rl = (RelativeLayout) findViewById(R.id.oobeVideoViewGroup);
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(OOBEActivity.this, R.anim.fade_out_slow);
        rl.startAnimation(fadeOutAnimation);

        fadeOutAnimation.setAnimationListener(new AnimationListener()
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
                rl.setVisibility(View.GONE);
                mVideo.setVisibility(View.GONE);
                mSkipVideoButton.setVisibility(View.GONE);
            }
        });

        startTutorialInitialAnimation();
    }

    private void startTutorialInitialAnimation()
    {
        Animation oobeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_slow);
        findViewById(R.id.oobe_background).startAnimation(oobeAnimation);
    }

    private void setupFullTutorialSteps()
    {
        setupEdgeSwipeTutorialSteps();
        setupEditFavoritesTutorialSteps();
    }

    private void jumpToNextSlide() {
		// increment the step counter
        mCurrentStep++;

        if (mCurrentStep < mAnimationSteps.size())
        {
            startStepAnimation(mAnimationSteps.get(mCurrentStep));
        }
	}
    
    private void setupListeners()
    {
        mSkipButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            	if(mCurrentStep < 2 && (mTutorialToShow == OOBE_DEVICE_TUTORIAL)){
            		jumpToNextSlide();
            	}else {
            		endOOBEActivity();
            	}
            }
        });

        mStartButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                jumpToNextSlide();
            }

			

        });

        mNextButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // increment the step counter
                mCurrentStep++;

                if (mCurrentStep < mAnimationSteps.size())
                {
                    startStepAnimation(mAnimationSteps.get(mCurrentStep));
                }
                else
                {
                    endOOBEActivity();
                }
            }

        });

        mBackButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                // decrement the step counter
                mCurrentStep--;

                if (mCurrentStep >= 0)
                {
                    startStepAnimation(mAnimationSteps.get(mCurrentStep));
                }
                else
                {
                    endOOBEActivity();
                }
            }

        });
    }

    private void startStepAnimation(OOBESteps step)
    {
        switch (step)
        {
            case SELECT_LANGUAGE:
                startAnimation(null);
                setTextView(mOOBETextGroup1);
                break;
            case SETUP_WIFI:
                startAnimation(null);
                setTextView(mOOBETextGroup2);
                break;
            case DEVICE_INTRO:
                startAnimation(null);
                setTextView(mOOBETextGroup3);
                break;
            case EDGE_SWIPE_APPEAR:
                startAnimation(new EdgeSwipeTutorialAnimationHelper());
                setTextView(mOOBETextGroup4);
                break;
            case EDGE_SWIPE_SELECTION:
                startAnimation(new OpenAppTutorialAnimationHelper());
                setTextView(mOOBETextGroup5);
                break;
            case EDIT_INTRO:
                startAnimation(null);
                setTextView(mOOBETextGroup6);
                break;
            case EDIT_DRAG_NEW_FAVORITE:
                startAnimation(new AddFavTutorialAnimationHelper());
                setTextView(mOOBETextGroup7);
                break;
            case EDIT_DRAG_REMOVE_FAVORITE:
                startAnimation(new RemoveFavTutorialAnimationHelper());
                setTextView(mOOBETextGroup8);
                break;
            case EDIT_DRAG_TRADE_FAVORITE:
                startAnimation(new MoveFavTutorialAnimationHelper());
                setTextView(mOOBETextGroup9);
                break;
            default:

        }

        setupButtons();
    }

    private void setupButtons() {

		int visibilityNext = View.VISIBLE;
		int visibilityBack = View.VISIBLE;
		int visibilityStart = View.VISIBLE;
		int visibilitySkip = View.VISIBLE;

		if (mCurrentStep <= 0) {

			if (mTutorialToShow == OOBE_DEVICE_TUTORIAL) {
				if (mCurrentStep < 1) {
					visibilityBack = View.GONE;
					visibilityStart = View.GONE;
					visibilityNext = View.GONE;
				} else {
					visibilityStart = View.GONE;
					visibilitySkip = View.GONE;
					visibilityNext = View.GONE;
				}
			} else {
				visibilityNext = View.GONE;
				visibilityBack = View.GONE;
				visibilitySkip = View.GONE;
			}
		} else if (mCurrentStep >= (mAnimationSteps.size() - 1)) {

			mSkipButton.setText(getResources().getString(R.string.oobe_done));
			visibilityNext = View.GONE;
			visibilityStart = View.GONE;
		} else {
			mSkipButton.setText(getResources().getString(R.string.oobe_skip));
			visibilityStart = View.GONE;
			if (mTutorialToShow == OOBE_DEVICE_TUTORIAL) {
				if (mCurrentStep < 2) {
					visibilityStart = View.GONE;
					visibilityNext = View.GONE;
				}
				
				if (mCurrentStep < 1) {
					visibilityBack = View.GONE;
				}
			}

		}

		mNextButton.setVisibility(visibilityNext);
		mBackButton.setVisibility(visibilityBack);
		mStartButton.setVisibility(visibilityStart);
		mSkipButton.setVisibility(visibilitySkip);
	}


    private void setTextView(View targetTextView)
    {
        mOOBETextGroup1.setVisibility(targetTextView == mOOBETextGroup1 ? View.VISIBLE : View.GONE);
//        Log.d("OOBE", "mTextGroup1 is " + (mOOBETextGroup1.getVisibility() == View.VISIBLE ? " visible" : " gone"));
        mOOBETextGroup2.setVisibility(targetTextView == mOOBETextGroup2 ? View.VISIBLE : View.GONE);
//        Log.d("OOBE", "mTextGroup2 is " + (mOOBETextGroup2.getVisibility() == View.VISIBLE ? " visible" : " gone"));
        mOOBETextGroup3.setVisibility(targetTextView == mOOBETextGroup3 ? View.VISIBLE : View.GONE);
//        Log.d("OOBE", "mTextGroup3 is " + (mOOBETextGroup3.getVisibility() == View.VISIBLE ? " visible" : " gone"));
        mOOBETextGroup4.setVisibility(targetTextView == mOOBETextGroup4 ? View.VISIBLE : View.GONE);
//        Log.d("OOBE", "mTextGroup4 is " + (mOOBETextGroup4.getVisibility() == View.VISIBLE ? " visible" : " gone"));
        mOOBETextGroup5.setVisibility(targetTextView == mOOBETextGroup5 ? View.VISIBLE : View.GONE);
//        Log.d("OOBE", "mTextGroup5 is " + (mOOBETextGroup5.getVisibility() == View.VISIBLE ? " visible" : " gone"));
        mOOBETextGroup6.setVisibility(targetTextView == mOOBETextGroup6 ? View.VISIBLE : View.GONE);
//        Log.d("OOBE", "mTextGroup6 is " + (mOOBETextGroup6.getVisibility() == View.VISIBLE ? " visible" : " gone"));
        mOOBETextGroup7.setVisibility(targetTextView == mOOBETextGroup7 ? View.VISIBLE : View.GONE);
//        Log.d("OOBE", "mTextGroup7 is " + (mOOBETextGroup7.getVisibility() == View.VISIBLE ? " visible" : " gone"));

        mOOBETextGroup8.setVisibility(targetTextView == mOOBETextGroup8 ? View.VISIBLE : View.GONE);
//        Log.d("OOBE", "mTextGroup8 is " + (mOOBETextGroup8.getVisibility() == View.VISIBLE ? " visible" : " gone"));

        mOOBETextGroup9.setVisibility(targetTextView == mOOBETextGroup9 ? View.VISIBLE : View.GONE);
//        Log.d("OOBE", "mTextGroup9 is " + (mOOBETextGroup9.getVisibility() == View.VISIBLE ? " visible" : " gone"));

        Animation oobeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_fast);

        targetTextView.startAnimation(oobeAnimation);

        targetTextView.setVisibility(View.VISIBLE);
    }

    private void endOOBEActivity()
    {
        mSkipButton.setVisibility(View.GONE);
        mNextButton.setVisibility(View.GONE);
        mBackButton.setVisibility(View.GONE);

        finish();
    }

    void startAnimation(TutorialAnimationHelper anim)
    {
        if (anim != null)
        {
            if (mCurAnimHelper == null)
            {
                mCurAnimHelper = anim;
                mCurAnimHelper.setTutorialAnimationHelperListener(this);
                View animHelperRootView = mCurAnimHelper.setup(getApplicationContext());
                mAnimationHolder.addView(animHelperRootView);
                mCurAnimHelper.playIntro();
            }
            else
            {
                mCurAnimHelper.playOutro();
                mNextAnimHelper = anim;
            }
        }
        else
        {
            if (mCurAnimHelper != null)
            {
                mCurAnimHelper.playOutro();
                mNextAnimHelper = null;
            }
        }
    }

    @Override
    public void onTutorialAnimationFinished(TutorialAnimationHelper helper, TutorialState state)
    {
        if (helper == mCurAnimHelper)
        {
            if (state == TutorialState.Outro)
            {
                mAnimationHolder.removeAllViews();
                mCurAnimHelper = mNextAnimHelper;
                mNextAnimHelper = null;
                if (mCurAnimHelper != null)
                {
                    View animHelperRootView = mCurAnimHelper.setup(getApplicationContext());
                    mCurAnimHelper.setTutorialAnimationHelperListener(this);
                    mAnimationHolder.addView(animHelperRootView);
                    mCurAnimHelper.playIntro();
                }
            }
            else if (state == TutorialState.Intro)
            {
                mCurAnimHelper.playMain();
            }
        }
    }

    public static void changeLocale(Locale locale)
    {
        try
        {
            Class<?> activityManagerNative = Class.forName("android.app.ActivityManagerNative");

            Object am = activityManagerNative.getMethod("getDefault").invoke(activityManagerNative);

            Object config = am.getClass().getMethod("getConfiguration").invoke(am);
            config.getClass().getDeclaredField("locale").set(config, locale);
            config.getClass().getDeclaredField("userSetLocale").setBoolean(config, true);

            am.getClass().getMethod("updateConfiguration", android.content.res.Configuration.class).invoke(am, config);
//            Log.i("", "send change locale request");
        } catch (Exception e)
        {
            Log.e("", "change locale error:", e);
        }
    }

	public TutorialAnimationHelper getCurrentAnimationHelper() {
		return mCurAnimHelper;
	}
}
