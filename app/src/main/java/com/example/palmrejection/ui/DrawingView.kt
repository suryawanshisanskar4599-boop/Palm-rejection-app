package com.example.palmrejection.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.palmrejection.R
import com.example.palmrejection.touch.StrokeManager

class DrawingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val strokeManager = StrokeManager()
    
    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.stroke_color)
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 10f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw completed paths
        for (path in strokeManager.completedPaths) {
            canvas.drawPath(path, paint)
        }
        // Draw current paths
        for (path in strokeManager.activePaths.values) {
            canvas.drawPath(path, paint)
        }
    }

    fun startStroke(pointerId: Int, x: Float, y: Float) {
        strokeManager.startStroke(pointerId, x, y)
        invalidate()
    }

    fun continueStroke(pointerId: Int, x: Float, y: Float) {
        strokeManager.continueStroke(pointerId, x, y)
        invalidate()
    }

    fun finishStroke(pointerId: Int) {
        strokeManager.finishStroke(pointerId)
        invalidate()
    }

    fun undo() {
        strokeManager.undo()
        invalidate()
    }

    fun clear() {
        strokeManager.clear()
        invalidate()
    }
}
