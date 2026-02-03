package com.braintrainer.app.ui.stats

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LineChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50") // Green line
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    
    private val dotStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#20000000") // Semi-transparent black
        strokeWidth = 2f
    }
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333")
        textSize = 36f
        textAlign = Paint.Align.CENTER
    }

    private var dataPoints: List<Pair<Long, Float>> = emptyList() // DateMillis, Value

    fun setData(data: List<Pair<Long, Int>>) {
        // Sort by date just in case
        this.dataPoints = data.sortedBy { it.first }.map { it.first to it.second.toFloat() }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (dataPoints.isEmpty()) {
            canvas.drawText(context.getString(com.braintrainer.app.R.string.stats_no_data), width / 2f, height / 2f, textPaint)
            return
        }

        val padding = 60f
        val w = width.toFloat()
        val h = height.toFloat()
        val graphW = w - padding * 2
        val graphH = h - padding * 2

        // Determine ranges
        val minX = dataPoints.minOf { it.first }
        val maxX = dataPoints.maxOf { it.first }
        val minY = dataPoints.minOf { it.second }.coerceAtMost(80f) // Brain Age min sensible
        val maxY = dataPoints.maxOf { it.second }.coerceAtLeast(minY + 10f)

        val xRange = (maxX - minX).coerceAtLeast(1)
        val yRange = (maxY - minY).coerceAtLeast(1f)

        // Draw Grid Lines (Horizontal) - 5 lines (Y-Axis)
        // We want MIN Value (Best Score) at TOP
        // MAX Value (Worst Score) at BOTTOM
        for (i in 0..4) {
            val y = padding + (i * graphH / 4) // Top to Bottom
            canvas.drawLine(padding, y, w - padding, y, gridPaint)
            val value = minY + (i * yRange / 4)
            canvas.drawText("${value.toInt()}", padding / 2, y + 10f, textPaint.apply { textAlign = Paint.Align.CENTER })
        }
        textPaint.textAlign = Paint.Align.CENTER // Reset
        
        // Draw X-Axis Labels (Date) - 3 labels (Start, Middle, End)
        val dateFmt = SimpleDateFormat("dd/MM", Locale.getDefault())
        for (i in 0..2) {
            val x = padding + (i * graphW / 2)
            val time = minX + (i * xRange / 2)
            val label = dateFmt.format(Date(time))
            canvas.drawText(label, x, h - padding + 40f, textPaint)
        }

        // Path
        val path = Path()
        val points = mutableListOf<Pair<Float, Float>>()

        dataPoints.forEachIndexed { index, point ->
            val x = padding + ((point.first - minX).toFloat() / xRange) * graphW
            // Y Axis Inverted: Min Value (Best) at Top (padding), Max Value (Worst) at Bottom
            val y = padding + ((point.second - minY) / yRange) * graphH
            
            points.add(x to y)
            
            if (index == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
        }

        canvas.drawPath(path, linePaint)

        // Draw dots and values
        points.forEachIndexed { index, (x, y) ->
            canvas.drawCircle(x, y, 12f, dotPaint)
            canvas.drawCircle(x, y, 12f, dotStrokePaint)
            
            // Draw Brain Age Value above dot
            val ageVal = dataPoints[index].second.toInt()
            canvas.drawText("$ageVal", x, y - 25f, textPaint)
        }
    }
}
