package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Senior-friendly appointment entity containing full clinical visit details,
 * preparation reminders (fasting, medication list), and questions for the doctor.
 * Associated with a DoctorProfile via doctorId.
 */
@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val doctorId: Long? = null,
    val doctorName: String,
    val specialty: String,
    val clinicName: String,
    val address: String,
    val phoneNumber: String,
    val dateText: String, // e.g. "Tuesday, Oct 20"
    val timeText: String, // e.g. "10:30 AM"
    val timestampMillis: Long,
    val notes: String = "",
    val requiresFasting: Boolean = false,
    val bringMedicationList: Boolean = true,
    val needRideOrCaregiver: Boolean = false,
    val questionsForDoctor: String = "",
    val isCompleted: Boolean = false
)
