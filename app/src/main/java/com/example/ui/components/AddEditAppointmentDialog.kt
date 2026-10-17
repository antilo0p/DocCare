package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Appointment
import com.example.data.DoctorProfile
import com.example.ui.LocalFontSizeScale
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAppointmentDialog(
    appointment: Appointment?,
    doctors: List<DoctorProfile> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit
) {
    val scale = LocalFontSizeScale.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedDoctorId by remember { mutableStateOf(appointment?.doctorId) }
    var doctorName by remember { mutableStateOf(appointment?.doctorName ?: "") }
    var specialty by remember { mutableStateOf(appointment?.specialty ?: "Primary Care Physician") }
    var clinicName by remember { mutableStateOf(appointment?.clinicName ?: "") }
    var address by remember { mutableStateOf(appointment?.address ?: "") }
    var phoneNumber by remember { mutableStateOf(appointment?.phoneNumber ?: "") }
    var dateText by remember { mutableStateOf(appointment?.dateText ?: "") }
    var timeText by remember { mutableStateOf(appointment?.timeText ?: "") }
    var notes by remember { mutableStateOf(appointment?.notes ?: "") }
    var requiresFasting by remember { mutableStateOf(appointment?.requiresFasting ?: false) }
    var bringMedicationList by remember { mutableStateOf(appointment?.bringMedicationList ?: true) }
    var needRideOrCaregiver by remember { mutableStateOf(appointment?.needRideOrCaregiver ?: false) }
    var questionsForDoctor by remember { mutableStateOf(appointment?.questionsForDoctor ?: "") }

    val isEditing = appointment != null && appointment.id != 0L

    val specialtiesList = listOf(
        "Primary Care Physician",
        "Cardiologist (Heart)",
        "Eye Specialist (Vision)",
        "Dentist (Teeth)",
        "Orthopedic (Joints)",
        "Dermatologist (Skin)",
        "Neurologist",
        "Ear, Nose & Throat",
        "Podiatrist (Foot Doctor)"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("add_edit_appointment_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isEditing) "Edit Appointment" else "New Doctor Appointment",
                    fontSize = scale.scale(22.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Quick Associate with Saved Doctor Profiles (Senior Friendly 1-Tap Fill)
            if (doctors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Select from My Doctors (1-Tap Auto Fill):",
                    fontSize = scale.scale(16.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                val docScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(docScrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    doctors.forEach { doc ->
                        val isChosen = selectedDoctorId == doc.id || doctorName.equals(doc.name, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isChosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                if (isChosen) 2.dp else 1.dp,
                                if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedDoctorId = doc.id
                                    doctorName = doc.name
                                    specialty = doc.specialty
                                    clinicName = doc.clinicName
                                    address = doc.address
                                    phoneNumber = doc.phoneNumber
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isChosen) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = doc.name,
                                    fontSize = scale.scale(15.sp),
                                    fontWeight = if (isChosen) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = if (isChosen) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Doctor Name Field (Large)
            OutlinedTextField(
                value = doctorName,
                onValueChange = {
                    doctorName = it
                    // Clear selected doctor id if user types a different name
                    val match = doctors.firstOrNull { doc -> doc.name.equals(it.trim(), ignoreCase = true) }
                    selectedDoctorId = match?.id
                },
                label = { Text("Doctor Name (e.g. Dr. Robert Chen)", fontSize = scale.scale(16.sp)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_doctor_name"),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = scale.scale(18.sp),
                    fontWeight = FontWeight.Bold
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Specialty selector chips
            Text(
                text = "Specialty:",
                fontSize = scale.scale(16.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            val specialtyScroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(specialtyScroll),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                specialtiesList.forEach { item ->
                    val isSelected = specialty == item
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { specialty = item }
                    ) {
                        Text(
                            text = item,
                            fontSize = scale.scale(14.sp),
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Date & Time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (e.g. Monday, Nov 2)", fontSize = scale.scale(14.sp)) },
                    modifier = Modifier.weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = scale.scale(16.sp), fontWeight = FontWeight.SemiBold),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = { Text("Time (e.g. 10:30 AM)", fontSize = scale.scale(14.sp)) },
                    modifier = Modifier.weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = scale.scale(16.sp), fontWeight = FontWeight.SemiBold),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Clinic Name
            OutlinedTextField(
                value = clinicName,
                onValueChange = { clinicName = it },
                label = { Text("Clinic or Hospital Name", fontSize = scale.scale(15.sp)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = scale.scale(16.sp)),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Clinic Address
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address / Suite Number", fontSize = scale.scale(15.sp)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = scale.scale(16.sp)),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Phone Number
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number to Call", fontSize = scale.scale(15.sp)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = scale.scale(16.sp), fontWeight = FontWeight.Bold),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Senior Checklists
            Text(
                text = "Appointment Checklists:",
                fontSize = scale.scale(16.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { requiresFasting = !requiresFasting }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(checked = requiresFasting, onCheckedChange = { requiresFasting = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Fasting required (No food or drink)",
                            fontSize = scale.scale(15.sp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { bringMedicationList = !bringMedicationList }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(checked = bringMedicationList, onCheckedChange = { bringMedicationList = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bring current medication list",
                            fontSize = scale.scale(15.sp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { needRideOrCaregiver = !needRideOrCaregiver }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(checked = needRideOrCaregiver, onCheckedChange = { needRideOrCaregiver = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Arrange ride / Caregiver accompaniment",
                            fontSize = scale.scale(15.sp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Questions to Ask Doctor
            OutlinedTextField(
                value = questionsForDoctor,
                onValueChange = { questionsForDoctor = it },
                label = { Text("Questions to ask the doctor (one per line)", fontSize = scale.scale(15.sp)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = scale.scale(16.sp)),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Doctor's instructions or visit notes", fontSize = scale.scale(15.sp)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = scale.scale(16.sp)),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Giant Save Button (60dp tall)
            ElevatedButton(
                onClick = {
                    val finalDate = if (dateText.isBlank()) {
                        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(cal.time)
                    } else dateText

                    val finalTime = if (timeText.isBlank()) "10:00 AM" else timeText
                    val finalDoctor = if (doctorName.isBlank()) "Doctor Visit" else doctorName

                    val newAppt = (appointment ?: Appointment(
                        doctorId = selectedDoctorId,
                        doctorName = finalDoctor,
                        specialty = specialty,
                        clinicName = clinicName,
                        address = address,
                        phoneNumber = phoneNumber,
                        dateText = finalDate,
                        timeText = finalTime,
                        timestampMillis = System.currentTimeMillis() + 86400000L
                    )).copy(
                        doctorId = selectedDoctorId,
                        doctorName = finalDoctor,
                        specialty = specialty,
                        clinicName = clinicName,
                        address = address,
                        phoneNumber = phoneNumber,
                        dateText = finalDate,
                        timeText = finalTime,
                        notes = notes,
                        requiresFasting = requiresFasting,
                        bringMedicationList = bringMedicationList,
                        needRideOrCaregiver = needRideOrCaregiver,
                        questionsForDoctor = questionsForDoctor
                    )
                    onSave(newAppt)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("save_appointment_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "SAVE APPOINTMENT",
                    fontSize = scale.scale(18.sp),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
