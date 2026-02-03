package com.braintrainer.app.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.braintrainer.app.R

class ClashBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val baseBitmap: Bitmap? = try {
        BitmapFactory.decodeResource(resources, R.drawable.bg_clash_base)
    } catch (e: Exception) { null }

    private val patternBitmap: Bitmap? = try {
        BitmapFactory.decodeResource(resources, R.drawable.bg_clash_pattern)
    } catch (e: Exception) { null }

    private var patternShader: BitmapShader? = null
    private val patternPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val matrix = Matrix()
    
    private var offsetX = 0f
    private var offsetY = 0f
    private val scrollSpeed = 0.5f // Pixels per frame approx

    init {
        patternBitmap?.let {
            patternShader = BitmapShader(it, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            patternPaint.shader = patternShader
            patternPaint.alpha = 130 // Subtle pattern transparency (adjust as needed)
        }
    }

    override fun onDraw(canvas: Canvas) {
        // 1. Draw Static Base
        baseBitmap?.let {
            val src = Rect(0, 0, it.width, it.height)
            val dst = Rect(0, 0, width, height)
            canvas.drawBitmap(it, src, dst, null)
        } ?: canvas.drawColor(Color.parseColor("#1A5BBD")) // Fallback blue

        // 2. Draw Animated Pattern
        patternShader?.let {
            offsetX += scrollSpeed
            offsetY += scrollSpeed
            
            // Loop offsets to keep them small (though BitmapShader handles repeats)
            if (patternBitmap != null) {
                if (offsetX > patternBitmap.width) offsetX -= patternBitmap.width
                if (offsetY > patternBitmap.height) offsetY -= patternBitmap.height
            }
            
            matrix.setTranslate(offsetX, offsetY)
            it.setLocalMatrix(matrix)
            
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), patternPaint)
        }

        // 3. Keep animating
        invalidate()
    }
}
