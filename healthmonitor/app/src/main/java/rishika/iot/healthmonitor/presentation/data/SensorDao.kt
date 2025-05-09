package rishika.iot.healthmonitor.presentation.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDao {
    @Insert
    suspend fun insert(sensorData: SensorData)

    @Query("SELECT * FROM sensor_data ORDER BY timestamp DESC")
    suspend fun getAll(): List<SensorData>

    @Query("SELECT * FROM sensor_data ORDER BY timestamp DESC")
    fun getAllSensorData(): Flow<List<SensorData>>

    @Query("DELETE FROM sensor_data") // Add this
    suspend fun clearAll()
}
