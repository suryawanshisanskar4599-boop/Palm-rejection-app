package com.example.palmrejection.touch

import android.view.MotionEvent
import android.view.VelocityTracker
import com.example.palmrejection.model.DetectionResult
import com.example.palmrejection.model.TouchData

class TouchAnalyzer(private val palmDetector: PalmDetector) {

    private var velocityTracker: VelocityTracker? = null
    private val lockedPalmPointers = mutableSetOf<Int>()
    private val pointerStartX = mutableMapOf<Int, Float>()
    private val pointerStartY = mutableMapOf<Int, Float>()

    fun analyzeEvent(event: MotionEvent): Map<Int, DetectionResult> {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                velocityTracker?.clear()
                velocityTracker = velocityTracker ?: VelocityTracker.obtain()
                velocityTracker?.addMovement(event)
                lockedPalmPointers.clear()
                pointerStartX.clear()
                pointerStartY.clear()
                
                val id = event.getPointerId(0)
                pointerStartX[id] = event.getX(0)
                pointerStartY[id] = event.getY(0)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                pointerStartX[id] = event.getX(index)
                pointerStartY[id] = event.getY(index)
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
                pointerStartX.remove(upPointerId)
                pointerStartY.remove(upPointerId)
            }
        }

        val results = mutableMapOf<Int, DetectionResult>()

        for (i in 0 until event.pointerCount) {
            val pointerId = event.getPointerId(i)
            val x = event.getX(i)
            val y = event.getY(i)
            val touchData = TouchData(
                x = x,
                y = y,
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

            // Calculate how far the pointer has moved since it first touched
            val startX = pointerStartX[pointerId] ?: x
            val startY = pointerStartY[pointerId] ?: y
            val dx = x - startX
            val dy = y - startY
            val distanceMoved = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

            // If a pointer moves more than 40 pixels, it is actively drawing. Break the palm lock!
            if (distanceMoved > 40f) {
                lockedPalmPointers.remove(pointerId)
                results[pointerId] = DetectionResult.VALID_DRAW_INPUT
            } else {
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
        }

        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            velocityTracker?.recycle()
            velocityTracker = null
            lockedPalmPointers.clear()
            pointerStartX.clear()
            pointerStartY.clear()
        }

        return results
    }
}
