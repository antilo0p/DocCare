package com.example.voice

sealed interface VoiceCommand {
    data object ReadNextAppointment : VoiceCommand
    data object ReadTodayAppointments : VoiceCommand
    data object ReadAllAppointments : VoiceCommand
    data class CallDoctor(val doctorQuery: String? = null) : VoiceCommand
    data class AddNewAppointment(val doctorName: String? = null, val specialty: String? = null) : VoiceCommand
    data class ReadQuestions(val doctorQuery: String? = null) : VoiceCommand
    data object ShowDoctors : VoiceCommand
    data object ShowAppointments : VoiceCommand
    data class AddNewDoctor(val doctorName: String? = null) : VoiceCommand
    data class ReadDoctorProfile(val doctorQuery: String? = null) : VoiceCommand
    data object MakeTextBigger : VoiceCommand
    data object MakeTextSmaller : VoiceCommand
    data object StopSpeaking : VoiceCommand
    data object Help : VoiceCommand
    data class Unknown(val rawText: String) : VoiceCommand
}
