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
package org.fairphone.launcher.edgeswipe.edit;

import java.util.ArrayList;

import org.fairphone.launcher.ApplicationInfo;
import org.fairphone.launcher.R;
import org.fairphone.launcher.edgeswipe.ui.EditFavoritesGridView;
import org.fairphone.launcher.edgeswipe.ui.EditFavoritesGridView.OnEditFavouritesIconDraggedListener;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Edit favorites activity implements functionality to edit your favorite apps
 * that will appear with the edge swipe.
 */
public class EditFavoritesActivity extends Activity implements
		View.OnDragListener, DragDropItemLayoutListener {
	private static final String TAG = EditFavoritesActivity.class
			.getSimpleName();

	// This is used to differentiate a drag from the all apps to favorites
	// from a drag between two favorites to perform a swap
	public static final int SELECTED_APPS_DRAG = 0;
	public static final int ALL_APPS_DRAG = 1;

	private AllAppsListAdapter mAllAppsListAdapter;
	private ArrayList<ApplicationInfo> mAllApps;

	private ArrayList<RelativeLayout> mFavIcons;

	private ApplicationInfo[] mSelectedApps;

	private EditFavoritesGridView mAllAppsGridView;

	private int mDragOrigin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fp_edit_favorites);

		mAllApps = AppDiscoverer.getInstance().getPackages();

		mSelectedApps = FavoritesStorageHelper.loadSelectedApps(this, 4);

		mFavIcons = new ArrayList<RelativeLayout>();

		mFavIcons.add((RelativeLayout) findViewById(R.id.favouriteGroup1));
		mFavIcons.add((RelativeLayout) findViewById(R.id.favouriteGroup2));
		mFavIcons.add((RelativeLayout) findViewById(R.id.favouriteGroup3));
		mFavIcons.add((RelativeLayout) findViewById(R.id.favouriteGroup4));

		setupAllAppsList();

		setupSelectedAppsList();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	/**
	 * Setup the list with all the apps installed on the device.
	 */
	private void setupAllAppsList() {
		mAllAppsGridView = (EditFavoritesGridView) findViewById(R.id.allAppsGridView);

		mAllAppsListAdapter = new AllAppsListAdapter(this);

		mAllAppsListAdapter.setAllApps(mAllApps);

		mAllAppsGridView.setLongClickable(true);

		mAllAppsGridView
				.setOnItemLongClickListener(new OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View v, int position, long id) {
						startDraggingIcon(v, position);

						return true;
					}
				});

		mAllAppsGridView
				.setOnEditFavouritesIconDraggedListener(new OnEditFavouritesIconDraggedListener() {

					@Override
					public void OnEditFavouritesIconDragged(
							AdapterView<?> parent, View view, int position,
							long id) {
						startDraggingIcon(view, position);
					}
				});

		mAllAppsGridView.setAdapter(mAllAppsListAdapter);

		// set the drag listener to enable favorite icon removal
		View allAppsGroupView = findViewById(R.id.allAppsGroup);
		allAppsGroupView.setOnDragListener(new DropDragEventListener(this,
				mFavIcons, mSelectedApps, mAllApps, true));

	}

	private void startDraggingIcon(View view, int position) {
		// display a circle around the possible destinations
		toggleFavoriteCircleSelection(-1, true);

		View mainView = this.getWindow().getDecorView();
		ApplicationInfo applicationInfo = mAllApps.get(position);

		// set the item with the origin of the drag and the index of the dragged
		// view
		mDragOrigin = EditFavoritesActivity.ALL_APPS_DRAG;
		String selectedItem = serializeItem(mDragOrigin, position);
		ClipData.Item item = new ClipData.Item(selectedItem);
		ClipData dragData = ClipData.newPlainText(
				applicationInfo.getApplicationTitle(),
				applicationInfo.getApplicationTitle());
		dragData.addItem(item);

		mainView.startDrag(dragData,
				new IconDragShadowBuilder(view, new BitmapDrawable(
						getResources(), applicationInfo.iconBitmap)), view, 0);

		mainView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
	}

	/**
	 * Setup the list with the selected app list, which is the holder of the
	 * apps that has been selected to become the favorite apps.
	 */
	private void setupSelectedAppsList() {
		for (int i = 0; i < 4; i++) {
			setupFavoriteIcon(mFavIcons.get(i), mSelectedApps[i], i, false);

			// set the listeners
			// The last argument is set to false since this hasn't the ability
			// to remove icons, it only switches them
			mFavIcons.get(i).setOnDragListener(
					new DropDragEventListener(this, mFavIcons, mSelectedApps,
							mAllApps, false));
		}
	}

	@Override
	public void setupFavoriteIcon(RelativeLayout rla,
			ApplicationInfo applicationInfo, int idx, boolean performAnimation) {

		if (applicationInfo == null) {
			final View dragPlaceholderView = rla.getChildAt(0);
			final View iconView = rla.getChildAt(1);

			if (performAnimation) {
				startViewFadeOutFadeInAnimation(dragPlaceholderView, iconView,
						null);
			} else {
				dragPlaceholderView.setVisibility(View.VISIBLE);
				iconView.setVisibility(View.INVISIBLE);
			}

			rla.setOnLongClickListener(null);
			mSelectedApps[idx] = null;
		} else {
			final View dragPlaceholderView = rla.getChildAt(0);

			final TextView iconView = (TextView) rla.getChildAt(1);

//			Log.d(TAG, "Adding app : " + applicationInfo.getApplicationTitle());

			if (mSelectedApps[idx] == null) {
				updateFavoriteIcon(applicationInfo, iconView);
				if (performAnimation) {
					startViewFadeOutFadeInAnimation(iconView,
							dragPlaceholderView, null);
				} else {
					dragPlaceholderView.setVisibility(View.INVISIBLE);
					iconView.setVisibility(View.VISIBLE);
				}
			} else {
				if (performAnimation) {
					startViewFadeOutFadeInAnimation(null, iconView,
							applicationInfo);
				} else {
					updateFavoriteIcon(applicationInfo, iconView);
					dragPlaceholderView.setVisibility(View.INVISIBLE);
					iconView.setVisibility(View.VISIBLE);
				}
			}
			mSelectedApps[idx] = applicationInfo;

			// Set the listener
			// pass the main view and the instance setup the drag and visibility
			// of some views
			final View mainView = this.getWindow().getDecorView();
			rla.setOnLongClickListener(new IdLongClickListener(idx, mainView,
					this));
		}

		FavoritesStorageHelper.storeSelectedApps(this, mSelectedApps);
	}

	/**
	 * Update the icon and label of a favorite
	 * 
	 * @param applicationInfo
	 *            App information that contains the icon and label
	 * @param icon
	 *            the icon to update
	 */
	private void updateFavoriteIcon(ApplicationInfo applicationInfo,
			final TextView icon) {
		Drawable drawable = new BitmapDrawable(getResources(),
				applicationInfo.iconBitmap);

		Resources r = getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
				r.getDisplayMetrics());
		drawable.setBounds(0, 0, Math.round(px), Math.round(px));

		icon.setCompoundDrawables(null, drawable, null, null);
		icon.setText(applicationInfo.getApplicationTitle());
	}

	/**
	 * Performs the animation when replacing one favorite
	 * 
	 * @param viewToFadeIn
	 *            view that will appear. When null it means that we are swapping
	 *            two favorites
	 * @param viewToFadeOut
	 *            view that will disappear.
	 * @param applicationInfo
	 *            app info that is used to swap two favorites
	 */
	private void startViewFadeOutFadeInAnimation(final View viewToFadeIn,
			final View viewToFadeOut, final ApplicationInfo applicationInfo) {
		Animation fadeOutAnimation = AnimationUtils.loadAnimation(this,
				R.anim.fade_out_fast);
		fadeOutAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				viewToFadeOut.setVisibility(View.INVISIBLE);

				if (applicationInfo == null && viewToFadeIn != null) {
					if (viewToFadeIn.getVisibility() != View.VISIBLE) {
						viewToFadeIn.setVisibility(View.VISIBLE);
						Animation fadeInAnimation = AnimationUtils
								.loadAnimation(getApplicationContext(),
										R.anim.fade_in_fast);
						viewToFadeIn.startAnimation(fadeInAnimation);
					}
				} else if (applicationInfo != null) {
					// get the new icon
					updateFavoriteIcon(applicationInfo,
							(TextView) viewToFadeOut);

					viewToFadeOut.setVisibility(View.VISIBLE);
					Animation fadeInAnimation = AnimationUtils.loadAnimation(
							getApplicationContext(), R.anim.fade_in_fast);
					viewToFadeOut.startAnimation(fadeInAnimation);
				}
			}
		});
		if (viewToFadeOut.getVisibility() == View.VISIBLE) {
			viewToFadeOut.startAnimation(fadeOutAnimation);
		}
	}

	/**
	 * Capture the back button press, to make sure we save the selected apps
	 * before exiting.
	 */
	@Override
	public void onBackPressed() {

		FavoritesStorageHelper.storeSelectedApps(this, mSelectedApps);

		Intent intent = getIntent();
		setResult(RESULT_OK, intent);

		finish();
	}

	/**
	 * Serializes the item id and origin that is being dragged.
	 * 
	 * @param appOrigin
	 *            the origin: can be SELECT_APPS_DRAG or ALL_APPS_DRAG
	 * @param appIndex
	 *            the item index
	 * @return the serialized item info
	 */
	public static String serializeItem(int appOrigin, int appIndex) {
		String selectedItem = appOrigin + ";" + appIndex;
		return selectedItem;
	}

	/**
	 * Deserializes the item id and origin that is being dragged.
	 * 
	 * @param toDeserialize
	 *            string to deserialize
	 * @return an array containing {appOrign, appIndex}
	 */
	public static String[] deserializeItem(String toDeserialize) {
		String[] selectedItem = toDeserialize.split(";");
		return selectedItem;
	}

	class IdLongClickListener implements View.OnLongClickListener {
		private int mId;
		private View mMainView;
		private DragDropItemLayoutListener mListener;

		public IdLongClickListener(int id, View mainView,
				DragDropItemLayoutListener listener) {
			super();

			mId = id;
			mMainView = mainView;
			mListener = listener;
		}

		@Override
		public boolean onLongClick(View v) {
			// Show the zone where favorites can be removed
			mListener.showAllAppsRemoveZone();

			// display a circle around the possible destinations
			toggleFavoriteCircleSelection(-1, true);

			// set the drag info
			ApplicationInfo applicationInfo = mSelectedApps[mId];

			// set the item with the origin of the drag and the index of the
			// dragged view
			mDragOrigin = EditFavoritesActivity.SELECTED_APPS_DRAG;
			String selectedItem = serializeItem(mDragOrigin, mId);
			ClipData.Item item = new ClipData.Item(selectedItem);
			ClipData dragData = ClipData.newPlainText(
					applicationInfo.getApplicationTitle(),
					applicationInfo.getApplicationTitle());
			dragData.addItem(item);

			mMainView.startDrag(dragData, new IconDragShadowBuilder(v,
					new BitmapDrawable(getResources(),
							applicationInfo.iconBitmap)), v, 0);

			mMainView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

			return true;
		}
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		return false;
	}

	private static class DropDragEventListener implements View.OnDragListener {

		private ArrayList<RelativeLayout> mFavIcons;
		private ApplicationInfo[] mSelectedApps;
		private ArrayList<ApplicationInfo> mAllApps;
		private DragDropItemLayoutListener mListener;
		/**
		 * True means that a favorite will be removed from the list.
		 */
		private boolean mIsToRemove;

		public DropDragEventListener(DragDropItemLayoutListener listener,
				ArrayList<RelativeLayout> favIcons,
				ApplicationInfo[] mSelectedApps2,
				ArrayList<ApplicationInfo> allApps,
				boolean toDeleteFromFavorites) {
			mFavIcons = favIcons;
			mSelectedApps = mSelectedApps2;
			mAllApps = allApps;
			mListener = listener;
			mIsToRemove = toDeleteFromFavorites;
		}

		@Override
		public boolean onDrag(View v, DragEvent event) {
			// Defines a variable to store the action type for the incoming
			// event
			final int action = event.getAction();

			RelativeLayout rla = (RelativeLayout) v;

			// Handles each of the expected events
			switch (action) {

			case DragEvent.ACTION_DRAG_STARTED: {
				// Moving an icon to an occupied position replaces the
				// current one
				return true;
			}
			case DragEvent.ACTION_DRAG_ENTERED:
				// toggle the red glow when removing favorites
				int idx = mFavIcons.indexOf(rla);
				if (idx == -1) {
					mListener.toggleAllAppRemoveZoneRedGlow(event.getX(),
							event.getY());
				} else {
					mListener.hideAllAppsRemoveZoneRedGlow();
				}
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				mListener.hideAllAppsRemoveZoneRedGlow();
				break;
			case DragEvent.ACTION_DROP:
				int id = mFavIcons.indexOf(rla);

				// get the Item data
				// 0 is the origin
				// 1 is the index
				String[] clipItemData = EditFavoritesActivity
						.deserializeItem(event
								.getClipData()
								.getItemAt(
										event.getClipData().getItemCount() - 1)
								.getText().toString());

				int position = -1;
				ApplicationInfo info = null;

				// obtain the applicationInfo
				switch (Integer.parseInt(clipItemData[0])) {
				case EditFavoritesActivity.SELECTED_APPS_DRAG:
					position = Integer.parseInt(clipItemData[1]);

					// when not removing an icon swap is performed
					if (!mIsToRemove) {
						info = mSelectedApps[position];
						mListener.setupFavoriteIcon(mFavIcons.get(position),
								mSelectedApps[id], position, true);
					} else {
						// remove the favorite
						id = position;
						rla = mFavIcons.get(id);
					}
					break;
				case EditFavoritesActivity.ALL_APPS_DRAG:
					position = Integer.parseInt(clipItemData[1]);
					info = mAllApps.get(position);
					break;
				default:
					Log.e(TAG,
							"Unknown Icon Origin received by OnDragListener.");
					break;
				}

				// only setup the icon if a valid id is obtained
				if (id != -1) {
					mListener.setupFavoriteIcon(rla, info, id, true);
				}

				return true;
			case DragEvent.ACTION_DRAG_ENDED:
				// hide zone remove zone
				mListener.hideAllAppsRemoveZone();
				return true;
			default:
				Log.e(TAG, "Unknown action type received by OnDragListener.");
			}

			return false;
		}
	}

	private static class IconDragShadowBuilder extends View.DragShadowBuilder {

		private Drawable mIcon;

		public IconDragShadowBuilder(View v, Drawable icon) {
			super(v);

			// Creates a draggable image that will fill the Canvas provided by
			// the system.
			// shadow = new ColorDrawable(Color.LTGRAY);
			mIcon = icon;
		}

		@Override
		public void onProvideShadowMetrics(Point size, Point touch) {
			// Defines local variables
			int width;
			int height;

			// Sets the width of the shadow to half the width of the original
			// View
			width = 100;

			// Sets the height of the shadow to half the height of the original
			// View
			height = 100;

			// The drag shadow is a ColorDrawable. This sets its dimensions to
			// be the same as the
			// Canvas that the system will provide. As a result, the drag shadow
			// will fill the
			// Canvas.
			mIcon.setBounds(0, 0, width, height);

			// Sets the size parameter's width and height values. These get back
			// to the system
			// through the size parameter.
			size.set(width, height);

			// Sets the touch point's position to be in the middle of the drag
			// shadow
			touch.set(width / 2, height);
		}

		// Defines a callback that draws the drag shadow in a Canvas that the
		// system constructs
		// from the dimensions passed in onProvideShadowMetrics().
		@Override
		public void onDrawShadow(Canvas canvas) {

			// Draws the ColorDrawable in the Canvas passed in from the system.
			// shadow.draw(canvas);
			mIcon.draw(canvas);
		}

	}

	@Override
	public void hideAllAppsRemoveZone() {
		hideAllAppsRemoveZoneRedGlow();

		View removeZoneView = findViewById(R.id.allAppsTextView);
		removeZoneView.setVisibility(View.INVISIBLE);

		mAllAppsGridView.setAlpha(1f);

		// hide the circle around the possible destinations
		toggleFavoriteCircleSelection(-1, false);
	}

	@Override
	public void showAllAppsRemoveZone() {
		View removeZoneView = findViewById(R.id.allAppsTextView);
		removeZoneView.setVisibility(View.VISIBLE);

		mAllAppsGridView.setAlpha(0.2f);
	}

	@Override
	public void showAllAppsRemoveZoneRedGlow() {
		View allAppsGroupView = findViewById(R.id.allAppsGroup);
		allAppsGroupView.setBackground(getResources().getDrawable(R.drawable.fp_edit_favs_remove_red_bg));
	}

	@Override
	public void hideAllAppsRemoveZoneRedGlow() {
		View allAppsGroupView = findViewById(R.id.allAppsGroup);
		allAppsGroupView.setBackground(getResources().getDrawable(
				R.drawable.edit_menu_list_background));
	}

	@Override
	public void toggleAllAppRemoveZoneRedGlow(float pointerX, float pointerY) {
		// the red glow only makes sense when removing favorites
		if (mDragOrigin == SELECTED_APPS_DRAG) {
			if (isInRemoveZone(pointerX, pointerY)) {
				showAllAppsRemoveZoneRedGlow();
			} else {
				hideAllAppsRemoveZoneRedGlow();
			}
		}
	}

	private boolean isInRemoveZone(float pointerX, float pointerY) {

		boolean validX = false;
		boolean validY = false;
		View allAppsGroupView = findViewById(R.id.allAppsGroup);

		validX = pointerX <= (allAppsGroupView.getX() + allAppsGroupView
				.getWidth());
		validY = pointerY >= allAppsGroupView.getY();

		return validX && validY;
	}

	/**
	 * Display a circle around the favorite possible positions when configuring
	 * it
	 * 
	 * @param selectedFavorite
	 *            the favorite position that will not be circled. -1 means that
	 *            all circles will be shown.
	 * @param showCircle
	 *            true: displays a white circle; false: displays a faded white
	 *            circle
	 */
	private void toggleFavoriteCircleSelection(int selectedFavorite,
			boolean showCircle) {
		for (int i = 0; i < mFavIcons.size(); i++) {
			if (i != selectedFavorite && showCircle) {
				mFavIcons.get(i)
						.setBackground(
								getResources().getDrawable(
										R.drawable.edit_menu_circle));
			} else {
				mFavIcons.get(i).setBackground(
						getResources().getDrawable(
								R.drawable.edit_menu_circle_faded));
			}
		}
	}

}
