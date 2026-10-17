package com.example.voice

import java.util.Locale

object VoiceCommandParser {

    /**
     * Parses spoken text into a high-level actionable VoiceCommand.
     * Robust against fillers like "please", "can you", "um", etc.
     */
    fun parse(text: String): VoiceCommand {
        val clean = text.lowercase(Locale.ROOT).trim()

        if (clean.isBlank()) {
            return VoiceCommand.Unknown("")
        }

        // Silence / Stop
        if (clean.contains("stop") || clean.contains("quiet") || clean.contains("silence") ||
            clean.contains("shut up") || clean.contains("pause")
        ) {
            return VoiceCommand.StopSpeaking
        }

        // Help / Instructions
        if (clean == "help" || clean.contains("what can i say") || clean.contains("how to use") ||
            clean.contains("voice commands") || clean.contains("instructions")
        ) {
            return VoiceCommand.Help
        }

        // Font size commands
        if (clean.contains("bigger text") || clean.contains("make text bigger") ||
            clean.contains("large text") || clean.contains("larger") || clean.contains("zoom in") ||
            clean.contains("increase font") || clean.contains("increase text")
        ) {
            return VoiceCommand.MakeTextBigger
        }
        if (clean.contains("smaller text") || clean.contains("make text smaller") ||
            clean.contains("normal text") || clean.contains("standard text") ||
            clean.contains("decrease font") || clean.contains("reset text")
        ) {
            return VoiceCommand.MakeTextSmaller
        }

        // Show Doctors section
        if (clean.contains("show doctors") || clean.contains("my doctors") ||
            clean.contains("doctors list") || clean.contains("doctor profiles") ||
            clean.contains("who are my doctors") || clean.contains("view doctors")
        ) {
            return VoiceCommand.ShowDoctors
        }

        // Show Appointments section
        if (clean.contains("show appointments") || clean.contains("my visits") ||
            clean.contains("appointment list") || clean.contains("view appointments")
        ) {
            return VoiceCommand.ShowAppointments
        }

        // Add Doctor Profile
        if (clean.contains("add doctor") || clean.contains("new doctor") ||
            clean.contains("create doctor") || clean.contains("save doctor")
        ) {
            val doctor = extractDoctorName(clean)
            return VoiceCommand.AddNewDoctor(doctor)
        }

        // Questions / Checklist
        if (clean.contains("question") || clean.contains("what should i ask") ||
            clean.contains("ask the doctor") || clean.contains("what to ask")
        ) {
            val doctor = extractDoctorName(clean)
            return VoiceCommand.ReadQuestions(doctor)
        }

        // Call Doctor
        if (clean.startsWith("call") || clean.contains("phone") || clean.contains("dial")) {
            val doctor = extractDoctorName(clean)
            return VoiceCommand.CallDoctor(doctor)
        }

        // Add / Schedule new appointment
        if (clean.contains("add appointment") || clean.contains("new appointment") ||
            clean.contains("schedule") || clean.contains("book appointment") ||
            clean.contains("make appointment") || clean.contains("create appointment")
        ) {
            val doctor = extractDoctorName(clean)
            val specialty = extractSpecialty(clean)
            return VoiceCommand.AddNewAppointment(doctor, specialty)
        }

        // Today's appointments
        if (clean.contains("today") || clean.contains("today's") || clean.contains("this morning") ||
            clean.contains("this afternoon")
        ) {
            return VoiceCommand.ReadTodayAppointments
        }

        // Read all / schedule
        if (clean.contains("read all") || clean.contains("all appointments") ||
            clean.contains("everything") || clean.contains("full schedule") ||
            clean.contains("list my appointments")
        ) {
            return VoiceCommand.ReadAllAppointments
        }

        // Doctor details / Read doctor profile
        if (clean.contains("doctor profile") || clean.contains("tell me about") || clean.contains("who is")) {
            val doctor = extractDoctorName(clean)
            return VoiceCommand.ReadDoctorProfile(doctor)
        }

        // Next appointment (default intent when asking "when is my appointment", "doctor visit", etc.)
        if (clean.contains("next") || clean.contains("upcoming") || clean.contains("when") ||
            clean.contains("appointment") || clean.contains("doctor") || clean.contains("visit")
        ) {
            return VoiceCommand.ReadNextAppointment
        }

        return VoiceCommand.Unknown(text)
    }

    private fun extractDoctorName(text: String): String? {
        val drIndex = text.indexOf("dr.")
        val doctorIndex = text.indexOf("doctor")
        val index = when {
            drIndex != -1 -> drIndex
            doctorIndex != -1 -> doctorIndex
            else -> -1
        }
        if (index != -1) {
            val substring = text.substring(index).trim()
            val words = substring.split(" ")
            if (words.size >= 2) {
                // e.g. "dr. chen" or "doctor miller"
                val prefix = words[0].replaceFirstChar { it.uppercase() }
                val name = words[1].replaceFirstChar { it.uppercase() }
                return "$prefix $name"
            }
        }
        return null
    }

    private fun extractSpecialty(text: String): String? {
        return when {
            text.contains("heart") || text.contains("cardio") -> "Cardiologist (Heart Doctor)"
            text.contains("eye") || text.contains("vision") -> "Eye Specialist (Ophthalmologist)"
            text.contains("dentist") || text.contains("teeth") -> "Dentist"
            text.contains("bone") || text.contains("ortho") || text.contains("joint") -> "Orthopedic Specialist"
            text.contains("skin") || text.contains("derma") -> "Dermatologist"
            text.contains("general") || text.contains("primary") -> "Primary Care Physician"
            else -> null
        }
    }
}
