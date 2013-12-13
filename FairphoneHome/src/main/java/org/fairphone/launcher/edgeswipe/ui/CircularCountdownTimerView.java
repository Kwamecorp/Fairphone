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
package org.fairphone.launcher.edgeswipe.ui;

import org.fairphone.launcher.R;
import org.fairphone.launcher.util.KWMathUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class CircularCountdownTimerView extends View {
	public interface CircularCountdownListener {
		public void onCountdownFinished(CircularCountdownTimerView countdownView);
	}

	private Drawable timerDrawable;
	private RectF clipBounds = new RectF();
	private RectF viewBounds = new RectF();
	private Rect viewBoundsI = new Rect();
	private Paint clipPaint;
	private Paint sourcePaint;

	private Bitmap viewBitmap;
	private BitmapShader timerShader;

	private float curFillRatio = 1.0f;
	private float startFillRatio = 1.0f;
	private float endFillRatio = 0.0f;
	private long animationDuration = 0;
	private long animationDelay = 0;
	private long curAnimationTime = 0;
	private long prevFrame = 0;
	private boolean isAnimating = false;

	private int prevWidth = 0;
	private int prevHeight = 0;

	CircularCountdownListener listener;

	public CircularCountdownTimerView(Context context) {
		super(context);
		init();
	}

	public CircularCountdownTimerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CircularCountdownTimerView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void startCountdownAnimation(long duration) {
		startCountdownAnimation(duration, 0);
	}

	public void startCountdownAnimation(long duration, long delay) {
		prevFrame = System.currentTimeMillis();
		animationDelay = delay;
		startFillRatio = 1.0f;
		curFillRatio = 1.0f;
		endFillRatio = 0.0f;
		isAnimating = true;
		curAnimationTime = 0;
		animationDuration = duration;

		postInvalidate();
	}

	public boolean isCountingDown() {
		return isAnimating;
	}

	public boolean isFinished() {
		return curFillRatio == 0.0f;
	}

	public float getCountdownRatio() {
		return curFillRatio;
	}

	public void pauseCountdownAnimation() {
		isAnimating = false;
		postInvalidate();
	}

	public void resumeCountdownAnimation() {
		isAnimating = true;
		prevFrame = System.currentTimeMillis();
		postInvalidate();
	}

	public void cancelCountdownAnimation() {
		alpha = 0;
		isAnimating = false;
		curFillRatio = 1.0f;
		curAnimationTime = 0;
		postInvalidate();
	}

	public void finishCountdownAnimation() {
		alpha = 0;
		postInvalidate();
		curAnimationTime = animationDuration;
		curFillRatio = 0.0f;
	}

	public void forceCountdownBegin() {
		alpha = 0;
		curFillRatio = 1.0f;
		isAnimating = false;
		postInvalidate();
	}

	public void forceCountdownEnd() {
		alpha = 0;
		curFillRatio = 0.0f;
		isAnimating = false;
		postInvalidate();
	}

	public CircularCountdownListener getCircularCountdownListener() {
		return listener;
	}

	public void setCircularCountdownListener(CircularCountdownListener listener) {
		this.listener = listener;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		clipPaint.setShader(null);
		timerShader = null;
		viewBitmap.recycle();
		viewBitmap = null;
		super.onDetachedFromWindow();
	}

	private void init() {
		timerDrawable = getContext().getResources().getDrawable(
				R.drawable.fp_fav_icon_ring);

		clipPaint = new Paint();
		clipPaint.setColor(0xFFFFFFFF);
		sourcePaint = new Paint();
		sourcePaint.setColor(0xFFFFFFFF);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		float w = getWidth();
		float h = getHeight();
		float w2 = w / 2;
		float h2 = h / 2;
		timerDrawable.setBounds(0, 0, (int) w, (int) h);

		float radius = (float) Math.sqrt(w2 * w2 + h2 * h2);
		clipBounds.set(w2 - radius, h2 - radius, w2 + radius, h2 + radius);
		viewBounds.set(0, 0, (int) w, (int) h);
		viewBoundsI.set(0, 0, (int) w, (int) h);

		if (getWidth() != prevWidth || getHeight() != prevHeight
				|| viewBitmap == null) {
			if (viewBitmap != null) {
				viewBitmap.recycle();
			}

			viewBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
					Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(viewBitmap);
			// canvas.drawBitmap(sourceBitmap, auxRectI, viewBoundsI,
			// sourcePaint);
			timerDrawable.setBounds(viewBoundsI);
			timerDrawable.draw(canvas);

			timerShader = new BitmapShader(viewBitmap, Shader.TileMode.CLAMP,
					Shader.TileMode.CLAMP);
			clipPaint.setShader(timerShader);
		}

		prevWidth = getWidth();
		prevHeight = getHeight();
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		updateAnimation();

		if (curAnimationTime - animationDelay > 0) {
			alpha += 0.03f;
			
			if(alpha > 1.0f){
				alpha = 1.0f;
			}
			
			
		}
		
		setAlpha(alpha);
		
		if (timerShader != null) {
			canvas.drawArc(viewBounds, 0, 360.f * curFillRatio, true,
					clipPaint);
		}
	}
	float alpha = 0;
	
	private void updateAnimation() {
		long curTime = System.currentTimeMillis();
		long dt = curTime - prevFrame;
		if (dt > 500) {
			dt = 1000 / 60;
		}
		if (isAnimating) {
			curAnimationTime += dt;
			boolean isFinished = false;
			long curAnimTime = Math.max(curAnimationTime - animationDelay, 0);
			if (curAnimTime >= animationDuration) {
				curAnimTime = animationDuration;
				curAnimTime = animationDuration + animationDelay;
				isFinished = true;
				isAnimating = false;
				alpha = 0;
			}

			float timeRatio = KWMathUtils.getLongRatio(0, animationDuration,
					curAnimTime);
			curFillRatio = KWMathUtils.blend(startFillRatio, endFillRatio,
					timeRatio);

			invalidate();
			if (isFinished) {
				alpha = 0;
				if (listener != null) {
					listener.onCountdownFinished(this);
				}
			}
		}
		prevFrame = curTime;
	}
}
