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
package org.fairphone.peaceofmind.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class VerticalSeekBar extends SeekBar {

	private VerticalScrollListener _listener;

	public VerticalSeekBar(Context context) {
		super(context);
	}

	public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public VerticalSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setPeaceListener(VerticalScrollListener listener) {
		_listener = listener;
	}

	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(h, w, oldh, oldw);
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec,
			int heightMeasureSpec) {
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	protected void onDraw(Canvas c) {
		c.rotate(90);
		c.translate(0, -getWidth());

		super.onDraw(c);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled()) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			updateScroll(event);
			break;
		case MotionEvent.ACTION_MOVE:
			updateScroll(event);
			break;
		case MotionEvent.ACTION_UP:
			updateScroll(event);
			performScrollEnded();
			break;

		case MotionEvent.ACTION_CANCEL:
			break;
		}
		return true;
	}
	
	public void setInvertedProgress(int y){
		int i = 0;
		i = getMax() - (int) (getMax() * y / getHeight());
		setProgress(100 - i);
		onSizeChanged(getWidth(), getHeight(), 0, 0);
	}

	private void updateScroll(MotionEvent event) {
		int i = 0;
		i = getMax() - (int) (getMax() * event.getY() / getHeight());
		setProgress(100 - i);
		onSizeChanged(getWidth(), getHeight(), 0, 0);
		
		if (_listener != null) {
			_listener.updateBarScroll(getProgress());
		}
	}

	private void performScrollEnded() {
		float progress = getProgress() / 100.0f;

		if (_listener != null) {
			_listener.scrollEnded(progress);
		}
	}
}