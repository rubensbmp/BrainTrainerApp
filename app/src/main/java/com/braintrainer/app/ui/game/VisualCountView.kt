package com.braintrainer.app.ui.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.braintrainer.app.R
import kotlin.random.Random

class VisualCountView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var targetCount = 0
    private var distractorCount = 0
    
    // Type 0 = Target (Basketball), 1 = Box, 2 = Pizza, 3 = Tennis
    private data class Item(val type: Int, val x: Float, val y: Float, val size: Float)
    private val items = mutableListOf<Item>()
    private var isInitialized = false

    // Bitmaps
    private var bmpTarget: Bitmap? = null
    private var bmpDist1: Bitmap? = null
    private var bmpDist2: Bitmap? = null
    private var bmpDist3: Bitmap? = null

    init {
        setWillNotDraw(false)
        // Load Bitmaps
        bmpTarget = BitmapFactory.decodeResource(resources, R.drawable.ic_item_basketball)
        bmpDist1 = BitmapFactory.decodeResource(resources, R.drawable.ic_item_box)
        bmpDist2 = BitmapFactory.decodeResource(resources, R.drawable.ic_item_pizza)
        bmpDist3 = BitmapFactory.decodeResource(resources, R.drawable.ic_item_tennis)
    }

    fun configure(targetCount: Int, distractorCount: Int = 5) {
        this.targetCount = targetCount
        this.distractorCount = distractorCount
        generateItems()
        invalidate()
    }

    private fun generateItems() {
        items.clear()
        if (width > 0 && height > 0) {
            spawnItems(width, height)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        spawnItems(w, h)
    }

    private fun spawnItems(w: Int, h: Int) {
        if (w == 0 || h == 0) return
        items.clear()
        
        val padding = 20f
        val itemSize = 100f // Slightly bigger for icons
        val safeW = w - padding * 2
        val safeH = h - padding * 2
        
        val total = targetCount + distractorCount
        
        for (i in 0 until total) {
            val isTarget = i < targetCount
            val type = if (isTarget) 0 else Random.nextInt(1, 4) // 1, 2, or 3
            
            // Try to find a spot
            var x: Float
            var y: Float
            var attempts = 0
            do {
                x = padding + Random.nextFloat() * (safeW - itemSize) // Ensure strictly inside
                y = padding + Random.nextFloat() * (safeH - itemSize)
                attempts++
            } while (isTooClose(x, y, itemSize) && attempts < 15)
            
            // If failed, place anyway? Or skip? Overlap usually OK if small collisions
            items.add(Item(type, x, y, itemSize))
        }
        isInitialized = true
    }
    
    private fun isTooClose(x: Float, y: Float, size: Float): Boolean {
        items.forEach { item ->
            val dx = item.x - x
            val dy = item.y - y
            val dist = kotlin.math.sqrt(dx*dx + dy*dy)
            if (dist < size * 0.9f) return true // Allow slight overlap
        }
        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isInitialized && items.isEmpty() && (targetCount > 0 || distractorCount > 0)) {
            spawnItems(width, height)
        }

        items.forEach { item ->
            val bmp = when(item.type) {
                0 -> bmpTarget
                1 -> bmpDist1
                2 -> bmpDist2
                3 -> bmpDist3
                else -> bmpTarget
            }
            
            if (bmp != null) {
                val dst = RectF(item.x, item.y, item.x + item.size, item.y + item.size)
                canvas.drawBitmap(bmp, null, dst, null)
            }
        }
    }
}
