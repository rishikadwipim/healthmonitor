package rishika.iot.healthmonitor.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.work.*
import rishika.iot.healthmonitor.presentation.ui.SensorScreen
import rishika.iot.healthmonitor.presentation.workers.SyncSensorDataWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize WorkManager and trigger sync worker to sync offline data
        triggerSyncWorker()

        setContent {
            MaterialTheme {
                Surface {
                    SensorScreen(applicationContext)
                }
            }
        }
    }

    private fun triggerSyncWorker() {
        // Create a OneTimeWorkRequest for syncing offline data
        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncSensorDataWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // Ensures worker runs only when connected to the internet
                    .build()
            )
            .setInitialDelay(10, TimeUnit.SECONDS)  // Set a delay before starting the sync
            .build()

        // Enqueue the worker
        WorkManager.getInstance(this).enqueue(syncWorkRequest)
    }
}
