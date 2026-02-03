package com.braintrainer.app.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

// Turbo-fix: Implementing Alpha Extraction for guaranteed shadow softness
class ClashImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    // Paint for the shadow:
    // 1. Color set to semi-transparent black
    // 2. BlurMaskFilter applied
    // NO ColorFilter needed because we will draw the ALPHA mask.
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#80000000") // 50% Black
        maskFilter = BlurMaskFilter(35f, BlurMaskFilter.Blur.NORMAL) // Very soft
    }
    
    var showShadow: Boolean = true
    
    private val dx = 20f
    private val dy = 20f
    
    // Bitmap to capture the view's drawable content
    private var captureBitmap: Bitmap? = null
    private var captureCanvas: Canvas? = null
    
    // Extracted alpha mask (recycled correctly)
    private var alphaBitmap: Bitmap? = null

    init {
        // Essential: BlurMaskFilter requires Software rendering on View level for drawBitmap calls usually
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        if (width == 0 || height == 0) return
        val d = drawable ?: return

        // 1. Prepare Capture Bitmap
        if (captureBitmap == null || captureBitmap?.width != width || captureBitmap?.height != height) {
            captureBitmap?.recycle()
            alphaBitmap?.recycle()
            alphaBitmap = null
            
            try {
                captureBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                captureCanvas = Canvas(captureBitmap!!)
            } catch (e: Exception) {
                super.onDraw(canvas)
                return
            }
        }

        // 2. Clear and Draw Drawable to Capture Bitmap
        captureBitmap!!.eraseColor(Color.TRANSPARENT)
        super.onDraw(captureCanvas!!) 
        
        // 3. Extract Alpha Mask
        // We only regenerate this if key parameters changed, but for simplicity/safety in this dynamic view:
        // We do it every frame? No, expensive.
        // We can optimization: assume drawable doesn't animate its shape constantly.
        // But let's just do it. extractAlpha allocates a new Bitmap.
        // To avoid GC thrashing, we should only do this if 'd' changes? 
        // For this specific app (Quiz), the drawable changes when question changes.
        // However, invalidation happens.
        
        // Let's rely on the fact extractAlpha is reasonably fast for small UI images, 
        // but it DOES allocate. Ideally we cache 'alphaBitmap'.
        // CACHE LOGIC: 
        // If we assumed the image is static for this draw cycle. 
        // Let's do a quick "Are we dirty?" check? Hard to know if drawable changed content.
        
        if (showShadow) {
            val extracted = captureBitmap!!.extractAlpha()
            
            // 4. Draw Shadow using Alpha Mask
            // The shadowPaint colors the alpha pixels and blurs them.
            canvas.drawBitmap(extracted, dx, dy, shadowPaint)
            extracted.recycle()
        }
        
        // 5. Draw Foreground
        // Draw the cached capture so we don't re-render drawable logic
        canvas.drawBitmap(captureBitmap!!, 0f, 0f, null)
    }
}
