/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Triggertrap Ltd
 * Author Neil Davies
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.triggertrap.seekarc

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.*

/**
 * SeekArc.java
 *
 *
 * This is a class that functions much like a SeekBar but
 * follows a circle path instead of a straight line.
 *
 * @author Neil Davies
 */
class SeekArc : View {

    /**
     * The Drawable for the seek arc thumbnail
     */
    private var mThumb: Drawable? = null

    /**
     * The Maximum value that this SeekArc can be set to
     */
    private var max = 100

    /**
     * The Current value that the SeekArc is set to
     */
    private var mProgress = 0

    /**
     * The width of the progress line for this SeekArc
     */
    private var mProgressWidth = 4

    /**
     * The Width of the background arc for the SeekArc
     */
    private var mArcWidth = 2

    /**
     * The Angle to start drawing this Arc from
     */
    private var mStartAngle = 0

    /**
     * The Angle through which to draw the arc (Max is 360)
     */
    private var mSweepAngle = 360

    /**
     * The rotation of the SeekArc- 0 is twelve o'clock
     */
    private var mRotation = 0

    /**
     * Give the SeekArc rounded edges
     */
    private var mRoundedEdges = false

    /**
     * Enable touch inside the SeekArc
     */
    private var mTouchInside = true

    /**
     * Will the progress increase clockwise or anti-clockwise
     */
    var isClockwise = true

    /**
     * is the control enabled/touchable
     */
    private var mEnabled = true

    // Internal variables
    private var mArcRadius = 0
    private var mProgressSweep = 0f
    private val mArcRect = RectF()
    private var mArcPaint: Paint? = null
    private var mProgressPaint: Paint? = null
    private var mTranslateX: Int = 0
    private var mTranslateY: Int = 0
    private var mThumbXPos: Int = 0
    private var mThumbYPos: Int = 0
    private var mTouchIgnoreRadius: Float = 0.toFloat()
    private var mOnSeekArcChangeListener: OnSeekArcChangeListener? = null

    var progress: Int
        get() = mProgress
        set(progress) = updateProgress(progress, false)

    var progressWidth: Int
        get() = mProgressWidth
        set(mProgressWidth) {
            this.mProgressWidth = mProgressWidth
            mProgressPaint!!.strokeWidth = mProgressWidth.toFloat()
        }

    var arcWidth: Int
        get() = mArcWidth
        set(mArcWidth) {
            this.mArcWidth = mArcWidth
            mArcPaint!!.strokeWidth = mArcWidth.toFloat()
        }

    var arcRotation: Int
        get() = mRotation
        set(mRotation) {
            this.mRotation = mRotation
            updateThumbPosition()
        }

    var startAngle: Int
        get() = mStartAngle
        set(mStartAngle) {
            this.mStartAngle = mStartAngle
            updateThumbPosition()
        }

    var sweepAngle: Int
        get() = mSweepAngle
        set(mSweepAngle) {
            this.mSweepAngle = mSweepAngle
            updateThumbPosition()
        }

    var progressColor: Int
        get() = mProgressPaint!!.color
        set(color) {
            mProgressPaint!!.color = color
            invalidate()
        }

    var arcColor: Int
        get() = mArcPaint!!.color
        set(color) {
            mArcPaint!!.color = color
            invalidate()
        }

    interface OnSeekArcChangeListener {

        /**
         * Notification that the progress level has changed. Clients can use the
         * fromUser parameter to distinguish user-initiated changes from those
         * that occurred programmatically.
         *
         * @param seekArc  The SeekArc whose progress has changed
         * @param progress The current progress level. This will be in the range
         * 0..max where max was set by
         * [SeekArc.max]. (The default value for
         * max is 100.)
         * @param fromUser True if the progress change was initiated by the user.
         */
        fun onProgressChanged(seekArc: SeekArc, progress: Int, fromUser: Boolean)

        /**
         * Notification that the user has started a touch gesture. Clients may
         * want to use this to disable advancing the seekbar.
         *
         * @param seekArc The SeekArc in which the touch gesture began
         */
        fun onStartTrackingTouch(seekArc: SeekArc)

