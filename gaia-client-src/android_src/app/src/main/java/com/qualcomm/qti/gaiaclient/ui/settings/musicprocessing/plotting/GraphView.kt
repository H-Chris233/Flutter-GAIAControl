/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.musicprocessing.plotting

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.qualcomm.qti.gaiaclient.R
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow

class GraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * Definition of the styles for the different elements to be drawn for this GraphView.
     */
    private val paints = object {
        val axeLabels = Paint()
        val grid = Paint()
        val curve = Paint()

        init {
            axeLabels.textSize = 28.0.toFloat()
            axeLabels.isAntiAlias = true
            axeLabels.style = Paint.Style.FILL_AND_STROKE
            axeLabels.color = resources.getColor(R.color.eq_graphview_axe_label, null)

            grid.color = resources.getColor(R.color.eq_graphview_grid, null)
            grid.isAntiAlias = true
            grid.strokeWidth = 2.0.toFloat()
            grid.style = Paint.Style.STROKE

            curve.color = resources.getColor(R.color.eq_graphview_curve, null)
            curve.isAntiAlias = true
            curve.strokeWidth = 3.0.toFloat()
            curve.style = Paint.Style.STROKE
        }
    }

    private val dbMaxMin = 12.0
    private val startFrequency = 20.0
    private val endFrequency = 20000.0

    private val marginInset = 40
    private val yMin = -18.0
    private val yMax = 12.0
    private var currentWidth = 0
    private var currentHeight = 0

    private var graphData = GraphData(emptyList(), emptyList())

    var filterBank = EQFilterBank()
        set(newValue) {
            field = newValue
            recalculate()
            invalidate()
        }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawGraph(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        currentWidth = w
        currentHeight = h
        recalculate()
    }

    private fun recalculate() {
        // work out frequency points we want to plot depending on width of graph
        val frequencies = mutableListOf<Double>()

        if (currentWidth > marginInset * 2) {
            val logRatio = log10(endFrequency / startFrequency)
            for (xPos in marginInset until currentWidth - marginInset) {
                val freq =
                    startFrequency * 10.0.pow(((xPos - marginInset).toDouble() / (currentWidth - (marginInset * 2)).toDouble()) * logRatio)

                if (freq < (filterBank.frequency / 2)) {
                    frequencies.add(freq)
                }
            }
        }

        graphData = filterBank.generateDbGainGraph(frequencies)
    }

    private fun drawGraph(canvas: Canvas?) {
        if (canvas == null) {
            return
        }

        drawScale(canvas)
        drawAxesLabels(canvas)
        plotPoints(canvas)
    }

    private fun drawScale(canvas: Canvas) {
        // Draw outer frame
        val frameRect = Rect(marginInset, marginInset, currentWidth - marginInset, currentHeight - marginInset)
        val framePath = Path()
        framePath.addRect(RectF(frameRect), Path.Direction.CW)
        canvas.drawPath(framePath, paints.grid)

        val frameHeight = currentHeight - (2 * marginInset)
        val frameWidth = currentWidth - (2 * marginInset)
        val yScale = frameHeight.toDouble() / (yMax - yMin)

        var y: Double
        var level = 0.0
        val yStep = if (dbMaxMin > 5.9) 6.0 else 1.0
        val linePath = Path()

        //Draw Horizontal lines.

        while (level <= yMax) {
            y = ((yMax - level) * yScale) + marginInset.toDouble()

            linePath.moveTo((marginInset + 1).toFloat(), y.toFloat())
            linePath.lineTo((currentWidth - (marginInset + 1)).toFloat(), y.toFloat())
            canvas.drawPath(linePath, paints.grid)
            linePath.reset()
            level += yStep
        }

        level = -yStep
        while (level >= yMin) {
            y = ((yMax - level) * yScale) + marginInset.toDouble()
            linePath.moveTo((marginInset + 1).toFloat(), y.toFloat())
            linePath.lineTo((currentWidth - (marginInset + 1)).toFloat(), y.toFloat())
            canvas.drawPath(linePath, paints.grid)
            linePath.reset()
            level -= yStep
        }

        // Draw Vertical lines
        var freqExp = log10(startFrequency).toInt()
        var freqMant = (ceil(startFrequency / 10.0.pow(freqExp.toDouble()))).toInt()

        var freq: Double
        do {
            freqMant += 1
            if (freqMant >= 10) {
                freqMant = 1
                freqExp += 1
            }
            freq = freqMant.toDouble() * 10.0.pow(freqExp.toDouble())
            val x =
                marginInset + (frameWidth * ((ln(freq) - ln(startFrequency)) / (ln(endFrequency) - ln(startFrequency))))
            linePath.moveTo(x.toFloat(), (marginInset + 1).toFloat())
            linePath.lineTo(x.toFloat(), (marginInset + frameHeight - 1).toFloat())
            canvas.drawPath(linePath, paints.grid)
            linePath.reset()
        } while (freq < endFrequency)
    }

    private fun drawAxesLabels(canvas: Canvas) {
        val frameHeight = currentHeight - (2 * marginInset)
        val frameWidth = currentWidth - (2 * marginInset)

        val yScale = frameHeight.toDouble() / (yMax - yMin)

        var y: Double
        var level = 0.0
        val yStep = if (dbMaxMin > 5.9) 6.0 else 1.0

        //Draw Horizontal Axes

        while (level <= yMax) {
            y = ((yMax - level) * yScale) + marginInset
            val str = "%.0f".format(level)
            canvas.drawText(str, 0.0.toFloat(), (y + 10.0).toFloat(), paints.axeLabels)
            level += yStep
        }

        level = -yStep

        while (level >= yMin) {
            y = ((yMax - level) * yScale) + marginInset
            val str = "%.0f".format(level)
            canvas.drawText(str, 0.0.toFloat(), (y + 10.0).toFloat(), paints.axeLabels)
            level -= yStep
        }

        // Draw Vertical Axes
        var freqExp = (log10(startFrequency)).toInt()
        var freqMant = (ceil(startFrequency / 10.0.pow(freqExp.toDouble()))).toInt()

        var freq = 0.0
        do {
            freqMant += 1
            if (freqMant >= 10) {
                freqMant = 1
                freqExp += 1
            }

            if (freqMant == 1) {
                // Draw text
                freq = freqMant.toDouble() * 10.0.pow(freqExp.toDouble())
                val x =
                    marginInset + (frameWidth * ((ln(freq) - ln(startFrequency)) / (ln(endFrequency) - ln(startFrequency))))

                val str = if (freq < 1000.0) "%.0f".format(freq) else "%.0fK".format(freq / 1000.0)
                val strWidth = paints.axeLabels.measureText(str)
                canvas.drawText(str, (x - (strWidth / 2)).toFloat(), currentHeight.toFloat() - 2, paints.axeLabels)
            }
        } while (freq < endFrequency)
    }

    private fun screenPoint(graphPoint: GraphPoint): PointF {
        val frameHeight = currentHeight - (2 * marginInset)
        val frameWidth = currentWidth - (2 * marginInset)

        val yScale = frameHeight.toDouble() / (yMax - yMin)
        val freq = graphPoint.frequency
        val result = graphPoint.result
        val x = marginInset + (frameWidth * (ln(freq) - ln(startFrequency)) /
                (ln(endFrequency) - ln(startFrequency)))

        val y = marginInset + ((yMax - result) * yScale)
        return PointF(x.toFloat(), y.toFloat())
    }

    private fun drawCurve(curve: GraphCurve, canvas: Canvas) {
        val path = Path()

        // Move to first point

        if (curve.count() > 1) {
            val firstPoint = screenPoint(curve[0])
            path.moveTo(firstPoint.x, firstPoint.y)

            // Now add in extra points
            for (pointIndex in 1 until curve.count()) {
                val point = screenPoint(curve[pointIndex])
                path.lineTo(point.x, point.y)
            }
            canvas.drawPath(path, paints.curve)
        }
    }

    private fun plotPoints(canvas: Canvas) {
        // Draw combined
        drawCurve(graphData.totalCurve, canvas)
    }
}
