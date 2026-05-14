package com.example.palmrejection.touch

import android.graphics.Path

class StrokeManager {
    val currentPath = Path()
    val completedPaths = mutableListOf<Path>()

    fun startStroke(x: Float, y: Float) {
        currentPath.moveTo(x, y)
    }

    fun continueStroke(x: Float, y: Float) {
        currentPath.lineTo(x, y)
    }

    fun finishStroke() {
        if (!currentPath.isEmpty) {
            completedPaths.add(Path(currentPath))
            currentPath.reset()
        }
    }

    fun undo() {
        if (completedPaths.isNotEmpty()) {
            completedPaths.removeAt(completedPaths.size - 1)
        }
    }

    fun clear() {
        completedPaths.clear()
        currentPath.reset()
    }
}
