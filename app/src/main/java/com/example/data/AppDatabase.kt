package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(entities = [Appointment::class, DoctorProfile::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appointmentDao(): AppointmentDao
    abstract fun doctorDao(): DoctorDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "doccare_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.doctorDao(), database.appointmentDao())
                    }
                }
            }

            private suspend fun populateInitialData(doctorDao: DoctorDao, appointmentDao: AppointmentDao) {
                val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())

                // 1. Initial Doctors
                val doc1 = DoctorProfile(
                    name = "Dr. Robert Chen",
                    specialty = "Cardiologist (Heart Doctor)",
                    clinicName = "Memorial Health Heart Institute",
                    address = "450 Mercy Drive, Suite 204",
                    phoneNumber = "555-234-8899",
                    notes = "Prefers morning visits. Request electrocardiogram results beforehand. Takes Medicare Part B."
                )
                val doc2 = DoctorProfile(
                    name = "Dr. Eleanor Vance",
                    specialty = "Eye Specialist (Ophthalmologist)",
                    clinicName = "ClearVision Eye Clinic",
                    address = "820 Pine Street, 1st Floor",
                    phoneNumber = "555-876-1234",
                    notes = "Dilation drops used for exam. Patient requires sunglasses and ride home after visits."
                )
                val doc3 = DoctorProfile(
                    name = "Dr. James Miller",
                    specialty = "Primary Care Physician",
                    clinicName = "St. Luke Senior Wellness Clinic",
                    address = "1200 Washington Blvd, Room 102",
                    phoneNumber = "555-432-1000",
                    notes = "Family doctor for general health, annual wellness reviews, and flu/pneumonia vaccines."
                )

                val docIds = doctorDao.insertAll(listOf(doc1, doc2, doc3))
                val doc1Id = docIds.getOrNull(0) ?: 1L
                val doc2Id = docIds.getOrNull(1) ?: 2L
                val doc3Id = docIds.getOrNull(2) ?: 3L

                // 1. Tomorrow morning appointment
                val tomorrow = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 30)
                    set(Calendar.SECOND, 0)
                }

                // 2. Appointment 3 days from now
                val inThreeDays = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 3)
                    set(Calendar.HOUR_OF_DAY, 14)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }

                // 3. Appointment next week
                val nextWeek = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 7)
                    set(Calendar.HOUR_OF_DAY, 11)
                    set(Calendar.MINUTE, 15)
                    set(Calendar.SECOND, 0)
                }

                val initialAppointments = listOf(
                    Appointment(
                        doctorId = doc1Id,
                        doctorName = doc1.name,
                        specialty = doc1.specialty,
                        clinicName = doc1.clinicName,
                        address = doc1.address,
                        phoneNumber = doc1.phoneNumber,
                        dateText = dateFormat.format(tomorrow.time),
                        timeText = "9:30 AM",
                        timestampMillis = tomorrow.timeInMillis,
                        notes = "Routine blood pressure checkup and review of heart medication.",
                        requiresFasting = true,
                        bringMedicationList = true,
                        needRideOrCaregiver = false,
                        questionsForDoctor = "Ask if I should take blood pressure pill with breakfast.\nMention slight ankle swelling in the evening."
                    ),
                    Appointment(
                        doctorId = doc2Id,
                        doctorName = doc2.name,
                        specialty = doc2.specialty,
                        clinicName = doc2.clinicName,
                        address = doc2.address,
                        phoneNumber = doc2.phoneNumber,
                        dateText = dateFormat.format(inThreeDays.time),
                        timeText = "2:00 PM",
                        timestampMillis = inThreeDays.timeInMillis,
                        notes = "Annual vision exam and glaucoma pressure check. Eyes will be dilated, bring sunglasses.",
                        requiresFasting = false,
                        bringMedicationList = false,
                        needRideOrCaregiver = true,
                        questionsForDoctor = "Ask for prescription renewal for eye drops.\nCheck reading glasses power."
                    ),
                    Appointment(
                        doctorId = doc3Id,
                        doctorName = doc3.name,
                        specialty = doc3.specialty,
                        clinicName = doc3.clinicName,
                        address = doc3.address,
                        phoneNumber = doc3.phoneNumber,
                        dateText = dateFormat.format(nextWeek.time),
                        timeText = "11:15 AM",
                        timestampMillis = nextWeek.timeInMillis,
                        notes = "Semi-annual wellness visit and flu vaccine.",
                        requiresFasting = false,
                        bringMedicationList = true,
                        needRideOrCaregiver = false,
                        questionsForDoctor = "Ask about seasonal flu and shingles vaccines.\nDiscuss knee stiffness."
                    )
                )

                appointmentDao.insertAll(initialAppointments)
            }
        }
    }
}
