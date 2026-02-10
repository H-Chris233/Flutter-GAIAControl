/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.musicprocessing

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatSeekBar
import com.qualcomm.qti.gaiaclient.R

@SuppressLint("RtlHardcoded")
class VerticalSeekBar : AppCompatSeekBar {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var changeListener: OnSeekBarChangeListener? = null

    private val minGain = -12.0
    private val maxGain = 3.0

    private val notches: Array<Notch> = arrayOf(
        Notch(getPercent(0.0), 2),
        Notch(getPercent(-6.0), 1)
    )

    init {
        progress = 50
        splitTrack = false

        // get drawables for the bar background and progress with notches
        val density = context.resources.displayMetrics.density
        val background = NotchDrawable(
            resources.getColor(R.color.vertical_slider_progress_background, context.theme),
            resources.getColor(R.color.vertical_slider_progress_background_notch, context.theme),
            notches, density
        )
        val clipDrawable = NotchDrawable(
            resources.getColor(R.color.vertical_slider_progress, context.theme),
            resources.getColor(R.color.vertical_slider_progress_notch, context.theme),
            notches, density
        )
        val progressClip = ClipDrawable(clipDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL)

        // set drawables
        val layer = progressDrawable.mutate() as LayerDrawable
        layer.setDrawableByLayerId(android.R.id.background, background)
        layer.setDrawableByLayerId(android.R.id.progress, progressClip)
        progressDrawable = layer
    }

    var gain: Double
        set(newGain) {
            // Need to normalize -12 -> +3 to 0 -> 100
            val range = maxGain - minGain
            val newProgress = ((newGain - minGain) / range) * 100
            progress = newProgress.toInt()
            onSizeChanged(width, height, 0, 0)
        }
        get() {
            val range = maxGain - minGain
            return ((progress / 100.0) * range) + minGain
        }

    override fun setOnSeekBarChangeListener(newListener: OnSeekBarChangeListener?) {
        changeListener = newListener
    }

    override fun onDraw(canvas: Canvas?) {
        // We need to rotate and move the canvas to draw vertically
        canvas?.rotate(-90.0f)
        canvas?.translate(-height.toFloat(), 0f)
        super.onDraw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Note swapped width and height
        super.onSizeChanged(h, w, oldh, oldw)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Note swapped width and height
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        // Note swapped width and height
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                progress = max - (max * (event.y / height)).toInt()
                onSizeChanged(width, height, 0, 0)
                changeListener?.onProgressChanged(this, progress, true)
            }
            MotionEvent.ACTION_DOWN -> {
                changeListener?.onStartTrackingTouch(this)
            }
            MotionEvent.ACTION_UP -> {
                changeListener?.onStopTrackingTouch(this)
            }
        }
        return true
    }

    private fun getPercent(gain: Double): Float {
        return (gain - minGain).toFloat() / (maxGain - minGain).toFloat();
    }
}
