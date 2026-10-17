package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Doctor Profile entity holding comprehensive physician and clinic contact information.
 */
@Entity(tableName = "doctors")
data class DoctorProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val specialty: String,
    val clinicName: String,
    val address: String,
    val phoneNumber: String,
    val notes: String = ""
)
