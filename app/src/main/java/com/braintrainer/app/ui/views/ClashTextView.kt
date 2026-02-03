package com.braintrainer.app.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.braintrainer.app.R

class ClashTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    
    var showShadow: Boolean = true

    init {
        try {
            typeface = ResourcesCompat.getFont(context, R.font.righteous)
        } catch (e: Exception) {}
        
        setTextColor(Color.WHITE)
        includeFontPadding = false
        // Extra padding to accommodate outline (10f) and shadow (6f)
        setPadding(16, 8, 16, 16)
    }

    override fun onDraw(canvas: Canvas) {
        if (text.isNullOrEmpty()) {
            super.onDraw(canvas)
            return
        }

        val originalColor = currentTextColor
        val realBackground = background
        paint.isAntiAlias = true
        paint.isSubpixelText = true

        // 1. Draw Background (with transparent text)
        setTextColor(Color.TRANSPARENT)
        super.onDraw(canvas)

        // Avoid drawing background again
        background = null

        if (showShadow) {
            // 2. Draw Shadow Text
            paint.style = Paint.Style.FILL
            setTextColor(Color.BLACK)
            canvas.save()
            canvas.translate(0f, 6f)
            super.onDraw(canvas)
            canvas.restore()

            // 3. Draw Outline Text
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 10f
            paint.strokeJoin = Paint.Join.ROUND
            paint.strokeCap = Paint.Cap.ROUND
            setTextColor(Color.BLACK)
            super.onDraw(canvas)
        }

        // 4. Draw Main Text
        paint.style = Paint.Style.FILL
        setTextColor(Color.WHITE)
        super.onDraw(canvas)
        
        // Restore
        background = realBackground
        setTextColor(originalColor)
    }
}