        /**
         * Notification that the user has finished a touch gesture. Clients may
         * want to use this to re-enable advancing the seekarc.
         *
         * @param seekArc The SeekArc in which the touch gesture began
         */
        fun onStopTrackingTouch(seekArc: SeekArc)
    }

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, R.attr.seekArcStyle)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs, defStyle)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {

        Log.d(TAG, "Initialising SeekArc")
        val density = context.resources.displayMetrics.density

        // Defaults, may need to link this into theme settings
        var arcColor = ContextCompat.getColor(context, R.color.progress_gray)
        var progressColor = ContextCompat.getColor(context, R.color.default_blue_light)
        val thumbHalfHeight: Int
        val thumbHalfWidth: Int
        mThumb = ContextCompat.getDrawable(context, R.drawable.seek_arc_control_selector)
        // Convert progress width to pixels for current density
        mProgressWidth = (mProgressWidth * density).toInt()


        if (attrs != null) {
            // Attribute initialization
            val a = context.obtainStyledAttributes(
                attrs,
                R.styleable.SeekArc, defStyle, 0
            )

            val thumb = a.getDrawable(R.styleable.SeekArc_thumb)
            if (thumb != null) {
                mThumb = thumb
            }


            thumbHalfHeight = mThumb!!.intrinsicHeight / 2
            thumbHalfWidth = mThumb!!.intrinsicWidth / 2
            mThumb!!.setBounds(
                -thumbHalfWidth, -thumbHalfHeight, thumbHalfWidth,
                thumbHalfHeight
            )

            max = a.getInteger(R.styleable.SeekArc_max, max)
            mProgress = a.getInteger(R.styleable.SeekArc_progressValue, mProgress)
            mProgressWidth = a.getDimension(
                R.styleable.SeekArc_progressWidth, mProgressWidth.toFloat()
            ).toInt()
            mArcWidth = a.getDimension(
                R.styleable.SeekArc_arcWidth,
                mArcWidth.toFloat()
            ).toInt()
            mStartAngle = a.getInt(R.styleable.SeekArc_startAngle, mStartAngle)
            mSweepAngle = a.getInt(R.styleable.SeekArc_sweepAngle, mSweepAngle)
            mRotation = a.getInt(R.styleable.SeekArc_rotation, mRotation)
            mRoundedEdges = a.getBoolean(
                R.styleable.SeekArc_roundEdges,
                mRoundedEdges
            )
            mTouchInside = a.getBoolean(
                R.styleable.SeekArc_touchInside,
                mTouchInside
            )
            isClockwise = a.getBoolean(
                R.styleable.SeekArc_clockwise,
                isClockwise
            )
            mEnabled = a.getBoolean(R.styleable.SeekArc_enabled, mEnabled)

            arcColor = a.getColor(R.styleable.SeekArc_arcColor, arcColor)
            progressColor = a.getColor(
                R.styleable.SeekArc_progressColor,
                progressColor
            )

            a.recycle()
        }

        mProgress = min(mProgress, max)
        mProgress = max(mProgress, 0)

        mSweepAngle = min(mSweepAngle, 360)
        mSweepAngle = max(mSweepAngle, 0)

        mProgressSweep = mProgress.toFloat() / max * mSweepAngle

        mStartAngle = if (mStartAngle > 360) 0 else mStartAngle
        mStartAngle = max(mStartAngle, 0)

        mArcPaint = Paint()
        mArcPaint!!.color = arcColor
        mArcPaint!!.isAntiAlias = true
        mArcPaint!!.style = Paint.Style.STROKE
        mArcPaint!!.strokeWidth = mArcWidth.toFloat()
        //mArcPaint.setAlpha(45);

        mProgressPaint = Paint()
        mProgressPaint!!.color = progressColor
        mProgressPaint!!.isAntiAlias = true
        mProgressPaint!!.style = Paint.Style.STROKE
        mProgressPaint!!.strokeWidth = mProgressWidth.toFloat()

        if (mRoundedEdges) {
            mArcPaint!!.strokeCap = Paint.Cap.ROUND
            mProgressPaint!!.strokeCap = Paint.Cap.ROUND
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!isClockwise) {
            canvas.scale(-1f, 1f, mArcRect.centerX(), mArcRect.centerY())
        }

        // Draw the arcs
        // The initial rotational offset -90 means we start at 12 o'clock
        val mAngleOffset = -90
        val arcStart = mStartAngle + mAngleOffset + mRotation
        val arcSweep = mSweepAngle
        canvas.drawArc(mArcRect, arcStart.toFloat(), arcSweep.toFloat(), false, mArcPaint!!)
        canvas.drawArc(
            mArcRect, arcStart.toFloat(), mProgressSweep, false,
            mProgressPaint!!
        )

        if (mEnabled) {
            // Draw the thumb nail
            canvas.translate(
                (mTranslateX - mThumbXPos).toFloat(),
                (mTranslateY - mThumbYPos).toFloat()
            )
            mThumb!!.draw(canvas)
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val height = getDefaultSize(
            suggestedMinimumHeight,
            heightMeasureSpec
        )
        val width = getDefaultSize(
            suggestedMinimumWidth,
            widthMeasureSpec
        )
        val min = min(width, height)
        val top: Float
        val left: Float
        val arcDiameter: Int

        mTranslateX = (width * 0.5f).toInt()
        mTranslateY = (height * 0.5f).toInt()

        arcDiameter = min - paddingLeft
        mArcRadius = arcDiameter / 2
        top = (height / 2 - arcDiameter / 2).toFloat()
        left = (width / 2 - arcDiameter / 2).toFloat()
        mArcRect.set(left, top, left + arcDiameter, top + arcDiameter)

        val arcStart = mProgressSweep.toInt() + mStartAngle + mRotation + 90
        mThumbXPos = (mArcRadius * cos(Math.toRadians(arcStart.toDouble()))).toInt()
        mThumbYPos = (mArcRadius * sin(Math.toRadians(arcStart.toDouble()))).toInt()

        setTouchInSide(mTouchInside)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mEnabled) {
            this.parent.requestDisallowInterceptTouchEvent(true)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    onStartTrackingTouch()
                    updateOnTouch(event)
                }
                MotionEvent.ACTION_MOVE -> updateOnTouch(event)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    onStopTrackingTouch()
                    isPressed = false
                    this.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            return true
        }
        return false
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (mThumb != null && mThumb!!.isStateful) {
            val state = drawableState
            mThumb!!.state = state
        }
        invalidate()
    }

    private fun onStartTrackingTouch() {
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener!!.onStartTrackingTouch(this)
        }
    }

    private fun onStopTrackingTouch() {
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener!!.onStopTrackingTouch(this)
        }
    }

    private fun updateOnTouch(event: MotionEvent) {
        val ignoreTouch = ignoreTouch(event.x, event.y)
        if (ignoreTouch) {
            return
        }
        isPressed = true
        val mTouchAngle = getTouchDegrees(event.x, event.y)
        val progress = getProgressForAngle(mTouchAngle)
        onProgressRefresh(progress)
    }

    private fun ignoreTouch(xPos: Float, yPos: Float): Boolean {
        var ignore = false
        val x = xPos - mTranslateX
        val y = yPos - mTranslateY

        val touchRadius = sqrt((x * x + y * y).toDouble()).toFloat()
        if (touchRadius < mTouchIgnoreRadius) {
            ignore = true
        }
        return ignore
    }

    private fun getTouchDegrees(xPos: Float, yPos: Float): Double {
        var x = xPos - mTranslateX
        val y = yPos - mTranslateY
        //invert the x-coord if we are rotating anti-clockwise
        x = if (isClockwise) x else -x
        // convert to arc Angle
        var angle = Math.toDegrees(
            atan2(y.toDouble(), x.toDouble()) + Math.PI / 2 - Math.toRadians(mRotation.toDouble())
        )
        if (angle < 0) {
            angle += 360
        }
        angle -= mStartAngle.toDouble()
        return angle
    }

    private fun getProgressForAngle(angle: Double): Int {
        var touchProgress = (valuePerDegree() * angle).roundToLong().toInt()

        touchProgress = if (touchProgress < 0)
            INVALID_PROGRESS_VALUE
        else
            touchProgress
        touchProgress = if (touchProgress > max)
            INVALID_PROGRESS_VALUE
        else
            touchProgress
        return touchProgress
    }

    private fun valuePerDegree(): Float {
        return max.toFloat() / mSweepAngle
    }

    private fun onProgressRefresh(progress: Int) {
        updateProgress(progress, true)
    }

    private fun updateThumbPosition() {
        val thumbAngle =
            (mStartAngle.toFloat() + mProgressSweep + mRotation.toFloat() + 90f).toInt()
        mThumbXPos = (mArcRadius * cos(Math.toRadians(thumbAngle.toDouble()))).toInt()
        mThumbYPos = (mArcRadius * sin(Math.toRadians(thumbAngle.toDouble()))).toInt()
    }

    private fun updateProgress(progress: Int, fromUser: Boolean) {
        var progress = progress

        if (progress == INVALID_PROGRESS_VALUE) {
            return
        }

        progress = min(progress, max)
        progress = max(progress, 0)
        mProgress = progress

        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener!!
                .onProgressChanged(this, progress, fromUser)
        }

        mProgressSweep = progress.toFloat() / max * mSweepAngle

        updateThumbPosition()

        invalidate()
    }

    /**
     * Sets a listener to receive notifications of changes to the SeekArc's
     * progress level. Also provides notifications of when the user starts and
     * stops a touch gesture within the SeekArc.
     *
     * @param onSeekArcChangeListener The seek bar notification listener
     * @see SeekArc.OnSeekArcChangeListener
     */
    fun setOnSeekArcChangeListener(onSeekArcChangeListener: OnSeekArcChangeListener) {
        mOnSeekArcChangeListener = onSeekArcChangeListener
    }

    fun setRoundedEdges(isEnabled: Boolean) {
        mRoundedEdges = isEnabled
        if (mRoundedEdges) {
            mArcPaint!!.strokeCap = Paint.Cap.ROUND
            mProgressPaint!!.strokeCap = Paint.Cap.ROUND
        } else {
            mArcPaint!!.strokeCap = Paint.Cap.SQUARE
            mProgressPaint!!.strokeCap = Paint.Cap.SQUARE
        }
    }

    fun setTouchInSide(isEnabled: Boolean) {
        val thumbHalfHeight = mThumb!!.intrinsicHeight / 2
        val thumbHalfWidth = mThumb!!.intrinsicWidth / 2
        mTouchInside = isEnabled
        mTouchIgnoreRadius = if (mTouchInside) {
            mArcRadius.toFloat() / 4
        } else {
            // Don't use the exact radius makes interaction too tricky
            (mArcRadius - min(thumbHalfWidth, thumbHalfHeight)).toFloat()
        }
    }

    override fun isEnabled(): Boolean {
        return mEnabled
    }

    override fun setEnabled(enabled: Boolean) {
        this.mEnabled = enabled
    }

    companion object {
        private val TAG = SeekArc::class.java.simpleName
        private const val INVALID_PROGRESS_VALUE = -1
    }
}
