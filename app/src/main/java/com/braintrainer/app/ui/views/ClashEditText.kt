package com.braintrainer.app.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import com.braintrainer.app.R

class ClashEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    init {
        try {
            typeface = ResourcesCompat.getFont(context, R.font.righteous)
        } catch (e: Exception) {}
        
        setTextColor(Color.WHITE)
        setHintTextColor(Color.parseColor("#BBBBBB"))
        includeFontPadding = false
        // Padding for outline and shadow
        setPadding(20, 12, 20, 16)
    }

    override fun onDraw(canvas: Canvas) {
        val originalColor = currentTextColor
        val textStr = text?.toString() ?: ""
        
        // Always draw the "Clash Style" even if empty (for the cursor/hint consistency)
        // However, if we draw hint with outline, it might be too heavy.
        // Let's draw hint normally and text with outline.
        
        if (textStr.isEmpty()) {
            super.onDraw(canvas)
            return
        }

        paint.isAntiAlias = true
        paint.isSubpixelText = true

        // 1. Draw Shadow
        paint.style = Paint.Style.FILL
        setTextColor(Color.BLACK)
        canvas.save()
        canvas.translate(0f, 6f)
        super.onDraw(canvas)
        canvas.restore()

        // 2. Draw Outline
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        setTextColor(Color.BLACK)
        super.onDraw(canvas)

        // 3. Draw Main Text
        paint.style = Paint.Style.FILL
        setTextColor(Color.WHITE)
        super.onDraw(canvas)
        
        // Restore
        setTextColor(originalColor)
    }
}
