package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Appointment
import com.example.data.AppointmentRepository
import com.example.data.DoctorProfile
import com.example.voice.SpeechManager
import com.example.voice.VoiceCommand
import com.example.voice.VoiceCommandParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class MainSection(val label: String) {
    APPOINTMENTS("Appointments"),
    DOCTORS("My Doctors")
}

enum class FilterTab(val title: String) {
    UPCOMING("Upcoming"),
    TODAY("Today"),
    ALL("All Visits"),
    PAST("Past Visits")
}

data class UiState(
    val currentSection: MainSection = MainSection.APPOINTMENTS,
    val appointments: List<Appointment> = emptyList(),
    val doctors: List<DoctorProfile> = emptyList(),
    val filterTab: FilterTab = FilterTab.UPCOMING,
    val fontSizeScale: FontSizeScale = FontSizeScale.NORMAL,
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val soundLevel: Float = 0f,
    val voiceFeedbackMessage: String = "",
    val editingAppointment: Appointment? = null,
    val isAddEditOpen: Boolean = false,
    val isVoiceHelpOpen: Boolean = false,
    val activeQuestionsAppointment: Appointment? = null,
    val nextAppointment: Appointment? = null,
    val editingDoctor: DoctorProfile? = null,
    val isAddEditDoctorOpen: Boolean = false
)

class AppointmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppointmentRepository
    val speechManager: SpeechManager

    private val _currentSection = MutableStateFlow(MainSection.APPOINTMENTS)
    private val _filterTab = MutableStateFlow(FilterTab.UPCOMING)
    private val _fontSizeScale = MutableStateFlow(FontSizeScale.NORMAL)
    private val _voiceFeedbackMessage = MutableStateFlow("Tap the microphone or say 'What is my next appointment?'")
    private val _editingAppointment = MutableStateFlow<Appointment?>(null)
    private val _isAddEditOpen = MutableStateFlow(false)
    private val _isVoiceHelpOpen = MutableStateFlow(false)
    private val _activeQuestionsAppointment = MutableStateFlow<Appointment?>(null)
    private val _editingDoctor = MutableStateFlow<DoctorProfile?>(null)
    private val _isAddEditDoctorOpen = MutableStateFlow(false)

    val uiState: StateFlow<UiState>

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = AppointmentRepository(database.appointmentDao(), database.doctorDao())
        speechManager = SpeechManager(application)

        val allAppointmentsFlow = repository.allAppointments
        val allDoctorsFlow = repository.allDoctors
        val isListeningFlow = speechManager.isListening
        val isSpeakingFlow = speechManager.isSpeaking
        val soundLevelFlow = speechManager.soundLevel

        uiState = combine(
            allAppointmentsFlow,
            allDoctorsFlow,
            _currentSection,
            _filterTab,
            _fontSizeScale,
            isListeningFlow,
            isSpeakingFlow
        ) { args: Array<Any> ->
            @Suppress("UNCHECKED_CAST")
            val appointments = args[0] as List<Appointment>
            @Suppress("UNCHECKED_CAST")
            val doctors = args[1] as List<DoctorProfile>
            val currentSection = args[2] as MainSection
            val filterTab = args[3] as FilterTab
            val fontScale = args[4] as FontSizeScale
            val isListening = args[5] as Boolean
            val isSpeaking = args[6] as Boolean

            val nowMillis = System.currentTimeMillis()
            val filtered = when (filterTab) {
                FilterTab.UPCOMING -> appointments.filter { !it.isCompleted && it.timestampMillis >= nowMillis - 1000 * 60 * 60 * 2 }
                FilterTab.TODAY -> appointments.filter { isSameDay(it.timestampMillis, nowMillis) }
                FilterTab.PAST -> appointments.filter { it.isCompleted || it.timestampMillis < nowMillis - 1000 * 60 * 60 * 24 }
                FilterTab.ALL -> appointments
            }

            val next = appointments.firstOrNull { !it.isCompleted && it.timestampMillis >= nowMillis - 1000 * 60 * 60 * 2 }

            UiState(
                currentSection = currentSection,
                appointments = filtered,
                doctors = doctors,
                filterTab = filterTab,
                fontSizeScale = fontScale,
                isListening = isListening,
                isSpeaking = isSpeaking,
                soundLevel = soundLevelFlow.value,
                voiceFeedbackMessage = _voiceFeedbackMessage.value,
                editingAppointment = _editingAppointment.value,
                isAddEditOpen = _isAddEditOpen.value,
                isVoiceHelpOpen = _isVoiceHelpOpen.value,
                activeQuestionsAppointment = _activeQuestionsAppointment.value,
                nextAppointment = next,
                editingDoctor = _editingDoctor.value,
                isAddEditDoctorOpen = _isAddEditDoctorOpen.value
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState()
        )
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    fun switchSection(section: MainSection) {
        _currentSection.value = section
        val announcement = if (section == MainSection.DOCTORS) "Doctor profiles section." else "Appointments schedule section."
        _voiceFeedbackMessage.value = announcement
    }

    fun setFilterTab(tab: FilterTab) {
        _filterTab.value = tab
    }

    fun cycleFontSize() {
        _fontSizeScale.value = when (_fontSizeScale.value) {
            FontSizeScale.NORMAL -> FontSizeScale.LARGE
            FontSizeScale.LARGE -> FontSizeScale.EXTRA_LARGE
            FontSizeScale.EXTRA_LARGE -> FontSizeScale.NORMAL
        }
        val label = _fontSizeScale.value.label
        _voiceFeedbackMessage.value = "Text size set to $label."
    }

    fun setFontSize(scale: FontSizeScale) {
        _fontSizeScale.value = scale
        _voiceFeedbackMessage.value = "Text size set to ${scale.label}."
    }

    // --- Doctor Profiles Actions ---
    fun openAddDoctor(name: String? = null) {
        _editingDoctor.value = DoctorProfile(
            name = name ?: "",
            specialty = "Primary Care Physician",
            clinicName = "",
            address = "",
            phoneNumber = "",
            notes = ""
        )
        _isAddEditDoctorOpen.value = true
    }

    fun openEditDoctor(doctor: DoctorProfile) {
        _editingDoctor.value = doctor
        _isAddEditDoctorOpen.value = true
    }

    fun closeAddEditDoctor() {
        _isAddEditDoctorOpen.value = false
        _editingDoctor.value = null
    }

    fun saveDoctor(doctor: DoctorProfile) {
        viewModelScope.launch {
            if (doctor.id == 0L) {
                repository.insertDoctor(doctor)
                val msg = "Doctor profile for ${doctor.name} saved."
                _voiceFeedbackMessage.value = msg
                speechManager.speak(msg)
            } else {
                repository.updateDoctor(doctor)
                val msg = "Doctor profile updated."
                _voiceFeedbackMessage.value = msg
                speechManager.speak(msg)
            }
            closeAddEditDoctor()
        }
    }

    fun deleteDoctor(doctor: DoctorProfile) {
        viewModelScope.launch {
            repository.deleteDoctor(doctor)
            val msg = "Doctor profile for ${doctor.name} removed."
            _voiceFeedbackMessage.value = msg
            speechManager.speak(msg)
        }
    }

    fun readDoctorProfile(doctor: DoctorProfile) {
        val notes = if (doctor.notes.isNotBlank()) " Notes: ${doctor.notes}." else ""
        val text = "${doctor.name}, ${doctor.specialty}. Clinic: ${doctor.clinicName}, ${doctor.address}. Phone number: ${doctor.phoneNumber}.$notes"
        _voiceFeedbackMessage.value = text
        speechManager.speak(text)
    }

    /**
     * Schedules a visit directly pre-associated with a specific DoctorProfile!
     */
    fun openScheduleVisitForDoctor(doctor: DoctorProfile) {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
        }
        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        _editingAppointment.value = Appointment(
            doctorId = doctor.id,
            doctorName = doctor.name,
            specialty = doctor.specialty,
            clinicName = doctor.clinicName,
            address = doctor.address,
            phoneNumber = doctor.phoneNumber,
            dateText = dateFormat.format(cal.time),
            timeText = "10:00 AM",
            timestampMillis = cal.timeInMillis,
            notes = if (doctor.notes.isNotBlank()) "From doctor profile: ${doctor.notes}" else "",
            requiresFasting = false,
            bringMedicationList = true,
            needRideOrCaregiver = false,
            questionsForDoctor = ""
        )
        _isAddEditOpen.value = true
    }

    // --- Appointment Actions ---
    fun openAddAppointment(doctorName: String? = null, specialty: String? = null) {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
        }
        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        _editingAppointment.value = Appointment(
            doctorName = doctorName ?: "",
            specialty = specialty ?: "Primary Care Physician",
            clinicName = "",
            address = "",
            phoneNumber = "",
            dateText = dateFormat.format(cal.time),
            timeText = "10:00 AM",
            timestampMillis = cal.timeInMillis,
            notes = "",
            requiresFasting = false,
            bringMedicationList = true,
            needRideOrCaregiver = false,
            questionsForDoctor = ""
        )
        _isAddEditOpen.value = true
    }

    fun openEditAppointment(appointment: Appointment) {
        _editingAppointment.value = appointment
        _isAddEditOpen.value = true
    }

    fun closeAddEdit() {
        _isAddEditOpen.value = false
        _editingAppointment.value = null
    }

    fun openVoiceHelp() {
        _isVoiceHelpOpen.value = true
    }

    fun closeVoiceHelp() {
        _isVoiceHelpOpen.value = false
    }

    fun openQuestions(appointment: Appointment) {
        _activeQuestionsAppointment.value = appointment
    }

    fun closeQuestions() {
        _activeQuestionsAppointment.value = null
    }

    fun saveAppointment(appointment: Appointment) {
        viewModelScope.launch {
            if (appointment.id == 0L) {
                repository.insert(appointment)
                val msg = "Appointment with ${appointment.doctorName} saved."
                _voiceFeedbackMessage.value = msg
                speechManager.speak(msg)
            } else {
                repository.update(appointment)
                val msg = "Appointment updated."
                _voiceFeedbackMessage.value = msg
                speechManager.speak(msg)
            }
            closeAddEdit()
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.delete(appointment)
            val msg = "Appointment deleted."
            _voiceFeedbackMessage.value = msg
            speechManager.speak(msg)
        }
    }

    fun toggleCompleted(appointment: Appointment) {
        viewModelScope.launch {
            val newState = !appointment.isCompleted
            repository.toggleCompleted(appointment.id, newState)
            val msg = if (newState) "Visit with ${appointment.doctorName} marked as completed." else "Visit marked as upcoming."
            _voiceFeedbackMessage.value = msg
            speechManager.speak(msg)
        }
    }

    fun readNextAppointment() {
        viewModelScope.launch {
            val next = repository.getNextUpcoming()
            if (next != null) {
                val fastingText = if (next.requiresFasting) " Reminder: Fasting is required before this visit." else ""
                val medsText = if (next.bringMedicationList) " Please remember your list of current medications." else ""
                val spoken = "Your next appointment is with ${next.doctorName}, ${next.specialty}, on ${next.dateText} at ${next.timeText}, at ${next.clinicName}. $fastingText$medsText"
                _voiceFeedbackMessage.value = spoken
                speechManager.speak(spoken)
            } else {
                val none = "You have no upcoming doctor appointments scheduled."
                _voiceFeedbackMessage.value = none
                speechManager.speak(none)
            }
        }
    }

    fun readTodayAppointments() {
        viewModelScope.launch {
            val all = repository.getUpcomingList()
            val nowMillis = System.currentTimeMillis()
            val todayList = all.filter { isSameDay(it.timestampMillis, nowMillis) }
            if (todayList.isEmpty()) {
                val text = "You have no doctor appointments scheduled for today. Have a wonderful and relaxing day!"
                _voiceFeedbackMessage.value = text
                speechManager.speak(text)
            } else {
                val countText = if (todayList.size == 1) "You have 1 appointment today." else "You have ${todayList.size} appointments today."
                val details = todayList.joinToString(". ") { appt ->
                    "${appt.doctorName} at ${appt.timeText} at ${appt.clinicName}"
                }
                val spoken = "$countText $details"
                _voiceFeedbackMessage.value = spoken
                speechManager.speak(spoken)
            }
        }
    }

    fun readAllUpcoming() {
        viewModelScope.launch {
            val list = repository.getUpcomingList()
            if (list.isEmpty()) {
                val none = "You have no upcoming appointments in your schedule."
                _voiceFeedbackMessage.value = none
                speechManager.speak(none)
            } else {
                val summary = StringBuilder("You have ${list.size} upcoming appointments. ")
                list.forEachIndexed { index, appt ->
                    summary.append("Number ${index + 1}: ${appt.doctorName}, on ${appt.dateText} at ${appt.timeText}. ")
                }
                val spoken = summary.toString()
                _voiceFeedbackMessage.value = spoken
                speechManager.speak(spoken)
            }
        }
    }

    fun readAppointment(appointment: Appointment) {
        val fasting = if (appointment.requiresFasting) " Please note, fasting is required for tests." else ""
        val meds = if (appointment.bringMedicationList) " Bring your current medicine list." else ""
        val ride = if (appointment.needRideOrCaregiver) " Remember to arrange a ride or ask a caregiver." else ""
        val notes = if (appointment.notes.isNotBlank()) " Notes: ${appointment.notes}." else ""
        val text = "Appointment with ${appointment.doctorName}, ${appointment.specialty}, on ${appointment.dateText} at ${appointment.timeText}. Clinic: ${appointment.clinicName}, ${appointment.address}.$fasting$meds$ride$notes"
        _voiceFeedbackMessage.value = text
        speechManager.speak(text)
    }

    fun readQuestions(appointment: Appointment) {
        if (appointment.questionsForDoctor.isBlank()) {
            val text = "No questions have been noted yet for ${appointment.doctorName}."
            _voiceFeedbackMessage.value = text
            speechManager.speak(text)
        } else {
            val text = "Here are your questions for ${appointment.doctorName}: ${appointment.questionsForDoctor.replace("\n", ". ")}"
            _voiceFeedbackMessage.value = text
            speechManager.speak(text)
        }
    }

    fun stopSpeaking() {
        speechManager.stopSpeaking()
        _voiceFeedbackMessage.value = "Speech stopped."
    }

    fun startVoiceListening(context: Context) {
        speechManager.startListening(
            onResult = { recognizedText ->
                processVoiceCommand(recognizedText, context)
            },
            onError = { errorMsg ->
                _voiceFeedbackMessage.value = errorMsg
                speechManager.speak("I didn't quite catch that. Please tap the button and speak again.")
            }
        )
    }

    fun stopVoiceListening() {
        speechManager.stopListening()
    }

    fun processVoiceCommand(spokenText: String, context: Context) {
        _voiceFeedbackMessage.value = "Heard: \"$spokenText\""
        when (val command = VoiceCommandParser.parse(spokenText)) {
            VoiceCommand.ShowDoctors -> {
                switchSection(MainSection.DOCTORS)
                speechManager.speak("Showing your doctor profiles.")
            }
            VoiceCommand.ShowAppointments -> {
                switchSection(MainSection.APPOINTMENTS)
                speechManager.speak("Showing your appointments schedule.")
            }
            is VoiceCommand.AddNewDoctor -> {
                switchSection(MainSection.DOCTORS)
                openAddDoctor(command.doctorName)
                speechManager.speak("Opening new doctor profile form.")
            }
            is VoiceCommand.ReadDoctorProfile -> {
                viewModelScope.launch {
                    val docs = repository.getAllDoctorsList()
                    val target = if (!command.doctorQuery.isNullOrBlank()) {
                        docs.firstOrNull { it.name.contains(command.doctorQuery, ignoreCase = true) || it.specialty.contains(command.doctorQuery, ignoreCase = true) }
                            ?: docs.firstOrNull()
                    } else {
                        docs.firstOrNull()
                    }
                    if (target != null) {
                        readDoctorProfile(target)
                    } else {
                        speechManager.speak("No matching doctor profile found.")
                    }
                }
            }
            VoiceCommand.ReadNextAppointment -> {
                switchSection(MainSection.APPOINTMENTS)
                readNextAppointment()
            }
            VoiceCommand.ReadTodayAppointments -> {
                switchSection(MainSection.APPOINTMENTS)
                _filterTab.value = FilterTab.TODAY
                readTodayAppointments()
            }
            VoiceCommand.ReadAllAppointments -> {
                switchSection(MainSection.APPOINTMENTS)
                _filterTab.value = FilterTab.UPCOMING
                readAllUpcoming()
            }
            is VoiceCommand.CallDoctor -> {
                callDoctor(command.doctorQuery, context)
            }
            is VoiceCommand.AddNewAppointment -> {
                switchSection(MainSection.APPOINTMENTS)
                openAddAppointment(command.doctorName, command.specialty)
                val msg = if (command.doctorName != null) {
                    "Opening appointment form for ${command.doctorName}."
                } else {
                    "Opening new appointment form."
                }
                _voiceFeedbackMessage.value = msg
                speechManager.speak(msg)
            }
            is VoiceCommand.ReadQuestions -> {
                viewModelScope.launch {
                    val appts = repository.getUpcomingList()
                    val target = if (command.doctorQuery != null) {
                        appts.firstOrNull { it.doctorName.contains(command.doctorQuery, ignoreCase = true) } ?: appts.firstOrNull()
                    } else {
                        appts.firstOrNull()
                    }
                    if (target != null) {
                        readQuestions(target)
                    } else {
                        speechManager.speak("No upcoming appointments found to read questions for.")
                    }
                }
            }
            VoiceCommand.MakeTextBigger -> {
                _fontSizeScale.value = FontSizeScale.LARGE
                val msg = "Text made bigger for easier reading."
                _voiceFeedbackMessage.value = msg
                speechManager.speak(msg)
            }
            VoiceCommand.MakeTextSmaller -> {
                _fontSizeScale.value = FontSizeScale.NORMAL
                val msg = "Text set to standard size."
                _voiceFeedbackMessage.value = msg
                speechManager.speak(msg)
            }
            VoiceCommand.StopSpeaking -> {
                stopSpeaking()
            }
            VoiceCommand.Help -> {
                openVoiceHelp()
                val helpMsg = "You can ask: What is my next appointment? Show my doctors. Call my doctor. Or make text bigger."
                _voiceFeedbackMessage.value = helpMsg
                speechManager.speak(helpMsg)
            }
            is VoiceCommand.Unknown -> {
                val msg = "I heard: \"$spokenText\". You can ask: What is my next appointment, Show my doctors, or tap Help."
                _voiceFeedbackMessage.value = msg
                speechManager.speak(msg)
            }
        }
    }

    private fun callDoctor(doctorQuery: String?, context: Context) {
        viewModelScope.launch {
            // First search in doctor profiles
            val docs = repository.getAllDoctorsList()
            val matchedDoc = if (!doctorQuery.isNullOrBlank()) {
                docs.firstOrNull { it.name.contains(doctorQuery, ignoreCase = true) || it.specialty.contains(doctorQuery, ignoreCase = true) }
            } else null

            if (matchedDoc != null && matchedDoc.phoneNumber.isNotBlank()) {
                callDirect(matchedDoc.phoneNumber, matchedDoc.name, context)
                return@launch
            }

            // Fallback to appointments list
            val appts = repository.getUpcomingList()
            val target = if (!doctorQuery.isNullOrBlank()) {
                appts.firstOrNull { it.doctorName.contains(doctorQuery, ignoreCase = true) || it.specialty.contains(doctorQuery, ignoreCase = true) }
                    ?: docs.firstOrNull()?.let { null }
            } else {
                appts.firstOrNull()
            }

            if (target != null && target.phoneNumber.isNotBlank()) {
                callDirect(target.phoneNumber, target.doctorName, context)
            } else if (docs.isNotEmpty() && docs.first().phoneNumber.isNotBlank()) {
                callDirect(docs.first().phoneNumber, docs.first().name, context)
            } else {
                val msg = "No phone number found for that doctor."
                _voiceFeedbackMessage.value = msg
                speechManager.speak(msg)
            }
        }
    }

    fun callDirect(phoneNumber: String, doctorName: String, context: Context) {
        speechManager.speak("Calling $doctorName.")
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun openDirections(address: String, context: Context) {
        val geoUri = Uri.parse("geo:0,0?q=" + Uri.encode(address))
        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(mapIntent)
        } catch (_: Exception) {
            _voiceFeedbackMessage.value = "Address: $address"
            speechManager.speak("Address is: $address")
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
    }
}
