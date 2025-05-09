package rishika.iot.healthmonitor.presentation.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_data")
data class SensorData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "heart_rate" or "steps"
    val value: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false // Add a flag to mark the sync status
)
