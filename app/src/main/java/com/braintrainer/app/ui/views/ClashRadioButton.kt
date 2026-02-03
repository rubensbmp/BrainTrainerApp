package com.braintrainer.app.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.content.res.ResourcesCompat
import com.braintrainer.app.R

class ClashRadioButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.radioButtonStyle
) : AppCompatRadioButton(context, attrs, defStyleAttr) {

    init {
        try {
            typeface = ResourcesCompat.getFont(context, R.font.righteous)
        } catch (e: Exception) {}
        setTextColor(Color.WHITE)
        setAllCaps(false)
        setLayerType(LAYER_TYPE_SOFTWARE, null) 
        clipToOutline = false
        buttonDrawable = null
        includeFontPadding = false
    }

    override fun onDraw(canvas: Canvas) {
        val originalColor = currentTextColor
        val realBackground = background
        
        // 1. Draw Background (with transparent text)
        setTextColor(Color.TRANSPARENT)
        super.onDraw(canvas)

        // 2. Draw Shadow Text (NO background)
        background = null
        paint.isAntiAlias = true
        paint.isSubpixelText = true
        paint.style = Paint.Style.FILL
        setTextColor(Color.BLACK)
        canvas.save()
        canvas.translate(0f, 6f)
        super.onDraw(canvas)
        canvas.restore()

        // 3. Draw Outline Text (NO background)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        setTextColor(Color.BLACK)
        super.onDraw(canvas)

        // 4. Draw Main Text (NO background)
        paint.style = Paint.Style.FILL
        setTextColor(Color.WHITE)
        super.onDraw(canvas)
        
        // Restore
        background = realBackground
        updateTextColors(android.content.res.ColorStateList.valueOf(originalColor))
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                animate().scaleX(0.92f).scaleY(0.92f)
                    .setDuration(70)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                animate().scaleX(1.0f).scaleY(1.0f)
                    .setDuration(150)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateTextColors(colors: android.content.res.ColorStateList) {
        try {
            val method = android.widget.TextView::class.java.getDeclaredMethod("setTextColor", android.content.res.ColorStateList::class.java)
            method.isAccessible = true
            method.invoke(this, colors)
        } catch(e: Exception) {
            super.setTextColor(colors)
        }
    }
}
