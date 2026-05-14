package com.example.palmrejection.touch

import android.view.MotionEvent
import android.view.VelocityTracker
import com.example.palmrejection.model.DetectionResult
import com.example.palmrejection.model.TouchData

class TouchAnalyzer(private val palmDetector: PalmDetector) {

    private var velocityTracker: VelocityTracker? = null

    fun analyzeEvent(event: MotionEvent): DetectionResult {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                velocityTracker?.clear()
                velocityTracker = velocityTracker ?: VelocityTracker.obtain()
                velocityTracker?.addMovement(event)
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(event)
                velocityTracker?.computeCurrentVelocity(1000)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                velocityTracker?.recycle()
                velocityTracker = null
            }
        }

        val pointerIndex = event.actionIndex
        if (pointerIndex < 0 || pointerIndex >= event.pointerCount) {
            return DetectionResult.UNKNOWN
        }
        
        val touchData = TouchData(
            x = event.getX(pointerIndex),
            y = event.getY(pointerIndex),
            size = event.getSize(pointerIndex),
            pressure = event.getPressure(pointerIndex),
            timestamp = event.eventTime,
            pointerId = event.getPointerId(pointerIndex),
            toolType = event.getToolType(pointerIndex),
            action = event.actionMasked
        )

        val velocity = velocityTracker?.let {
            val vx = it.getXVelocity(touchData.pointerId)
            val vy = it.getYVelocity(touchData.pointerId)
            Math.sqrt((vx * vx + vy * vy).toDouble()).toFloat()
        } ?: 0f

        return palmDetector.classifyTouch(touchData, velocity, event.pointerCount)
    }
}
