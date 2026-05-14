package com.example.palmrejection.ui

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.palmrejection.R
import com.example.palmrejection.databinding.ActivityMainBinding
import com.example.palmrejection.model.DetectionResult
import com.example.palmrejection.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private var isDrawing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        // Scale 1-100 to realistic touch sizes (0.01 to 0.25)
        val sensitivity = prefs.getInt("palm_sensitivity", 30) / 400f
        val pressure = prefs.getInt("pressure_threshold", 50) / 100f
        viewModel.updateSettings(sensitivity, pressure)
    }

    private fun setupObservers() {
        viewModel.detectionStatus.observe(this) { status ->
            when (status) {
                DetectionResult.PALM_TOUCH -> {
                    binding.statusText.text = getString(R.string.status_palm_detected)
                    binding.statusText.setTextColor(ContextCompat.getColor(this, R.color.status_red))
                    binding.overlayView.visibility = View.VISIBLE
                }
                DetectionResult.VALID_DRAW_INPUT -> {
                    binding.statusText.text = getString(R.string.status_drawing)
                    binding.statusText.setTextColor(ContextCompat.getColor(this, R.color.status_green))
                    binding.overlayView.visibility = View.GONE
                }
                else -> {
                    binding.overlayView.visibility = View.GONE
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnClear.setOnClickListener {
            binding.drawingView.clear()
        }

        binding.btnUndo.setOnClickListener {
            binding.drawingView.undo()
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.drawingView.setOnTouchListener { _, event ->
            val results = viewModel.touchAnalyzer.analyzeEvent(event)
            
            var anyPalmDetected = false
            var anyDrawingActive = false

            for (i in 0 until event.pointerCount) {
                val pointerId = event.getPointerId(i)
                val result = results[pointerId]
                
                if (result == DetectionResult.PALM_TOUCH) {
                    anyPalmDetected = true
                } else if (result == DetectionResult.VALID_DRAW_INPUT) {
                    anyDrawingActive = true
                    val x = event.getX(i)
                    val y = event.getY(i)
                    
                    // A pointer can be moving during ANY event (even POINTER_DOWN/UP for other pointers).
                    // Always continue the stroke. StrokeManager will start it if it doesn't exist.
                    binding.drawingView.continueStroke(pointerId, x, y)

                    // Specifically handle start/finish for the exact pointer triggering the event
                    if (event.actionIndex == i) {
                        when (event.actionMasked) {
                            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                                // Stroke is already started/continued by the line above, but we can explicitly start if needed
                                binding.drawingView.startStroke(pointerId, x, y)
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                                binding.drawingView.finishStroke(pointerId)
                            }
                        }
                    }
                }
            }

            if (anyPalmDetected) {
                viewModel.updateStatus(DetectionResult.PALM_TOUCH)
            } else if (anyDrawingActive) {
                viewModel.updateStatus(DetectionResult.VALID_DRAW_INPUT)
            } else if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                viewModel.updateStatus(DetectionResult.UNKNOWN)
            }

            true // Consume the event
        }
    }
}
