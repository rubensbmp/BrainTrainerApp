package com.braintrainer.app.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import com.braintrainer.app.R

class ClashButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {
    var isSilent: Boolean = false

    init {
        try {
            typeface = ResourcesCompat.getFont(context, R.font.righteous)
        } catch (e: Exception) {}
        setTextColor(Color.WHITE)
        setAllCaps(false)
        setLayerType(LAYER_TYPE_SOFTWARE, null) 
        clipToOutline = false
        // Avoid aggressive padding that deforms the button shape
        setPadding(16, 8, 16, 12)
    }

    override fun onDraw(canvas: Canvas) {
        val originalColor = currentTextColor
        val realBackground = background
        
        // 1. Draw Background (with transparent text)
        setTextColor(Color.TRANSPARENT)
        super.onDraw(canvas)

        // 2. Draw Shadow Text (NO background)
        background = null
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
    
    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        when (event.action) {
            android.view.MotionEvent.ACTION_DOWN -> {
                if (!isSilent) {
                    com.braintrainer.app.util.MusicManager.playSFX(com.braintrainer.app.util.MusicManager.SFX.CLICK)
                }
                animate().scaleX(0.92f).scaleY(0.92f)
                    .setDuration(70)
                    .setInterpolator(android.view.animation.DecelerateInterpolator())
                    .start()
            }
            android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                animate().scaleX(1.0f).scaleY(1.0f)
                    .setDuration(150)
                    .setInterpolator(android.view.animation.OvershootInterpolator())
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
