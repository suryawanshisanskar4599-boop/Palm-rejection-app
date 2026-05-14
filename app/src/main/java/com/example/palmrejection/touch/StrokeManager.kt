package com.example.palmrejection.touch

import android.graphics.Path

class StrokeManager {
    val activePaths = mutableMapOf<Int, Path>()
    val completedPaths = mutableListOf<Path>()

    fun startStroke(pointerId: Int, x: Float, y: Float) {
        val path = Path()
        path.moveTo(x, y)
        activePaths[pointerId] = path
    }

    fun continueStroke(pointerId: Int, x: Float, y: Float) {
        val path = activePaths[pointerId]
        if (path != null) {
            path.lineTo(x, y)
        } else {
            startStroke(pointerId, x, y)
        }
    }

    fun finishStroke(pointerId: Int) {
        activePaths.remove(pointerId)?.let { path ->
            if (!path.isEmpty) {
                completedPaths.add(path)
            }
        }
    }

    fun undo() {
        if (completedPaths.isNotEmpty()) {
            completedPaths.removeAt(completedPaths.size - 1)
        }
    }

    fun clear() {
        completedPaths.clear()
        activePaths.clear()
    }
}
