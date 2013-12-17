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
import org.fairphone.oobe.animation.EditFavsTutorialAnimationView.EditFavsTutorialAnimationViewListener;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class AddFavTutorialAnimationHelper implements TutorialAnimationHelper, EditFavsTutorialAnimationViewListener
{
    EditFavsTutorialAnimationView animView;
    View rootView;
    Context context;
    TutorialAnimationHelperListener listener;
    TutorialState curState = TutorialState.IdleInvisible;
    
    
    @Override
    public View setup(Context context)
    {
        this.context = context;
        
        rootView = LayoutInflater.from(context).inflate(R.layout.tutorial_edit_fav_add_layout, null);
        animView = (EditFavsTutorialAnimationView)rootView.findViewById(R.id.editFavAddAnimationView);
        
        animView.playAddFavAnimation();
        animView.stopAnimations();
        
        animView.setEditFavsTutorialAnimationViewListener(this);
        
        return rootView;
    }


    @Override
    public void setTutorialAnimationHelperListener(TutorialAnimationHelperListener listener)
    {
        this.listener = listener;
    }

    int curAnimationId = 0;
    TutorialViewAnimationListener curStateAnimationListener;
    
    private TutorialViewAnimationListener getCurStateAnimationListener()
    {
        return curStateAnimationListener;
    }
    
    @Override
    public boolean playIntro()
    {
        if(curState==TutorialState.Intro)
            return false;
        startAnimationState(TutorialState.Intro,null);
        Animation fadeAnim = AnimationUtils.loadAnimation(context, R.anim.tutorial_intro);
        fadeAnim.setAnimationListener(getCurStateAnimationListener());
        rootView.setVisibility(View.VISIBLE);
        rootView.startAnimation(fadeAnim);
        return true;
    }


    @Override
    public boolean playMain()
    {
        if(curState!=TutorialState.IdleVisible)
            return false;
        
        startAnimationState(TutorialState.Main,null);
        animView.playAddFavAnimation();
        return true;
    }
    
    @Override
    public boolean playOutro()
    {
        if(curState==TutorialState.Outro)
            return false;
        
        startAnimationState(TutorialState.Outro, new Runnable()
        {
            @Override
            public void run()
            {
                rootView.setVisibility(View.GONE);
            }
        });
        Animation fadeAnim = AnimationUtils.loadAnimation(context, R.anim.tutorial_outro);
        fadeAnim.setAnimationListener(getCurStateAnimationListener());
        rootView.startAnimation(fadeAnim);
        
        return true;
    }
    
    private void startState(TutorialState newState)
    {
        curState = newState;
        curAnimationId++;
    }
    
    private void startAnimationState(TutorialState newState, Runnable runnable)
    {
        curState = newState;
        curAnimationId++;
        curStateAnimationListener = new TutorialViewAnimationListener(curAnimationId,runnable);
    }

    private void onAnimationFinished(int animationId)
    {
        if(animationId!=curAnimationId)
        {
            return;
        }
        
        if(curState==TutorialState.Intro)
        {
            startState(TutorialState.IdleVisible);
            playMain();
            if(listener!=null)
            {
                listener.onTutorialAnimationFinished(this, TutorialState.Intro);
            }
        }
        else if(curState==TutorialState.Main)
        {
            startState(TutorialState.IdleVisible);
            if(listener!=null)
            {
                listener.onTutorialAnimationFinished(this, TutorialState.Main);
            }
            playMain();
        }
        else if(curState==TutorialState.Outro)
        {
            startState(TutorialState.IdleInvisible);
            if(listener!=null)
            {
                listener.onTutorialAnimationFinished(this, TutorialState.Outro);
            }
        }
    }
    

    private class TutorialViewAnimationListener implements AnimationListener
    {
        int startNum = 0;
        int finishNum=0;
        int animationId;
        Runnable runnable;
        
        public TutorialViewAnimationListener(int animationid, Runnable runnable)
        {
            this.animationId = animationid;
            this.runnable = runnable;
        }
        
        int getAnimationId()
        {
            return animationId;
        }
        
        @Override
        public void onAnimationStart(Animation animation)
        {
            startNum++;
        }

        @Override
        public void onAnimationEnd(Animation animation)
        {
            finishNum++;
            if(finishNum==startNum)
            {
                if(runnable!=null)
                {
                    runnable.run();
                }
                onAnimationFinished(animationId);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation)
        {
        }
    }


    @Override
    public void OnAnimationFinished(EditFavsTutorialAnimationView view)
    {
        animView.playAddFavAnimation();
    }
}
