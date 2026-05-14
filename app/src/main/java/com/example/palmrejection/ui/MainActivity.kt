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
        val sensitivity = prefs.getInt("palm_sensitivity", 50) / 100f
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
            val result = viewModel.touchAnalyzer.analyzeEvent(event)
            viewModel.updateStatus(result)

            if (result == DetectionResult.VALID_DRAW_INPUT) {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        isDrawing = true
                        binding.drawingView.startStroke(event.x, event.y)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (isDrawing) {
                            binding.drawingView.continueStroke(event.x, event.y)
                        } else {
                            // If we weren't drawing but now valid, we can start
                            isDrawing = true
                            binding.drawingView.startStroke(event.x, event.y)
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (isDrawing) {
                            binding.drawingView.finishStroke()
                        }
                        isDrawing = false
                        viewModel.updateStatus(DetectionResult.UNKNOWN)
                    }
                }
            } else if (result == DetectionResult.PALM_TOUCH) {
                // If it was drawing and now palm, we might want to cancel current stroke
                if (isDrawing) {
                    isDrawing = false
                    // Optionally remove the current invalid stroke
                }
                if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                    viewModel.updateStatus(DetectionResult.UNKNOWN)
                }
            }
            true // Consume the event
        }
    }
}
