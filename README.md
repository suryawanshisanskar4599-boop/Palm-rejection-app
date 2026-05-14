# Software-Based Palm Rejection System for Android Tablets

## Project Overview
This project is an Android application that implements a software-level palm rejection system. It analyzes touch behavior, such as touch size, pressure, movement speed, and multi-touch patterns, to distinguish intentional drawing input from accidental palm touches. It functions without the need for hardware palm rejection or the device's camera, relying entirely on the Android `MotionEvent` API.

> **Note**: This is a software-based touch filtering system and does not provide hardware-level palm rejection.

## Features
- **Palm Detection**: Uses heuristic analysis to filter out large accidental touches, high pressure, static touches, and multiple nearby touch points.
- **Intentional Drawing Input**: Allows drawing only when the touch is small, precise, and resembles pen/finger movement.
- **Smart Touch Filter**: Rejects wide-area, slow touches and multi-touch accidental contacts while accepting precise and fast directional strokes.
- **Real-Time Visual Feedback**: Displays a green text indicator for accepted input and a red overlay when an accidental palm touch is detected.
- **Customizable Settings**: Allows users to configure palm sensitivity and pressure thresholds via a settings screen.
- **Stylus Support**: Prioritizes stylus inputs (`MotionEvent.TOOL_TYPE_STYLUS`).

## Architecture

The project follows the MVVM Architecture and has been modularized as follows:

```
com.example.palmrejection/
├── ui/
│   ├── MainActivity.kt        // Handles UI and Touch Listeners
│   ├── DrawingView.kt         // Custom Canvas for drawing paths
│   ├── OverlayView.kt         // Custom View for red palm overlay
│   ├── SettingsActivity.kt    // Settings screen for thresholds
├── touch/
│   ├── TouchAnalyzer.kt       // Processes velocity and raw MotionEvent data
│   ├── PalmDetector.kt        // Analyzes touch logic for palm rejection
│   ├── StrokeManager.kt       // Handles stroke paths and undo/clear logic
├── model/
│   ├── TouchData.kt           // Data class wrapping MotionEvent details
│   ├── DetectionResult.kt     // Enum for detection status
├── utils/
│   ├── Constants.kt           // Constant threshold values
│   ├── MathUtils.kt           // Helper math functions for path processing
├── viewmodel/
│   ├── MainViewModel.kt       // ViewModel maintaining state and detector instances
```

## How Touch Filtering Works
1. **Size & Pressure**: When the user touches the screen, the size and pressure of the touch point are calculated. Large and high-pressure contacts are flagged as potential palms.
2. **Velocity**: The `VelocityTracker` calculates movement speed. Slow or static large touches are treated as resting palms.
3. **Multi-Touch**: A sudden cluster of points often suggests a palm resting on the screen rather than a single drawing finger.
4. **Tool Type**: Using a stylus automatically classifies the touch as a valid drawing input.

## Installation Guide
1. Clone this repository or copy the project files into your local directory.
2. Open the project in the latest stable version of **Android Studio**.
3. Let Gradle sync and download dependencies (such as AndroidX Core, Material, and Lifecycle).
4. Run the project on an Android emulator or a physical device (Tablet recommended for testing palm rejection). Minimum required SDK is Android 8.0 (API 26).

## Dependencies
- AndroidX Core KTX
- AndroidX AppCompat
- Google Material Design
- AndroidX ConstraintLayout
- AndroidX Lifecycle ViewModel
- AndroidX Activity KTX
- AndroidX Preference KTX

## Known Limitations
- Hardware restrictions: Standard capacitive screens return limited touch size and pressure data; actual performance varies by hardware manufacturer.
- Complex multi-touch gestures when resting a palm may cause occasional drawing interruptions.
- Fast accidental palm rolls could temporarily mimic valid drawing input before stabilizing.

## Future Improvements
- **Machine Learning Classifier**: Train a lightweight ML model (TensorFlow Lite) to classify touch points based on movement patterns over time.
- **Adaptive Thresholds**: Automatically adjust `palmSizeThreshold` and `pressureThreshold` based on individual user profiles and habits.
- **User Calibration Mode**: Provide an onboarding screen allowing users to train the app on their specific hand size and drawing style.
- **Stroke Prediction**: Implement bezier curve prediction for smoother stroke rendering while drawing.
