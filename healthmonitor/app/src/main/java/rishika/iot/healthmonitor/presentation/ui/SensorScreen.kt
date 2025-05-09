package rishika.iot.healthmonitor.presentation.ui

import MqttService
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import rishika.iot.healthmonitor.presentation.data.SensorDao
import rishika.iot.healthmonitor.presentation.data.SensorData
import rishika.iot.healthmonitor.presentation.data.SensorDatabase
import rishika.iot.healthmonitor.presentation.sensors.HeartRateSensorManager
import rishika.iot.healthmonitor.presentation.sensors.StepSensorManager

@Composable
fun SensorScreen(context: Context) {
    var heartRate by remember { mutableStateOf(0f) }
    var steps by remember { mutableStateOf(0f) }

    val mqttService = remember { MqttService() }
    val coroutineScope = rememberCoroutineScope()
    val sensorDao = remember { SensorDatabase.getDatabase(context).sensorDao() }

    // This flag helps to track if sync is required
    var isSyncRequired by remember { mutableStateOf(false) }

    // ✅ Moved outside callbacks, so state persists correctly
    val lastHeartRatePublishTime = remember { mutableStateOf(0L) }
    val lastStepPublishTime = remember { mutableStateOf(0L) }
    val publishIntervalMillis = 10_000L  // 10 seconds for publishing

    // Connectivity Check Method
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    // Sync Method to publish unsynced data
    fun syncRoomDataToMqtt(sensorDao: SensorDao, mqttService: MqttService) {
        // Launch a coroutine to call the suspend function
        coroutineScope.launch {
            val unsyncedData = sensorDao.getAll() // Get all data that is stored in Room
            for (data in unsyncedData) {
                val topic = when (data.type) {
                    "heart_rate" -> "wearos/heart_rate"
                    "steps" -> "wearos/steps"
                    else -> return@launch
                }
                mqttService.publish(
                    topic,
                    """{"value": ${data.value}, "timestamp": ${data.timestamp}}"""
                )
                Log.d("MQTT", "Synced data: $data")
            }
        }
    }


    // Connect to MQTT and sync data if network available
    LaunchedEffect(Unit) {
        mqttService.connect(
            onConnected = {
                Log.d("MQTT", "Connected successfully!")
                if (isNetworkAvailable(context)) {
                    syncRoomDataToMqtt(sensorDao, mqttService) // Sync data when online
                }
            },
            onFailure = { e -> e.printStackTrace() }
        )
    }

    // Heart rate sensor manager
    val heartRateSensorManager = remember {
        HeartRateSensorManager(context) { hr ->
            heartRate = hr
            val timestamp = System.currentTimeMillis()
            coroutineScope.launch {
                sensorDao.insert(SensorData(type = "heart_rate", value = hr, timestamp = timestamp))
            }
            mqttService.publish("wearos/heart_rate", """{"value": $hr, "timestamp": $timestamp}""")
            Log.d("MQTT", "Published heart rate: $hr")
        }
    }

    // Step sensor manager
    val stepSensorManager = remember {
        StepSensorManager(context) { stepCount ->
            steps = stepCount
            val timestamp = System.currentTimeMillis()
            coroutineScope.launch {
                sensorDao.insert(SensorData(type = "steps", value = stepCount, timestamp = timestamp))
            }
            mqttService.publish("wearos/steps", """{"value": $stepCount, "timestamp": $timestamp}""")
            Log.d("MQTT", "Published steps: $stepCount")
        }
    }

    // Disposable effect to start and stop sensors
    DisposableEffect(Unit) {
        heartRateSensorManager.startListening()
        stepSensorManager.startListening()

        onDispose {
            heartRateSensorManager.stopListening()
            stepSensorManager.stopListening()
            mqttService.disconnect()
        }
    }

    // UI Layout for displaying heart rate and steps
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Heart Rate: $heartRate bpm", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Steps: $steps", style = MaterialTheme.typography.headlineMedium)

        if (heartRate > 100) {
            Text("⚠️ High Heart Rate!", color = MaterialTheme.colorScheme.error)
        }
        if (steps > 1000) {
            Text("⚠️ Take it slow", color = MaterialTheme.colorScheme.error)
        }
    }
}
