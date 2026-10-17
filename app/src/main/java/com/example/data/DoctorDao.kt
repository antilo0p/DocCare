package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DoctorDao {
    @Query("SELECT * FROM doctors ORDER BY name ASC")
    fun getAllDoctors(): Flow<List<DoctorProfile>>

    @Query("SELECT * FROM doctors ORDER BY name ASC")
    suspend fun getAllDoctorsList(): List<DoctorProfile>

    @Query("SELECT * FROM doctors WHERE id = :id LIMIT 1")
    fun getDoctorById(id: Long): Flow<DoctorProfile?>

    @Query("SELECT * FROM doctors WHERE id = :id LIMIT 1")
    suspend fun getDoctorByIdSync(id: Long): DoctorProfile?

    @Query("SELECT * FROM doctors WHERE name LIKE '%' || :query || '%' OR specialty LIKE '%' || :query || '%' LIMIT 1")
    suspend fun findDoctorByNameOrSpecialty(query: String): DoctorProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(doctor: DoctorProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(doctors: List<DoctorProfile>): List<Long>

    @Update
    suspend fun update(doctor: DoctorProfile)

    @Delete
    suspend fun delete(doctor: DoctorProfile)
}
