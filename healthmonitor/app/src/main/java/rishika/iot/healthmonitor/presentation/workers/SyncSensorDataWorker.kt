package rishika.iot.healthmonitor.presentation.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import rishika.iot.healthmonitor.presentation.data.SensorDatabase
import rishika.iot.healthmonitor.presentation.data.SensorData
import rishika.iot.healthmonitor.presentation.mqtt.MqttService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncSensorDataWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val sensorDao = SensorDatabase.getDatabase(context).sensorDao()
    private val mqttService = MqttService()

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch all unsynced data
                val unsyncedData = sensorDao.getAll()

                // If there is data, try to sync it
                for (sensorData in unsyncedData) {
                    try {
                        // Publish data to MQTT
                        mqttService.publish("wearos/${sensorData.type}", """{"value": ${sensorData.value}}""")
                        // After successful publish, remove or mark it as synced
                        // (Here we can update the database to mark data as synced)
                        sensorDao.insert(sensorData) // You can add a column `isSynced` to track synced data
                    } catch (e: Exception) {
                        // If failed to publish, leave the data for retry
                        return@withContext Result.retry()
                    }
                }
                // Successfully synced all data
                Result.success()
            } catch (e: Exception) {
                // Error in fetching data or publishing to MQTT
                Result.failure()
            }
        }
    }
}
