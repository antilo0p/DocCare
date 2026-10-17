package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY isCompleted ASC, timestampMillis ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE isCompleted = 0 AND timestampMillis >= :currentTimeMillis ORDER BY timestampMillis ASC")
    fun getUpcomingAppointments(currentTimeMillis: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    fun getAppointmentById(id: Long): Flow<Appointment?>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId ORDER BY isCompleted ASC, timestampMillis ASC")
    fun getAppointmentsForDoctor(doctorId: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE isCompleted = 0 ORDER BY timestampMillis ASC LIMIT 1")
    suspend fun getNextUpcomingAppointment(): Appointment?

    @Query("SELECT * FROM appointments WHERE isCompleted = 0 ORDER BY timestampMillis ASC")
    suspend fun getUpcomingAppointmentsList(): List<Appointment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: Appointment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appointments: List<Appointment>)

    @Update
    suspend fun update(appointment: Appointment)

    @Delete
    suspend fun delete(appointment: Appointment)

    @Query("UPDATE appointments SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)
}
