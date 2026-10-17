package com.example.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.components.AddEditAppointmentDialog
import com.example.ui.components.AddEditDoctorDialog
import com.example.ui.components.AppointmentCard
import com.example.ui.components.CaregiverCard
import com.example.ui.components.DoctorCard
import com.example.ui.components.QuestionsDialog
import com.example.ui.components.SeniorTopAppBar
import com.example.ui.components.VoiceCommandBanner
import com.example.ui.components.VoiceHelpDialog
import java.util.Locale

@Composable
fun DoctorAppointmentsScreen(
    viewModel: AppointmentViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Standard Android Speech Recognition intent launcher fallback
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                viewModel.processVoiceCommand(spoken, context)
            }
        }
    }

    // Audio permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceListening(context)
        } else {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a command (e.g. 'What is my next appointment?' or 'Show my doctors')")
            }
            try {
                speechRecognizerLauncher.launch(intent)
            } catch (_: Exception) {
                viewModel.processVoiceCommand("Help", context)
            }
        }
    }

    fun handleStartListening() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startVoiceListening(context)
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    CompositionLocalProvider(LocalFontSizeScale provides uiState.fontSizeScale) {
        val scale = uiState.fontSizeScale

        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                SeniorTopAppBar(
                    fontSizeScale = uiState.fontSizeScale,
                    isSpeaking = uiState.isSpeaking,
                    onCycleFontSize = { viewModel.cycleFontSize() },
                    onStopSpeaking = { viewModel.stopSpeaking() },
                    onOpenVoiceHelp = { viewModel.openVoiceHelp() },
                    onReadSchedule = { viewModel.readNextAppointment() }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (uiState.currentSection == MainSection.DOCTORS) {
                            viewModel.openAddDoctor()
                        } else {
                            viewModel.openAddAppointment()
                        }
                    },
                    modifier = Modifier
                        .height(64.dp)
                        .testTag(if (uiState.currentSection == MainSection.DOCTORS) "add_doctor_fab" else "add_appointment_fab"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.currentSection == MainSection.DOCTORS) "NEW DOCTOR" else "NEW APPOINTMENT",
                        fontSize = scale.scale(16.sp),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section Navigation Tabs (Appointments vs My Doctors)
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Appointments Section Tab
                            val isAppts = uiState.currentSection == MainSection.APPOINTMENTS
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isAppts) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.switchSection(MainSection.APPOINTMENTS) }
                                    .testTag("tab_appointments_section")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = if (isAppts) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Visits (${uiState.appointments.size})",
                                        fontSize = scale.scale(16.sp),
                                        fontWeight = if (isAppts) FontWeight.ExtraBold else FontWeight.Bold,
                                        color = if (isAppts) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Doctors Section Tab
                            val isDoctors = uiState.currentSection == MainSection.DOCTORS
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDoctors) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.switchSection(MainSection.DOCTORS) }
                                    .testTag("tab_doctors_section")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = null,
                                        tint = if (isDoctors) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "My Doctors (${uiState.doctors.size})",
                                        fontSize = scale.scale(16.sp),
                                        fontWeight = if (isDoctors) FontWeight.ExtraBold else FontWeight.Bold,
                                        color = if (isDoctors) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Voice Command Centerpiece (Available in both sections)
                item {
                    VoiceCommandBanner(
                        isListening = uiState.isListening,
                        soundLevel = uiState.soundLevel,
                        feedbackMessage = uiState.voiceFeedbackMessage,
                        onStartListening = { handleStartListening() },
                        onStopListening = { viewModel.stopVoiceListening() },
                        onExecuteCommandText = { commandText ->
                            viewModel.processVoiceCommand(commandText, context)
                        }
                    )
                }

                // Caregiver / Ride Help Card
                item {
                    CaregiverCard(
                        onCallCaregiver = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:555-987-6543")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    )
                }

                // =========================================================================
                // SECTION: DOCTORS DIRECTORY
                // =========================================================================
                if (uiState.currentSection == MainSection.DOCTORS) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Doctor Profiles",
                                    fontSize = scale.scale(22.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Your healthcare providers and specialists",
                                    fontSize = scale.scale(15.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            ElevatedButton(
                                onClick = { viewModel.openAddDoctor() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ADD", fontWeight = FontWeight.Bold, fontSize = scale.scale(14.sp))
                            }
                        }
                    }

                    if (uiState.doctors.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No Doctor Profiles Saved",
                                        fontSize = scale.scale(20.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Save your doctors' clinic, phone number, and specialty so you can easily schedule visits and call them in one tap.",
                                        fontSize = scale.scale(16.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    ElevatedButton(
                                        onClick = { viewModel.openAddDoctor() },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("ADD FIRST DOCTOR", fontSize = scale.scale(15.sp), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        items(
                            items = uiState.doctors,
                            key = { it.id }
                        ) { doctor ->
                            val associatedUpcoming = uiState.appointments.filter {
                                (it.doctorId == doctor.id || it.doctorName.equals(doctor.name, ignoreCase = true)) && !it.isCompleted
                            }
                            DoctorCard(
                                doctor = doctor,
                                upcomingAppointments = associatedUpcoming,
                                onCallDoctor = { viewModel.callDirect(it.phoneNumber, it.name, context) },
                                onScheduleVisit = { viewModel.openScheduleVisitForDoctor(it) },
                                onReadProfile = { viewModel.readDoctorProfile(it) },
                                onOpenDirections = { viewModel.openDirections(it, context) },
                                onEditDoctor = { viewModel.openEditDoctor(it) },
                                onDeleteDoctor = { viewModel.deleteDoctor(it) }
                            )
                        }
                    }
                }

                // =========================================================================
                // SECTION: APPOINTMENTS
                // =========================================================================
                if (uiState.currentSection == MainSection.APPOINTMENTS) {
                    // Friendly Hero Graphic & Greeting Banner
                    item {
                        HeroGreetingCard(
                            nextAppointmentDoctor = uiState.nextAppointment?.doctorName,
                            nextAppointmentTime = uiState.nextAppointment?.let { "${it.dateText} at ${it.timeText}" },
                            onReadNext = { viewModel.readNextAppointment() },
                            scaleMultiplier = scale.multiplier
                        )
                    }

                    // Filter Tabs (Upcoming, Today, All, Past)
                    item {
                        Column {
                            Text(
                                text = "Filter Visits:",
                                fontSize = scale.scale(16.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val tabScroll = rememberScrollState()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(tabScroll),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilterTab.values().forEach { tab ->
                                    val isSelected = uiState.filterTab == tab
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            2.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { viewModel.setFilterTab(tab) }
                                            .testTag("filter_tab_${tab.name}")
                                    ) {
                                        Text(
                                            text = tab.title,
                                            fontSize = scale.scale(16.sp),
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Empty state if no appointments
                    if (uiState.appointments.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EventNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No ${uiState.filterTab.title} Found",
                                        fontSize = scale.scale(20.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "You can tap '+ New Appointment' or select a doctor from 'My Doctors' to schedule a visit.",
                                        fontSize = scale.scale(16.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    ElevatedButton(
                                        onClick = { viewModel.openAddAppointment() },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("ADD APPOINTMENT", fontSize = scale.scale(15.sp), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        // Appointment Cards
                        items(
                            items = uiState.appointments,
                            key = { it.id }
                        ) { appt ->
                            AppointmentCard(
                                appointment = appt,
                                onReadAloud = { viewModel.readAppointment(it) },
                                onCallDoctor = { viewModel.callDirect(it.phoneNumber, it.doctorName, context) },
                                onOpenDirections = { viewModel.openDirections(it, context) },
                                onOpenQuestions = { viewModel.openQuestions(it) },
                                onToggleCompleted = { viewModel.toggleCompleted(it) },
                                onEdit = { viewModel.openEditAppointment(it) },
                                onDelete = { viewModel.deleteAppointment(it) }
                            )
                        }
                    }
                }
            }
        }

        // Add / Edit Appointment Modal (With Doctor Profiles 1-Tap Fill)
        if (uiState.isAddEditOpen) {
            AddEditAppointmentDialog(
                appointment = uiState.editingAppointment,
                doctors = uiState.doctors,
                onDismiss = { viewModel.closeAddEdit() },
                onSave = { viewModel.saveAppointment(it) }
            )
        }

        // Add / Edit Doctor Profile Modal
        if (uiState.isAddEditDoctorOpen) {
            AddEditDoctorDialog(
                doctor = uiState.editingDoctor,
                onDismiss = { viewModel.closeAddEditDoctor() },
                onSave = { viewModel.saveDoctor(it) }
            )
        }

        // Voice Commands Help Sheet
        if (uiState.isVoiceHelpOpen) {
            VoiceHelpDialog(
                onDismiss = { viewModel.closeVoiceHelp() },
                onExecuteCommand = { cmd -> viewModel.processVoiceCommand(cmd, context) }
            )
        }

        // Questions for Doctor Sheet
        uiState.activeQuestionsAppointment?.let { appt ->
            QuestionsDialog(
                appointment = appt,
                onDismiss = { viewModel.closeQuestions() },
                onReadQuestionsAloud = { viewModel.readQuestions(it) },
                onSaveQuestions = { viewModel.saveAppointment(it) }
            )
        }
    }
}

@Composable
private fun HeroGreetingCard(
    nextAppointmentDoctor: String?,
    nextAppointmentTime: String?,
    onReadNext: () -> Unit,
    scaleMultiplier: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Warm welcoming clinic graphic banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.doctor_hero_banner),
                    contentDescription = "Friendly doctor and senior patient",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Welcome to DocCare",
                    fontSize = (22 * scaleMultiplier).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (nextAppointmentDoctor != null && nextAppointmentTime != null) {
                    Text(
                        text = "Next visit: $nextAppointmentDoctor on $nextAppointmentTime",
                        fontSize = (16 * scaleMultiplier).sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ElevatedButton(
                        onClick = onReadNext,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Hear Next Visit Details",
                            fontSize = (14 * scaleMultiplier).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Manage your doctor visits easily with voice commands and large text.",
                        fontSize = (15 * scaleMultiplier).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
