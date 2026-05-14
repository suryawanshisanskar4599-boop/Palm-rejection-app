package com.example.palmrejection.touch

import android.view.MotionEvent
import android.view.VelocityTracker
import com.example.palmrejection.model.DetectionResult
import com.example.palmrejection.model.TouchData

class TouchAnalyzer(private val palmDetector: PalmDetector) {

    private var velocityTracker: VelocityTracker? = null
    private val lockedPalmPointers = mutableSetOf<Int>()

    fun analyzeEvent(event: MotionEvent): Map<Int, DetectionResult> {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                velocityTracker?.clear()
                velocityTracker = velocityTracker ?: VelocityTracker.obtain()
                velocityTracker?.addMovement(event)
                lockedPalmPointers.clear()
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(event)
                velocityTracker?.computeCurrentVelocity(1000)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // handle recycle at the end
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val upPointerId = event.getPointerId(event.actionIndex)
                lockedPalmPointers.remove(upPointerId)
            }
        }

        val results = mutableMapOf<Int, DetectionResult>()

        for (i in 0 until event.pointerCount) {
            val pointerId = event.getPointerId(i)
            val touchData = TouchData(
                x = event.getX(i),
                y = event.getY(i),
                size = event.getSize(i),
                pressure = event.getPressure(i),
                timestamp = event.eventTime,
                pointerId = pointerId,
                toolType = event.getToolType(i),
                action = event.actionMasked
            )

            val velocity = velocityTracker?.let {
                val vx = it.getXVelocity(pointerId)
                val vy = it.getYVelocity(pointerId)
                Math.sqrt((vx * vx + vy * vy).toDouble()).toFloat()
            } ?: 0f

            if (lockedPalmPointers.contains(pointerId)) {
                results[pointerId] = DetectionResult.PALM_TOUCH
            } else {
                val result = palmDetector.classifyTouch(touchData, velocity)
                if (result == DetectionResult.PALM_TOUCH) {
                    lockedPalmPointers.add(pointerId)
                }
                results[pointerId] = result
            }
        }

        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            velocityTracker?.recycle()
            velocityTracker = null
            lockedPalmPointers.clear()
        }

        return results
    }
}
