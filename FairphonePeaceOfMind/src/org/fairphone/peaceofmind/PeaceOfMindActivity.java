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
package org.fairphone.peaceofmind;

import org.fairphone.fairphonepeaceofmindapp.R;
import org.fairphone.peaceofmind.data.PeaceOfMindStats;
import org.fairphone.peaceofmind.ui.VerticalScrollListener;
import org.fairphone.peaceofmind.ui.VerticalSeekBar;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class PeaceOfMindActivity extends Activity implements VerticalScrollListener, PeaceOfMindApplicationBroadcastReceiver.Listener, OnPreparedListener, OnCompletionListener
{
    public static String START_PEACE_OF_MIND = "START_PEACE_OF_MIND";
    public static String END_PEACE_OF_MIND = "END_PEACE_OF_MIND";
    public static String UPDATE_PEACE_OF_MIND = "UPDATE_PEACE_OF_MIND";
    public static final String TIMER_TICK = "TIMER_TICK";
    public static int count = 0;

    private static final float INITIAL_PERCENTAGE = 0.1f;
    public static final int MINUTE = 60 * 1000;
    public static final int HOUR = 60 * MINUTE;
    protected static final String TAG = PeaceOfMindActivity.class.getSimpleName();
    public static final String BROADCAST_TARGET_PEACE_OF_MIND = "BROADCAST_TARGET_PEACE_OF_MIND";
    private TextView mTotalTimeText;

    private LinearLayout mCurrentTimeGroup;
    private TextView mCurrentTimeText;
    private LinearLayout mCurrentToTimeGroup;
    private TextView mCurrentToTimeText;
    private TextView mCurrentToText;

    private TextView mCurrentTimeAtText;
    private TextView mCurrentTimePEACEText;
    private LinearLayout mCurrentTimeInPeaceText;
    private VerticalSeekBar mVerticalSeekBar;
    private View mProgressView;
    private Button mHelpButton;
    private View mSeekbarBackgroundOff;
    private View mSeekbarBackgroundOn;

    private FrameLayout mHelpHolder;
    private LinearLayout mHelpLayout;
    private Button mCloseButton;

    private VideoView mVideo;

    private PeaceOfMindApplicationBroadcastReceiver mBroadCastReceiver;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupLayout();

        registerForPeaceOfMindBroadCasts();

        setupBroadCastReceiverAlarm();
    }

    private void loadAvailableData()
    {
        PeaceOfMindStats currentStats = PeaceOfMindStats.getStatsFromSharedPreferences(mSharedPreferences);

        mVerticalSeekBar.setThumb(getResources().getDrawable(currentStats.mIsOnPeaceOfMind ? R.drawable.seekbar_thumb_on : R.drawable.seekbar_thumb_off));
        mVerticalSeekBar.setThumbOffset(0);
        if (currentStats.mIsOnPeaceOfMind)
        {
            float targetTimePercent = (float) currentStats.mCurrentRun.mTargetTime / (float) PeaceOfMindStats.MAX_TIME;

            mVerticalSeekBar.setInvertedProgress((int) (targetTimePercent * mVerticalSeekBar.getHeight()));

            updateTextForNewTime(currentStats.mCurrentRun.mPastTime, currentStats.mCurrentRun.mTargetTime);
            updateTimeTextLabel(targetTimePercent * 100);
            updateScreenTexts();
        }
        else
        {
            mTotalTimeText.setText(generateStringTimeFromMillis(0, true));
            mCurrentTimeText.setText(generateStringTimeFromMillis(0, true));
        }

        updateBackground(currentStats.mIsOnPeaceOfMind);
        mVideo.setBackgroundResource(currentStats.mIsOnPeaceOfMind ? R.drawable.background_on_repeat : R.drawable.background_off_repeat);
    }

    private void updateScreenTexts()
    {
        PeaceOfMindStats currentStats = PeaceOfMindStats.getStatsFromSharedPreferences(mSharedPreferences);

        int blue = getResources().getColor(R.color.blue);
        int grey = getResources().getColor(R.color.blue_grey);

        if (currentStats.mIsOnPeaceOfMind)
        {
            // current time is blue
            mCurrentTimeText.setTextColor(blue);
            mCurrentTimeAtText.setTextColor(blue);
            mCurrentTimePEACEText.setTextColor(blue);

            mCurrentTimeText.setAlpha(1.0f);
            mCurrentTimeAtText.setAlpha(1.0f);
            mCurrentTimePEACEText.setAlpha(1.0f);

            mCurrentToTimeGroup.setVisibility(View.VISIBLE);
            
            mSeekbarBackgroundOff.setVisibility(View.GONE);
            mSeekbarBackgroundOn.setVisibility(View.VISIBLE);
        }
        else
        {
            // show the current time and text at grey
            mCurrentTimeText.setTextColor(grey);
            mCurrentTimeAtText.setTextColor(grey);
            mCurrentTimePEACEText.setTextColor(grey);

            mCurrentTimeText.setAlpha(0.5f);
            mCurrentTimeAtText.setAlpha(0.5f);
            mCurrentTimePEACEText.setAlpha(0.5f);

            mCurrentToTimeGroup.setVisibility(View.INVISIBLE);
            
            mSeekbarBackgroundOff.setVisibility(View.VISIBLE);
            mSeekbarBackgroundOn.setVisibility(View.GONE);
        }

        if (mTotalTimeText.getVisibility() == View.VISIBLE)
        {
        	//hide the current time group when the target time approaches 
        	//TODO: Fix the ugly magical numbers
        	float position = mCurrentTimeGroup.getY() - mTotalTimeText.getY();
            float alpha = (position < 500) ? (10.0f * (position-50) / 100.0f) : 1.0f;
            mCurrentTimeGroup.setAlpha(alpha);
            mCurrentTimeInPeaceText.setAlpha(alpha);
        }
        else
        {
        	if(mCurrentTimeGroup.getAlpha() != 1.0f ){
        		if(!currentStats.mIsOnPeaceOfMind){
	        		Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.target_time_fade_in_fast);
	        		mCurrentTimeGroup.startAnimation(fadeIn);
	        		mCurrentTimeInPeaceText.startAnimation(fadeIn);
        		}
	            mCurrentTimeGroup.setAlpha(1.0f);
	            mCurrentTimeInPeaceText.setAlpha(1.0f);
        	}
        }
    }

    private void updateBackground(boolean on)
    {
    	View backgroundOverlay = findViewById(R.id.backgroundOverlay);
        
        int backgroundDrawableId = on ? R.drawable.background_on_repeat : R.drawable.background_off_repeat;
        
        // setup the background
        backgroundOverlay.setBackgroundResource(backgroundDrawableId);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        mVideo.setVisibility(View.INVISIBLE);
        mVideo.stopPlayback();
        
        // load data from the shared preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        updateScreenTexts();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (isFinishing())
        {
            unRegisterForPeaceOfMindBroadCasts();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    private void setupLayout()
    {
        mTotalTimeText = (TextView) findViewById(R.id.timeTextTotal);

        mCurrentTimeGroup = (LinearLayout) findViewById(R.id.timeTextCurrentGroup);
        mCurrentTimeText = (TextView) findViewById(R.id.timeTextCurrent);
        mCurrentToTimeGroup = (LinearLayout) findViewById(R.id.toTimeGroup);
        mCurrentToTimeText = (TextView) findViewById(R.id.toTimeText);
        mCurrentToText = (TextView) findViewById(R.id.toText);

        mCurrentTimeInPeaceText = (LinearLayout) findViewById(R.id.inPeaceTextCurrent);

        mCurrentTimeAtText = (TextView) findViewById(R.id.currentAtText);
        mCurrentTimePEACEText = (TextView) findViewById(R.id.currentPeaceText);

        mVerticalSeekBar = (VerticalSeekBar) findViewById(R.id.verticalSeekBar);

        mProgressView = (View) findViewById(R.id.progressView);
        mHelpButton = (Button) findViewById(R.id.helpButton);
        mSeekbarBackgroundOff = findViewById(R.id.seekbar_background_off);
        mSeekbarBackgroundOn = findViewById(R.id.seekbar_background_on);

        if (mVerticalSeekBar != null)
        {
            mVerticalSeekBar.setPeaceListener(this);
        }

        mHelpHolder = (FrameLayout) findViewById(R.id.helpHolder);
        mHelpLayout = (LinearLayout) findViewById(R.id.helpLayout);
        mCloseButton = (Button) findViewById(R.id.closeButton);

        mHelpButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showHelp();
            }
        });

        mCloseButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                hideHelp();
            }
        });

        mVideo = (VideoView) findViewById(R.id.pomVideo);

        mVideo.setVisibility(View.INVISIBLE);

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.fp_start_pom_video);

        mVideo.setMediaController(null);
        mVideo.requestFocus();
        mVideo.setVideoURI(uri);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        loadAvailableData();
    }

    public void showHelp()
    {
        // disable the seekbar and help button when the help is showed
        mVerticalSeekBar.setEnabled(false);
        mHelpButton.setEnabled(false);

        mHelpHolder.setVisibility(View.VISIBLE);
        ObjectAnimator showIn = ObjectAnimator.ofFloat(mHelpHolder, "alpha", 0, 1);
        showIn.setDuration(400);
        showIn.start();

        showIn.addListener(new AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                mHelpLayout.setVisibility(View.VISIBLE);
                Interpolator decelerator = new DecelerateInterpolator();
                ObjectAnimator translateIn = ObjectAnimator.ofFloat(mHelpLayout, "translationY", 900f, 0f);
                translateIn.setInterpolator(decelerator);
                translateIn.setDuration(400);
                translateIn.start();
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }
        });

    }

    public void hideHelp()
    {
        ObjectAnimator showIn = ObjectAnimator.ofFloat(mHelpHolder, "alpha", 1, 0);
        showIn.setDuration(400);
        showIn.start();
        showIn.addListener(new AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                mHelpButton.setEnabled(true);
                mVerticalSeekBar.setEnabled(true);
                mHelpHolder.setVisibility(View.GONE);
                mHelpLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
            }
        });

    }
    
    @Override
    public void onBackPressed() {
        if (mHelpLayout.getVisibility() == View.VISIBLE) {
            hideHelp();
        } else {
            // allows standard use of back button for page 1
            super.onBackPressed();
        }
    }

    private void registerForPeaceOfMindBroadCasts()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_STARTED);
        filter.addAction(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_UPDATED);
        filter.addAction(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_ENDED);
        filter.addAction(PeaceOfMindApplicationBroadcastReceiver.PEACE_OF_MIND_TICK);

        mBroadCastReceiver = new PeaceOfMindApplicationBroadcastReceiver(this);
        registerReceiver(mBroadCastReceiver, filter);
    }

    private void unRegisterForPeaceOfMindBroadCasts()
    {
        unregisterReceiver(mBroadCastReceiver);
    }

    private void setupBroadCastReceiverAlarm()
    {
        Log.d(TAG, "Setting the alarm tick");
        Intent alarmIntent = new Intent(this, PeaceOfMindBroadCastReceiver.class);
        alarmIntent.setAction(PeaceOfMindActivity.TIMER_TICK);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), MINUTE, pendingIntent);
    }

    @Override
    public void updateBarScroll(float progress)
    {
        // TODO: Put the 612 seekbar dimension in resources in dp if possible
        int pos = (int) (612 - (612 / 100 * progress) + (progress / 2) - 53);

        updateTimeTextLabel(progress);

        if(mTotalTimeText.getVisibility() == View.INVISIBLE){
        	mTotalTimeText.setVisibility(View.VISIBLE);
        	Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.target_time_fade_in_fast);
        	mTotalTimeText.startAnimation(fadeIn);
        }
        mTotalTimeText.setY(pos);

        updateScreenTexts();
    }

    @Override
    public synchronized void scrollEnded(float percentage)
    {
        if(mTotalTimeText.getVisibility() == View.VISIBLE){
        	Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out_fast);
	    	mTotalTimeText.startAnimation(fadeOut);
	    	fadeOut.setAnimationListener(new AnimationListener()
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
	    	    	mTotalTimeText.setVisibility(View.INVISIBLE);
	    	    	updateScreenTexts();
	            }
	        });
        }

        long targetTime = roundToInterval((long) (percentage * PeaceOfMindStats.MAX_TIME));

        Intent intent = new Intent(getApplicationContext(), PeaceOfMindBroadCastReceiver.class);
        intent.setAction(PeaceOfMindActivity.UPDATE_PEACE_OF_MIND);

        intent.putExtra(PeaceOfMindActivity.BROADCAST_TARGET_PEACE_OF_MIND, targetTime);

        sendBroadcast(intent);
    }

    private long roundToInterval(long time)
    {

        int hours = (int) (time / HOUR);
        int minutes = (int) ((time - hours * HOUR) / MINUTE);

        int index = minutes % 10;

        long newTime = 0;

        switch (index)
        {
            case 1:
            case 6:
                newTime -= MINUTE;
                break;
            case 2:
            case 7:
                newTime -= 2 * MINUTE;
                break;
            case 3:
            case 8:
                newTime += 2 * MINUTE;
                break;
            case 4:
            case 9:
                newTime += MINUTE;
                break;
        }

        Log.d(TAG, "Index: " + index + " - " + newTime);

        return time + newTime;
    }

    private void updateTextForNewTime(long timePast, long targetTime)
    {
        long maxTime = PeaceOfMindStats.MAX_TIME;

        float timePercentage = 0;

        long timeUntilTarget = targetTime - timePast;
        mCurrentTimeText.setText(generateStringTimeFromMillis(timeUntilTarget, timeUntilTarget <= 0));

        int finalY = getCurrentProgressY(timePast, targetTime, maxTime, timePercentage);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, finalY);

        mProgressView.setLayoutParams(params);

        float pos = mVerticalSeekBar.getHeight() - finalY - 12;
        mCurrentTimeGroup.setY(pos);
        mCurrentTimeInPeaceText.setY(pos);

    }

    private int getCurrentProgressY(long timePast, long targetTime, long maxTime, float timePercentage)
    {
        if (targetTime > 0)
        {
            timePercentage = (((float) timePast / (float) (maxTime)));
        }

        System.out.println("Updating time to " + timePercentage + " - " + timePast + " target time " + targetTime);

        int finalY = (int) (0.8f * mVerticalSeekBar.getHeight() * timePercentage + (mVerticalSeekBar.getHeight() * INITIAL_PERCENTAGE));
        return finalY;
    }

    private String generateStringTimeFromMillis(long timePast, boolean reset)
    {
    	int hours = 0;
    	int minutes = 0;
    	if(!reset){
	        hours = (int) (timePast / HOUR);
	        int timeInMinutes = (int) (timePast - hours * HOUR);
	        
	        if(hours == 0){
	        	minutes = timeInMinutes - MINUTE > 0 ? timeInMinutes / MINUTE : 1;
	        }else{
	        	minutes = timeInMinutes / MINUTE;
	        }
    	}

        String timeStr = String.format("%d%s%02d", hours, getResources().getString(R.string.hour_separator), minutes);
        if(hours == 0){
        	mCurrentToText.setText(getResources().getString(R.string.to_m));
        }else{
        	mCurrentToText.setText(getResources().getString(R.string.to_h));
        }

        return timeStr;
    }

    private void updateTimeTextLabel(float progress)
    {
        long targetTime = roundToInterval((long) (PeaceOfMindStats.MAX_TIME * progress / 100.0f));

        mTotalTimeText.setText(generateStringTimeFromMillis(targetTime, targetTime == 0));
        mCurrentToTimeText.setText(generateStringTimeFromMillis(targetTime, targetTime == 0));
    }

    @Override
    public void peaceOfMindTick(long pastTime, long targetTime)
    {
        updateTextForNewTime(pastTime, targetTime);
        updateScreenTexts();
    }

    static Semaphore mSemaphore = new Semaphore(1);
    
    @Override
    public synchronized void peaceOfMindStarted(long targetTime)
    {
    	try {
			mSemaphore.tryAcquire(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out_fast);
        mSeekbarBackgroundOff.startAnimation(fadeOut);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_fast);

        mSeekbarBackgroundOn.startAnimation(fadeIn);

        fadeIn.setAnimationListener(new AnimationListener()
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
            	mSeekbarBackgroundOff.setVisibility(View.GONE);
                mSeekbarBackgroundOn.setVisibility(View.VISIBLE);
                
                mSemaphore.release();
            }
        });

        mVerticalSeekBar.setThumb(getResources().getDrawable(R.drawable.seekbar_thumb_on));
        mVerticalSeekBar.setThumbOffset(0);

        // fix thumb position
        float targetTimePercent = (float) targetTime / (float) PeaceOfMindStats.MAX_TIME;

        updateTextForNewTime(0, targetTime);
        mVerticalSeekBar.setInvertedProgress((int) (targetTimePercent * mVerticalSeekBar.getHeight()));

        startPeaceOfMindVideo();
    }

    private void startPeaceOfMindVideo()
    {
		mVideo.setBackgroundResource(R.drawable.background_off_repeat);
        mVideo.setVisibility(View.VISIBLE);

        mVideo.setOnPreparedListener(this);
        mVideo.setOnCompletionListener(this);
        mVideo.setDrawingCacheEnabled(true);
    }

    private void stopPeaceOfMindVideo()
    {   
		mVideo.removeCallbacks(null);
		if(mVideo.getVisibility() != View.VISIBLE){
			mVideo.setVisibility(View.VISIBLE);
		}
		
		Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out_fast);
		mVideo.startAnimation(fadeOut);

		updateBackground(false);
        fadeOut.setAnimationListener(new AnimationListener()
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
            	mVideo.setVisibility(View.INVISIBLE);
                mVideo.stopPlayback();
            }
        });
    }

    public void onPrepared(MediaPlayer mp)
    {
    	//Used to avoid the initial black flicker
    	//remove the foreground 30 miliseconds after the video starts
    	mVideo.postDelayed(new Runnable() {
            public void run()
            {
                if (mVideo.isPlaying()){
                	mVideo.setBackgroundResource(0);
                    return;
                }

            }
        }, 30);
    	
    	//Used to avoid the final black flicker
    	//remove the foreground 20 miliseconds before the video ends
    	mVideo.postDelayed(new Runnable() {
            public void run()
            {
                if (mVideo.isPlaying()){
                	mVideo.setBackgroundResource(R.drawable.background_on_repeat);		
                    return;
                }

            }
        }, mVideo.getDuration() - 20);
    	mVideo.start();
    }
    

	@Override
	public void onCompletion(MediaPlayer mp) {
		updateBackground(true);
		mVideo.removeCallbacks(null);
		mVideo.setBackgroundResource(R.drawable.background_on_repeat);
		mVideo.stopPlayback();
	}

    @Override
    public synchronized void peaceOfMindEnded()
    {
    	try {
			mSemaphore.tryAcquire(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_fast);

        mSeekbarBackgroundOff.startAnimation(fadeIn);

        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out_fast);
        mSeekbarBackgroundOn.startAnimation(fadeOut);

        fadeIn.setAnimationListener(new AnimationListener()
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
                mSeekbarBackgroundOff.setVisibility(View.VISIBLE);
                mSeekbarBackgroundOn.setVisibility(View.GONE);

            	updateScreenTexts();
                stopPeaceOfMindVideo();
                
                mSemaphore.release();
            }
        });
        
    	updateScreenTexts();
        mVerticalSeekBar.setThumb(getResources().getDrawable(R.drawable.seekbar_thumb_off));
        mVerticalSeekBar.setThumbOffset(0);
        mVerticalSeekBar.setInvertedProgress(0);
        updateTextForNewTime(0, 0);
        updateTimeTextLabel(0);

        mTotalTimeText.setVisibility(View.INVISIBLE);
    }

    @Override
    public void peaceOfMindUpdated(long pastTime, long newTargetTime)
    {
        updateTextForNewTime(pastTime, newTargetTime);
    }
}
