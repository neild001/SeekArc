/*******************************************************************************
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2013 Triggertrap Ltd
 * Author Neil Davies
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.triggertrap.seekarc;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * SeekArc.java
 * <p/>
 * This is a class that functions much like a SeekBar but
 * follows a circle path instead of a straight line.
 *
 * @author Neil Davies
 */
public class SeekArc extends View {

	private static final String TAG = SeekArc.class.getSimpleName();
	private static int INVALID_PROGRESS_VALUE = -1;
	private static final int MARKER_TEXT_SIZE = 18;
	private static final int TEXT_RADIUS = 40;
	private static final int MARKER_TOUCH_RADIUS = 48;
	private static final int CLICK_ACTION_THRESHOLD = 5;

	// The initial rotational offset -90 means we start at 12 o'clock
	private final int mAngleOffset = -90;

	/**
	 * The Drawable for the seek arc thumbnail
	 */
	private Drawable mThumb;

	/**
	 * The Drawable for the seek arc empty marker
	 */
	private Drawable mProgressMarkerEmpty;

	/**
	 * The Drawable for the seek arc fill marker
	 */
	private Drawable mProgressMarkerFill;

	/**
	 * The Maximum value that this SeekArc can be set to
	 */
	private int mMax = 100;

	/**
	 * The Current value that the SeekArc is set to
	 */
	private int mProgress = 0;

	/**
	 * The width of the progress line for this SeekArc
	 */
	private int mProgressWidth = 4;

	/**
	 * The Width of the background arc for the SeekArc
	 */
	private int mArcWidth = 2;

	/**
	 * The Angle to start drawing this Arc from
	 */
	private int mStartAngle = 0;

	/**
	 * The Angle through which to draw the arc (Max is 360)
	 */
	private int mSweepAngle = 360;

	/**
	 * The rotation of the SeekArc- 0 is twelve o'clock
	 */
	private int mRotation = 0;

	/**
	 * Give the SeekArc rounded edges
	 */
	private boolean mRoundedEdges = false;

	/**
	 * Enable touch inside the SeekArc
	 */
	private boolean mTouchInside = true;

	/**
	 * Will the progress increase clockwise or anti-clockwise
	 */
	private boolean mClockwise = true;

	/**
	 * is the control enabled/touchable
	 */
	private boolean mEnabled = true;

	/**
	 * The text color of the empty progress markers.
	 */
	private int mMarkerTextColorEmpty;

	/**
	 * The text color of the filled progress markers.
	 */
	private int mMarkerTextColorFill;

	/**
	 * The text size of the progress markers.
	 */
	private float mMarkerTextSize;

	/**
	 * The text radius of the progress markers. The longer the value, the longer the distance of the text from the marker.
	 */
	private int mMarkerTextRadius;

	// Internal variables
	private int mArcRadius = 0;
	private float mProgressSweep = 0;
	private RectF mArcRect = new RectF();
	private Paint mArcPaint;
	private Paint mProgressPaint;
	private int mTranslateX;
	private int mTranslateY;
	private int mThumbXPos;
	private int mThumbYPos;
	private double mTouchAngle;
	private float mTouchIgnoreRadius;
	private OnSeekArcChangeListener mOnSeekArcChangeListener;
	private ArrayList<Marker> mMarkers;
	private Paint mMarkerTextPaint;
	private float mClickStartX;
	private float mClickStartY;

	public interface OnSeekArcChangeListener {

		/**
		 * Notification that the progress level has changed. Clients can use the
		 * fromUser parameter to distinguish user-initiated changes from those
		 * that occurred programmatically.
		 *
		 * @param seekArc  The SeekArc whose progress has changed
		 * @param progress The current progress level. This will be in the range
		 *                 0..max where max was set by
		 *                 {@link SeekArc#setMax(int)}. (The default value for
		 *                 max is 100.)
		 * @param fromUser True if the progress change was initiated by the user.
		 */
		void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser);

		/**
		 * Notification that the user has started a touch gesture. Clients may
		 * want to use this to disable advancing the seekbar.
		 *
		 * @param seekArc The SeekArc in which the touch gesture began
		 */
		void onStartTrackingTouch(SeekArc seekArc);

