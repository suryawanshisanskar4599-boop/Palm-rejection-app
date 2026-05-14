package com.example.palmrejection.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.palmrejection.model.DetectionResult
import com.example.palmrejection.touch.PalmDetector
import com.example.palmrejection.touch.TouchAnalyzer

class MainViewModel : ViewModel() {

    val palmDetector = PalmDetector()
    val touchAnalyzer = TouchAnalyzer(palmDetector)

    private val _detectionStatus = MutableLiveData<DetectionResult>(DetectionResult.UNKNOWN)
    val detectionStatus: LiveData<DetectionResult> = _detectionStatus

    fun updateStatus(result: DetectionResult) {
        if (_detectionStatus.value != result) {
            _detectionStatus.value = result
        }
    }

    fun updateSettings(palmSensitivity: Float, pressureThreshold: Float) {
        palmDetector.palmSizeThreshold = palmSensitivity
        palmDetector.pressureThreshold = pressureThreshold
    }
}
