package com.example.data

import kotlinx.coroutines.flow.Flow

class AppointmentRepository(
    private val appointmentDao: AppointmentDao,
    private val doctorDao: DoctorDao
) {

    val allAppointments: Flow<List<Appointment>> = appointmentDao.getAllAppointments()
    val allDoctors: Flow<List<DoctorProfile>> = doctorDao.getAllDoctors()

    fun getUpcomingAppointments(currentMillis: Long): Flow<List<Appointment>> {
        return appointmentDao.getUpcomingAppointments(currentMillis)
    }

    fun getAppointmentById(id: Long): Flow<Appointment?> {
        return appointmentDao.getAppointmentById(id)
    }

    fun getAppointmentsForDoctor(doctorId: Long): Flow<List<Appointment>> {
        return appointmentDao.getAppointmentsForDoctor(doctorId)
    }

    suspend fun getNextUpcoming(): Appointment? {
        return appointmentDao.getNextUpcomingAppointment()
    }

    suspend fun getUpcomingList(): List<Appointment> {
        return appointmentDao.getUpcomingAppointmentsList()
    }

    suspend fun getAllDoctorsList(): List<DoctorProfile> {
        return doctorDao.getAllDoctorsList()
    }

    suspend fun findDoctor(query: String): DoctorProfile? {
        return doctorDao.findDoctorByNameOrSpecialty(query)
    }

    suspend fun insert(appointment: Appointment): Long {
        return appointmentDao.insert(appointment)
    }

    suspend fun update(appointment: Appointment) {
        appointmentDao.update(appointment)
    }

    suspend fun delete(appointment: Appointment) {
        appointmentDao.delete(appointment)
    }

    suspend fun toggleCompleted(id: Long, completed: Boolean) {
        appointmentDao.setCompleted(id, completed)
    }

    suspend fun insertDoctor(doctor: DoctorProfile): Long {
        return doctorDao.insert(doctor)
    }

    suspend fun updateDoctor(doctor: DoctorProfile) {
        doctorDao.update(doctor)
    }

    suspend fun deleteDoctor(doctor: DoctorProfile) {
        doctorDao.delete(doctor)
    }
}
