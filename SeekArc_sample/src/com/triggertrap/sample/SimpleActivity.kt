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
package com.triggertrap.sample

import android.app.Activity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.triggertrap.seekarc.SeekArc
import com.triggertrap.seekarc.SeekArc.OnSeekArcChangeListener
import kotlinx.android.synthetic.main.controls.*
import kotlinx.android.synthetic.main.custom_sample.*

/**
 * SimpleActivity.java
 *
 * @author Neil Davies
 */
open class SimpleActivity : Activity() {

    private var mSeekArc: SeekArc? = null
    private var mSeekArcProgress: TextView? = null

    open fun getLayoutFile(): Int {
        return R.layout.holo_sample
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutFile())

        mSeekArc = seekArc
        mSeekArcProgress = seekArcProgress
        val mRotation = rotation
        val mStartAngle = startAngle
        val mSweepAngle = sweepAngle
        val mArcWidth = arcWidth
        val mProgressWidth = progressWidth
        val mRoundedEdges = roundedEdges
        val mTouchInside = touchInside
        val mClockwise = clockwise
        val mEnabled = enabled

        mRotation.progress = mSeekArc!!.arcRotation
        mStartAngle.progress = mSeekArc!!.startAngle
        mSweepAngle.progress = mSeekArc!!.sweepAngle
        mArcWidth.progress = mSeekArc!!.arcWidth
        mProgressWidth.progress = mSeekArc!!.progressWidth

        mSeekArc!!.setOnSeekArcChangeListener(object : OnSeekArcChangeListener {

            override fun onStopTrackingTouch(seekArc: SeekArc) {}

            override fun onStartTrackingTouch(seekArc: SeekArc) {}

            override fun onProgressChanged(
                seekArc: SeekArc, progress: Int,
                fromUser: Boolean
            ) {
                mSeekArcProgress!!.text = progress.toString()
            }
        })

        mRotation.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(arg0: SeekBar) {

            }

            override fun onStartTrackingTouch(arg0: SeekBar) {}

            override fun onProgressChanged(view: SeekBar, progress: Int, fromUser: Boolean) {
                mSeekArc!!.arcRotation = progress
                mSeekArc!!.invalidate()
            }
        })

        mStartAngle.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(arg0: SeekBar) {

            }

            override fun onStartTrackingTouch(arg0: SeekBar) {}

            override fun onProgressChanged(view: SeekBar, progress: Int, fromUser: Boolean) {
                mSeekArc!!.startAngle = progress
                mSeekArc!!.invalidate()
            }
        })

        mSweepAngle.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(arg0: SeekBar) {

            }

            override fun onStartTrackingTouch(arg0: SeekBar) {}

            override fun onProgressChanged(view: SeekBar, progress: Int, fromUser: Boolean) {
                mSeekArc!!.sweepAngle = progress
                mSeekArc!!.invalidate()
            }
        })

        mArcWidth.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(arg0: SeekBar) {

            }

            override fun onStartTrackingTouch(arg0: SeekBar) {}

            override fun onProgressChanged(view: SeekBar, progress: Int, fromUser: Boolean) {
                mSeekArc!!.arcWidth = progress
                mSeekArc!!.invalidate()
            }
        })

        mProgressWidth.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(arg0: SeekBar) {

            }

            override fun onStartTrackingTouch(arg0: SeekBar) {}

            override fun onProgressChanged(view: SeekBar, progress: Int, fromUser: Boolean) {
                mSeekArc!!.progressWidth = progress
                mSeekArc!!.invalidate()
            }
        })

        mRoundedEdges.setOnCheckedChangeListener { _, isChecked ->
            mSeekArc!!.setRoundedEdges(isChecked)
            mSeekArc!!.invalidate()
        }

        mTouchInside.setOnCheckedChangeListener { _, isChecked ->
            mSeekArc!!.setTouchInSide(
                isChecked
            )
        }

        mClockwise.setOnCheckedChangeListener { _, isChecked ->
            mSeekArc!!.isClockwise = isChecked
            mSeekArc!!.invalidate()
        }

        mEnabled.setOnCheckedChangeListener { _, isChecked ->
            mSeekArc!!.isEnabled = isChecked
            mSeekArc!!.invalidate()
        }
    }

}
