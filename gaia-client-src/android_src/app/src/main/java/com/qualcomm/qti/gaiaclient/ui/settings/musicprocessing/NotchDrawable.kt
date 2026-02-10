/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.musicprocessing

import android.graphics.*
import android.graphics.drawable.Drawable

class NotchDrawable(
    backgroundColor: Int, notchColor: Int,
    private val notches: Array<Notch>, private val density: Float
) : Drawable() {

    private var backgroundPaint: Paint = Paint()
    private var notchPaint: Paint = Paint()

    init {
        backgroundPaint.color = backgroundColor
        notchPaint.color = notchColor
    }

    override fun draw(canvas: Canvas) {
        // get the bounds
        val boundLeft: Float = bounds.left.toFloat()
        val boundRight: Float = bounds.right.toFloat()
        val boundTop: Float = bounds.top.toFloat()
        val boundBottom: Float = bounds.bottom.toFloat()
        val verticalCentre = ((boundBottom - boundTop) / 2f) + boundTop
        val width: Float = boundRight - boundLeft

        // get the bar thickness
        val thickness: Float = PROGRESS_THICKNESS_DP * density

        // draw background
        val top: Float = verticalCentre - thickness / 2f
        val bottom: Float = verticalCentre + thickness / 2f
        canvas.drawRoundRect(boundLeft, top, boundRight, bottom, 25f, 25f, backgroundPaint)

        // draw lines
        for (notch: Notch in notches) {
            val notchThickness: Float = notch.thickness * density
            val notchCenter: Float = boundLeft + (notch.percent * width)
            val notchLeft: Float = notchCenter - notchThickness / 2f
            val notchRight: Float = notchCenter + notchThickness / 2f
            canvas.drawRect(notchLeft, top, notchRight, bottom, notchPaint)
        }
    }

    override fun setAlpha(alpha: Int) {
        backgroundPaint.alpha = alpha
        notchPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        backgroundPaint.colorFilter = colorFilter
        notchPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat"))
    @Suppress("OverrideDeprecatedMigration")
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    companion object {
        const val PROGRESS_THICKNESS_DP: Int = 6
    }

}
