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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
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
import com.example.data.DoctorProfile
import com.example.ui.LocalFontSizeScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDoctorDialog(
    doctor: DoctorProfile?,
    onDismiss: () -> Unit,
    onSave: (DoctorProfile) -> Unit
) {
    val scale = LocalFontSizeScale.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(doctor?.name ?: "") }
    var specialty by remember { mutableStateOf(doctor?.specialty ?: "Primary Care Physician") }
    var clinicName by remember { mutableStateOf(doctor?.clinicName ?: "") }
    var address by remember { mutableStateOf(doctor?.address ?: "") }
    var phoneNumber by remember { mutableStateOf(doctor?.phoneNumber ?: "") }
    var notes by remember { mutableStateOf(doctor?.notes ?: "") }

    val isEditing = doctor != null && doctor.id != 0L

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
        modifier = Modifier.testTag("add_edit_doctor_dialog")
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
                    text = if (isEditing) "Edit Doctor Profile" else "New Doctor Profile",
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

            Spacer(modifier = Modifier.height(16.dp))

            // Doctor Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Doctor Name (e.g. Dr. Sarah Jenkins)", fontSize = scale.scale(16.sp)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_doctor_profile_name"),
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

            // Address
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Clinic Address & Suite #", fontSize = scale.scale(15.sp)) },
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

            Spacer(modifier = Modifier.height(14.dp))

            // Relevant Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Relevant Notes (e.g. office hours, Medicare, preferred day)", fontSize = scale.scale(15.sp)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = scale.scale(16.sp)),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Giant Save Button
            ElevatedButton(
                onClick = {
                    val finalName = if (name.isBlank()) "Doctor" else name.trim()
                    val profile = (doctor ?: DoctorProfile(
                        name = finalName,
                        specialty = specialty,
                        clinicName = clinicName,
                        address = address,
                        phoneNumber = phoneNumber,
                        notes = notes
                    )).copy(
                        name = finalName,
                        specialty = specialty,
                        clinicName = clinicName,
                        address = address,
                        phoneNumber = phoneNumber,
                        notes = notes
                    )
                    onSave(profile)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("save_doctor_profile_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "SAVE DOCTOR PROFILE",
                    fontSize = scale.scale(18.sp),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
