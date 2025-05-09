package rishika.iot.healthmonitor.presentation.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class StepSensorManager(
    private val context: Context,
    private val onStepCountChanged: (Float) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private var usingStepDetector = false
    private var stepCountFromDetector = 0f

    fun startListening() {
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        sensors.forEach {
            Log.d("AvailableSensors", "${it.name} - ${it.type}")
        }

        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("StepSensor", "Started listening with Step Counter.")
        } else if (stepDetectorSensor != null) {
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
            usingStepDetector = true
            Log.d("StepSensor", "Step Counter not available. Using Step Detector instead.")
        } else {
            Log.e("StepSensor", "No step-related sensors available.")
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
        Log.d("StepSensor", "Stopped listening for step count.")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val stepCount = event.values[0]
//                Log.d("StepSensor", "Step Counter: $stepCount")
                onStepCountChanged(stepCount)
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                stepCountFromDetector += event.values[0] // Should be 1.0 per step
//                Log.d("StepSensor", "Step Detector: $stepCountFromDetector")
                onStepCountChanged(stepCountFromDetector)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
