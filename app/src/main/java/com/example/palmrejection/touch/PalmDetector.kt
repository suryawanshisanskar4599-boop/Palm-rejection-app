package com.example.palmrejection.touch

import android.view.MotionEvent
import com.example.palmrejection.model.DetectionResult
import com.example.palmrejection.model.TouchData
import com.example.palmrejection.utils.Constants

class PalmDetector {

    var palmSizeThreshold: Float = 0.08f // Much smaller default size threshold
    var pressureThreshold: Float = 0.6f

    fun classifyTouch(
        touchData: TouchData,
        velocity: Float
    ): DetectionResult {
        // Prioritize stylus input
        if (touchData.toolType == MotionEvent.TOOL_TYPE_STYLUS) {
            return DetectionResult.VALID_DRAW_INPUT
        }

        val isLargeTouch = touchData.size > palmSizeThreshold
        val isHighPressure = touchData.pressure > pressureThreshold
        val isSlowMovement = velocity < Constants.DEFAULT_VELOCITY_THRESHOLD

        if (isLargeTouch && (isHighPressure || isSlowMovement)) {
            return DetectionResult.PALM_TOUCH
        }

        // Additional heuristic check: even if not large, very high pressure might be a palm roll
        if (isHighPressure && touchData.size > (palmSizeThreshold * 0.8f)) {
            return DetectionResult.PALM_TOUCH
        }

        return DetectionResult.VALID_DRAW_INPUT
    }
}