		/**
		 * Notification that the user has finished a touch gesture. Clients may
		 * want to use this to re-enable advancing the seekarc.
		 *
		 * @param seekArc The SeekArc in which the touch gesture began
		 */
		void onStopTrackingTouch(SeekArc seekArc);
	}

	public interface OnMarkerClickListener {

		/**
		 * Notification that the user has clicked a marker.
		 *
		 * @param value The value of the marker
		 * @param label The label of the marker
		 */
		void onMarkerClicked(int value, String label);
	}

	public SeekArc(Context context) {
		super(context);
		init(context, null, 0);
	}

	public SeekArc(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, R.attr.seekArcStyle);
	}

	public SeekArc(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {

		Log.d(TAG, "Initialising SeekArc");
		final Resources res = getResources();
		float density = context.getResources().getDisplayMetrics().density;

		mClickStartX = 0;
		mClickStartY = 0;

		// Defaults, may need to link this into theme settings
		int arcColor = res.getColor(R.color.progress_gray);
		int progressColor = res.getColor(android.R.color.holo_blue_light);
		int thumbHalfheight = 0;
		int thumbHalfWidth = 0;
		mThumb = res.getDrawable(R.drawable.seek_arc_control_selector);
		mProgressMarkerEmpty = res.getDrawable(R.drawable.marker_empty);
		mProgressMarkerFill = res.getDrawable(R.drawable.marker_fill);
		// Convert progress width to pixels for current density
		mProgressWidth = (int) (mProgressWidth * density);
		mMarkers = new ArrayList<>();
		mMarkerTextPaint = new Paint();
		mMarkerTextPaint.setTextSize(MARKER_TEXT_SIZE);
		mMarkerTextPaint.setAntiAlias(true);
		mMarkerTextPaint.setTextAlign(Paint.Align.CENTER);

		if (attrs != null) {
			// Attribute initialization
			final TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.SeekArc, defStyle, 0);

			Drawable thumb = a.getDrawable(R.styleable.SeekArc_thumb);
			if (thumb != null) {
				mThumb = thumb;
			}

			thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
			thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
			mThumb.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth,
					thumbHalfheight);

			thumb = a.getDrawable(R.styleable.SeekArc_markerEmpty);
			if (thumb != null) {
				mProgressMarkerEmpty = thumb;
			}

			thumbHalfheight = (int) mProgressMarkerEmpty.getIntrinsicHeight() / 2;
			thumbHalfWidth = (int) mProgressMarkerEmpty.getIntrinsicWidth() / 2;
			mProgressMarkerEmpty.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth,
					thumbHalfheight);

			thumb = a.getDrawable(R.styleable.SeekArc_markerFill);
			if (thumb != null) {
				mProgressMarkerFill = thumb;
			}

			thumbHalfheight = (int) mProgressMarkerFill.getIntrinsicHeight() / 2;
			thumbHalfWidth = (int) mProgressMarkerFill.getIntrinsicWidth() / 2;
			mProgressMarkerFill.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth,
					thumbHalfheight);

			mMax = a.getInteger(R.styleable.SeekArc_max, mMax);
			mProgress = a.getInteger(R.styleable.SeekArc_progress, mProgress);
			mProgressWidth = (int) a.getDimension(
					R.styleable.SeekArc_progressWidth, mProgressWidth);
			mArcWidth = (int) a.getDimension(R.styleable.SeekArc_arcWidth,
					mArcWidth);
			mStartAngle = a.getInt(R.styleable.SeekArc_startAngle, mStartAngle);
			mSweepAngle = a.getInt(R.styleable.SeekArc_sweepAngle, mSweepAngle);
			mRotation = a.getInt(R.styleable.SeekArc_rotation, mRotation);
			mRoundedEdges = a.getBoolean(R.styleable.SeekArc_roundEdges,
					mRoundedEdges);
			mTouchInside = a.getBoolean(R.styleable.SeekArc_touchInside,
					mTouchInside);
			mClockwise = a.getBoolean(R.styleable.SeekArc_clockwise,
					mClockwise);
			mEnabled = a.getBoolean(R.styleable.SeekArc_enabled, mEnabled);
			arcColor = a.getColor(R.styleable.SeekArc_arcColor, arcColor);
			progressColor = a.getColor(R.styleable.SeekArc_progressColor,
					progressColor);

			mMarkerTextColorEmpty = a.getColor(R.styleable.SeekArc_markerTextEmptyColor, arcColor);
			mMarkerTextColorFill = a.getColor(R.styleable.SeekArc_markerTextFillColor, progressColor);
			mMarkerTextSize = (int) a.getDimension(R.styleable.SeekArc_markerTextSize, MARKER_TEXT_SIZE);
			mMarkerTextRadius = (int) a.getDimension(R.styleable.SeekArc_markerTextRadius, TEXT_RADIUS);

			a.recycle();
		}

		mProgress = (mProgress > mMax) ? mMax : mProgress;
		mProgress = (mProgress < 0) ? 0 : mProgress;

		mSweepAngle = (mSweepAngle > 360) ? 360 : mSweepAngle;
		mSweepAngle = (mSweepAngle < 0) ? 0 : mSweepAngle;

		mStartAngle = (mStartAngle > 360) ? 0 : mStartAngle;
		mStartAngle = (mStartAngle < 0) ? 0 : mStartAngle;

		mArcPaint = new Paint();
		mArcPaint.setColor(arcColor);
		mArcPaint.setAntiAlias(true);
		mArcPaint.setStyle(Paint.Style.STROKE);
		mArcPaint.setStrokeWidth(mArcWidth);
		//mArcPaint.setAlpha(45);

		mProgressPaint = new Paint();
		mProgressPaint.setColor(progressColor);
		mProgressPaint.setAntiAlias(true);
		mProgressPaint.setStyle(Paint.Style.STROKE);
		mProgressPaint.setStrokeWidth(mProgressWidth);

		if (mRoundedEdges) {
			mArcPaint.setStrokeCap(Paint.Cap.ROUND);
			mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (!mClockwise) {
			canvas.scale(-1, 1, mArcRect.centerX(), mArcRect.centerY());
		}

		// Draw the arcs
		final int arcStart = mStartAngle + mAngleOffset + mRotation;
		final int arcSweep = mSweepAngle;
		canvas.drawArc(mArcRect, arcStart, arcSweep, false, mArcPaint);
		canvas.drawArc(mArcRect, arcStart, mProgressSweep, false, mProgressPaint);

		if (mEnabled) {
			// Draw the thumb nail
			canvas.save();
			canvas.translate(mTranslateX - mThumbXPos, mTranslateY - mThumbYPos);
			mThumb.draw(canvas);
			canvas.restore();
		}

		// Draw the markers
		if (mMarkers != null && !mMarkers.isEmpty()) {
			mMarkerTextPaint.setTextSize(mMarkerTextSize);
			for (Marker marker : mMarkers) {
				canvas.save();
				canvas.translate(mTranslateX - marker.posX, mTranslateY - marker.posY);
				if (mProgress < marker.value) {
					mProgressMarkerEmpty.draw(canvas);
					mMarkerTextPaint.setColor(mMarkerTextColorEmpty);
				} else {
					mProgressMarkerFill.draw(canvas);
					mMarkerTextPaint.setColor(mMarkerTextColorFill);
				}
				canvas.restore();

				//draw the text of the marker, depending on the angle
				canvas.save();
				float textPosX = mTranslateX - marker.textPosX - marker.offsetX;
				float textPosY = mTranslateY - marker.textPosY - marker.offsetY;
				canvas.translate(textPosX, textPosY);
				canvas.drawText(marker.getLabel(), 0, 0, mMarkerTextPaint);
				canvas.restore();
			}
		}
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		if (mMarkers != null && !mMarkers.isEmpty()) {
			onMeasureWithMarkers(widthMeasureSpec, heightMeasureSpec);
			return;
		}

		final int height = getDefaultSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		final int width = getDefaultSize(getSuggestedMinimumWidth(),
				widthMeasureSpec);
		final int min = Math.min(width, height);
		float top = 0;
		float left = 0;
		int arcDiameter = 0;

		mTranslateX = (int) (width * 0.5f);
		mTranslateY = (int) (height * 0.5f);

		arcDiameter = min - getPaddingLeft();
		mArcRadius = arcDiameter / 2;
		top = height / 2 - (arcDiameter / 2);
		left = width / 2 - (arcDiameter / 2);
		mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);

		int arcStart = (int) mProgressSweep + mStartAngle + mRotation + 90;
		mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(arcStart)));
		mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(arcStart)));

		setTouchInSide(mTouchInside);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	private void onMeasureWithMarkers(int widthMeasureSpec, int heightMeasureSpec) {
		final int height = getDefaultSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		final int width = getDefaultSize(getSuggestedMinimumWidth(),
				widthMeasureSpec);
		final int min = Math.min(width, height);
		float top = 0;
		float left = 0;
		int arcDiameter = 0;

		mTranslateX = (int) (min * 0.5f);
		mTranslateY = (int) (min * 0.5f);

		arcDiameter = min - getPaddingLeft() - 2 * mMarkerTextRadius;
		mArcRadius = arcDiameter / 2;
		top = min / 2 - (arcDiameter / 2);
		left = min / 2 - (arcDiameter / 2);
		mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);

		int arcStart = (int) mProgressSweep + mStartAngle + mRotation + 90;
		mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(arcStart)));
		mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(arcStart)));

		updateMarkerPositions();

		setTouchInSide(mTouchInside);
		setMeasuredDimension(MeasureSpec.makeMeasureSpec(min, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(min, MeasureSpec.EXACTLY));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				startClick(event.getX(), event.getY());
				if (mEnabled) {
					onStartTrackingTouch();
					updateOnTouch(event);
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (mEnabled) {
					updateOnTouch(event);
				}
				break;
			case MotionEvent.ACTION_UP:
				endClick(event.getX(), event.getY());
				if (mEnabled) {
					onStopTrackingTouch();
					setPressed(false);
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				if (mEnabled) {
					onStopTrackingTouch();
					setPressed(false);
				}
				break;
		}

		return true;
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mThumb != null && mThumb.isStateful()) {
			int[] state = getDrawableState();
			mThumb.setState(state);
		}
		invalidate();
	}

	private void startClick(float posX, float posY) {
		mClickStartX = posX;
		mClickStartY = posY;
		setPressed(true);
	}

	private void endClick(float posX, float posY) {
		setPressed(false);
		if (mMarkers == null || !isAClick(mClickStartX, posX, mClickStartY, posY)) {
			return;
		}
		float minimumDist = Float.MAX_VALUE;
		Marker markerToClick = null;
		for (Marker marker : mMarkers) {
			if (marker.onMarkerClickListener == null) {
				continue;
			}
			float dist = getEuclideanDistance(mTranslateX - marker.posX, mTranslateY - marker.posY, posX, posY);
			if (dist > MARKER_TOUCH_RADIUS || dist > minimumDist) {
				continue;
			}
			minimumDist = dist;
			markerToClick = marker;
		}
		if (markerToClick != null) {
			markerToClick.onMarkerClickListener.onMarkerClicked(markerToClick.value, markerToClick.getLabel());
		}
	}

	private void onStartTrackingTouch() {
		if (mOnSeekArcChangeListener != null) {
			mOnSeekArcChangeListener.onStartTrackingTouch(this);
		}
	}

	private void onStopTrackingTouch() {
		if (mOnSeekArcChangeListener != null) {
			mOnSeekArcChangeListener.onStopTrackingTouch(this);
		}
	}

	private void updateOnTouch(MotionEvent event) {
		boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
		if (ignoreTouch) {
			return;
		}
		setPressed(true);
		mTouchAngle = getTouchDegrees(event.getX(), event.getY());
		int progress = getProgressForAngle(mTouchAngle);
		onProgressRefresh(progress, true);
	}

	private boolean ignoreTouch(float xPos, float yPos) {
		boolean ignore = false;
		float x = xPos - mTranslateX;
		float y = yPos - mTranslateY;

		float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
		if (touchRadius < mTouchIgnoreRadius) {
			ignore = true;
		}
		return ignore;
	}

	private double getTouchDegrees(float xPos, float yPos) {
		float x = xPos - mTranslateX;
		float y = yPos - mTranslateY;
		//invert the x-coord if we are rotating anti-clockwise
		x = (mClockwise) ? x : -x;
		// convert to arc Angle
		double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2)
				- Math.toRadians(mRotation));
		if (angle < 0) {
			angle = 360 + angle;
		}
		angle -= mStartAngle;
		return angle;
	}

	private int getProgressForAngle(double angle) {
		int touchProgress = (int) Math.round(valuePerDegree() * angle);

		touchProgress = (touchProgress < 0) ? INVALID_PROGRESS_VALUE
				: touchProgress;
		touchProgress = (touchProgress > mMax) ? INVALID_PROGRESS_VALUE
				: touchProgress;
		return touchProgress;
	}

	private float valuePerDegree() {
		return (float) mMax / mSweepAngle;
	}

	private void onProgressRefresh(int progress, boolean fromUser) {
		updateProgress(progress, fromUser);
	}

	private void updateThumbPosition() {
		int thumbAngle = (int) (mStartAngle + mProgressSweep + mRotation + 90);
		mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(thumbAngle)));
		mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(thumbAngle)));
		updateMarkerPositions();
	}

	private void updateMarkerPositions() {
		if (mMarkers == null) {
			mMarkers = new ArrayList<>();
			return;
		}
		if (mMarkers.isEmpty()) {
			return;
		}
		int angle = 0;
		for (Marker marker : mMarkers) {
			angle = (int) (mStartAngle + marker.progressSweep + mRotation + 90);
			marker.posX = (int) (mArcRadius * Math.cos(Math.toRadians(angle)));
			marker.posY = (int) (mArcRadius * Math.sin(Math.toRadians(angle)));
			marker.textPosX = (int) ((mArcRadius + TEXT_RADIUS) * Math.cos(Math.toRadians(angle)));
			marker.textPosY = (int) ((mArcRadius + TEXT_RADIUS) * Math.sin(Math.toRadians(angle)));
		}
	}

	private void updateProgress(int progress, boolean fromUser) {

		if (progress == INVALID_PROGRESS_VALUE) {
			return;
		}

		if (mOnSeekArcChangeListener != null) {
			mOnSeekArcChangeListener
					.onProgressChanged(this, progress, fromUser);
		}

		progress = (progress > mMax) ? mMax : progress;
		progress = (mProgress < 0) ? 0 : progress;

		mProgress = progress;
		mProgressSweep = (float) progress / mMax * mSweepAngle;

		updateThumbPosition();

		invalidate();
	}

	/**
	 * Sets a listener to receive notifications of changes to the SeekArc's
	 * progress level. Also provides notifications of when the user starts and
	 * stops a touch gesture within the SeekArc.
	 *
	 * @param l The seek bar notification listener
	 * @see SeekArc.OnSeekArcChangeListener
	 */
	public void setOnSeekArcChangeListener(OnSeekArcChangeListener l) {
		mOnSeekArcChangeListener = l;
	}

	public void setProgress(int progress) {
		updateProgress(progress, false);
	}

	public int getProgressWidth() {
		return mProgressWidth;
	}

	public void setProgressWidth(int mProgressWidth) {
		this.mProgressWidth = mProgressWidth;
		mProgressPaint.setStrokeWidth(mProgressWidth);
	}

	public int getArcWidth() {
		return mArcWidth;
	}

	public void setArcWidth(int mArcWidth) {
		this.mArcWidth = mArcWidth;
		mArcPaint.setStrokeWidth(mArcWidth);
	}

	public int getArcRotation() {
		return mRotation;
	}

	public void setArcRotation(int mRotation) {
		this.mRotation = mRotation;
		updateThumbPosition();
	}

	public int getStartAngle() {
		return mStartAngle;
	}

	public void setStartAngle(int mStartAngle) {
		this.mStartAngle = mStartAngle;
		updateThumbPosition();
	}

	public int getSweepAngle() {
		return mSweepAngle;
	}

	public void setSweepAngle(int mSweepAngle) {
		this.mSweepAngle = mSweepAngle;
		updateThumbPosition();
	}

	public void setRoundedEdges(boolean isEnabled) {
		mRoundedEdges = isEnabled;
		if (mRoundedEdges) {
			mArcPaint.setStrokeCap(Paint.Cap.ROUND);
			mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
		} else {
			mArcPaint.setStrokeCap(Paint.Cap.SQUARE);
			mProgressPaint.setStrokeCap(Paint.Cap.SQUARE);
		}
	}

	public void setTouchInSide(boolean isEnabled) {
		int thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
		int thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
		mTouchInside = isEnabled;
		if (mTouchInside) {
			mTouchIgnoreRadius = (float) mArcRadius / 4;
		} else {
			// Don't use the exact radius makes interaction too tricky
			mTouchIgnoreRadius = mArcRadius
					- Math.min(thumbHalfWidth, thumbHalfheight);
		}
	}

	public void setClockwise(boolean isClockwise) {
		mClockwise = isClockwise;
	}

	public boolean isClockwise() {
		return mClockwise;
	}

	public boolean isEnabled() {
		return mEnabled;
	}

	public void setEnabled(boolean enabled) {
		this.mEnabled = enabled;
	}

	public int getProgressColor() {
		return mProgressPaint.getColor();
	}

	public void setProgressColor(int color) {
		mProgressPaint.setColor(color);
		invalidate();
	}

	public int getArcColor() {
		return mArcPaint.getColor();
	}

	public void setArcColor(int color) {
		mArcPaint.setColor(color);
		invalidate();
	}

	public int getMax() {
		return mMax;
	}

	public void setMax(int mMax) {
		this.mMax = mMax;
	}

	public int getMarkerTextColorEmpty() {
		return mMarkerTextColorEmpty;
	}

	public void setMarkerTextColorEmpty(int color) {
		mMarkerTextColorEmpty = color;
		invalidate();
	}

	public int getMarkerTextColorFill() {
		return mMarkerTextColorFill;
	}

	public void setMarkerTextColorFill(int color) {
		mMarkerTextColorFill = color;
		invalidate();
	}

	public void setTextSize(float size) {
		mMarkerTextSize = size;
		invalidate();
	}

	public float getTextSize() {
		return mMarkerTextSize;
	}

	public void setTextRadius(int textRadius) {
		if (textRadius > 0) {
			mMarkerTextRadius = textRadius;
			invalidate();
		}
	}

	public int getTextRadius() {
		return mMarkerTextRadius;
	}

	/**
	 * Adds a marker on the SeekArc. If parameter label is provided (not null), then it will be shown as label on the marker,
	 * else the value will be shown as label.
	 *
	 * @param label                 The label of the marker.
	 * @param value                 The progress value of the marker
	 * @param textOffsetX           The X offset of the label
	 * @param textOffsetY           The Y offset of the label
	 * @param onMarkerClickListener The listener that corresponds to the click events of the marker. It can be null.
	 * @return True if the marker was added successfully, false otherwise.
	 */
	public boolean addMarker(String label, int value, int textOffsetX, int textOffsetY, OnMarkerClickListener onMarkerClickListener) {
		if (value > mMax || value < 0) {
			return false;
		}
		Marker marker = new Marker();
		String lb = label;
		if (label != null && label.length() > 6) {
			lb = label.substring(0, 6);
		}
		marker.label = lb;
		marker.value = value;
		marker.offsetX = textOffsetX;
		marker.offsetY = textOffsetY;
		marker.progressSweep = (float) value / mMax * mSweepAngle;
		marker.onMarkerClickListener = onMarkerClickListener;
		if (mMarkers == null) {
			mMarkers = new ArrayList<>();
		}
		mMarkers.add(marker);
		requestLayout();
		postInvalidate();
		return true;
	}

	private float getEuclideanDistance(float pos1X, float pos1Y, float pos2X, float pos2Y) {
		float x = pos2X - pos1X;
		float y = pos2Y - pos1Y;
		return (float) Math.sqrt(((x * x) + (y * y)));
	}

	private boolean isAClick(float startX, float endX, float startY, float endY) {
		float difference = getEuclideanDistance(startX, startY, endX, endY);
		return difference <= CLICK_ACTION_THRESHOLD;
	}

	private class Marker {
		int posX;
		int posY;
		int textPosX;
		int textPosY;
		int offsetX;
		int offsetY;
		String label;
		int value;
		float progressSweep;
		Rect textBounds;
		OnMarkerClickListener onMarkerClickListener;

		Marker() {
			posX = 0;
			posY = 0;
			textPosX = 0;
			textPosY = 0;
			offsetX = 0;
			offsetY = 0;
			value = 0;
			label = null;
			progressSweep = 0;
			textBounds = new Rect();
			onMarkerClickListener = null;
		}

		String getLabel() {
			String result = String.valueOf(value);
			if (label != null) {
				result = label;
			}
			mMarkerTextPaint.getTextBounds(result, 0, result.length(), textBounds);
			return result;
		}
	}
}