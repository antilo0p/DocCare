package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Appointment
import com.example.ui.LocalFontSizeScale

@Composable
fun AppointmentCard(
    appointment: Appointment,
    onReadAloud: (Appointment) -> Unit,
    onCallDoctor: (Appointment) -> Unit,
    onOpenDirections: (String) -> Unit,
    onOpenQuestions: (Appointment) -> Unit,
    onToggleCompleted: (Appointment) -> Unit,
    onEdit: (Appointment) -> Unit,
    onDelete: (Appointment) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = LocalFontSizeScale.current

    val cardBorderColor = if (appointment.isCompleted) {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.primary
    }

    val headerBgColor = if (appointment.isCompleted) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val headerTextColor = if (appointment.isCompleted) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("appointment_card_${appointment.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(2.dp, cardBorderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Date & Time in High Contrast
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBgColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "📅 ${appointment.dateText} at ${appointment.timeText}",
                        fontSize = scale.scale(18.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = headerTextColor
                    )
                }

                if (appointment.isCompleted) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "COMPLETED",
                            fontSize = scale.scale(12.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Doctor details body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Doctor Name
                Text(
                    text = appointment.doctorName,
                    fontSize = scale.scale(24.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Specialty Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = appointment.specialty,
                        fontSize = scale.scale(16.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Clinic & Address
                if (appointment.clinicName.isNotBlank() || appointment.address.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            if (appointment.clinicName.isNotBlank()) {
                                Text(
                                    text = appointment.clinicName,
                                    fontSize = scale.scale(17.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (appointment.address.isNotBlank()) {
                                Text(
                                    text = appointment.address,
                                    fontSize = scale.scale(15.sp),
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (appointment.address.isNotBlank()) {
                            OutlinedButton(
                                onClick = { onOpenDirections(appointment.address) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Directions,
                                    contentDescription = "Map directions",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Map", fontSize = scale.scale(13.sp), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Senior Care Reminders (Fasting, Meds list, Ride)
                if (appointment.requiresFasting || appointment.bringMedicationList || appointment.needRideOrCaregiver) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (appointment.requiresFasting) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFECE5),
                                border = BorderStroke(1.5.dp, Color(0xFFD84315)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Fasting alert",
                                        tint = Color(0xFFD84315),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "FASTING REQUIRED (No food 8 hours prior)",
                                        fontSize = scale.scale(14.sp),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFD84315)
                                    )
                                }
                            }
                        }

                        if (appointment.bringMedicationList) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFE0F2F1),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Medication,
                                        contentDescription = "Meds alert",
                                        tint = Color(0xFF00695C),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Bring current medication bottles/list",
                                        fontSize = scale.scale(14.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00695C)
                                    )
                                }
                            }
                        }

                        if (appointment.needRideOrCaregiver) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFE3F2FD),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🚗 Need ride or companion for dilation/tests",
                                        fontSize = scale.scale(14.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0D47A1)
                                    )
                                }
                            }
                        }
                    }
                }

                // Notes
                if (appointment.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Notes: ${appointment.notes}",
                        fontSize = scale.scale(15.sp),
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Two BIG Main Buttons for Seniors: "READ TO ME" and "CALL DOCTOR"
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Read Aloud Button (Min 54dp height)
                    ElevatedButton(
                        onClick = { onReadAloud(appointment) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("read_aloud_button_${appointment.id}"),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "READ TO ME",
                            fontSize = scale.scale(16.sp),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Call Doctor Button (Min 54dp height)
                    ElevatedButton(
                        onClick = { onCallDoctor(appointment) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("call_doctor_button_${appointment.id}"),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CALL",
                            fontSize = scale.scale(16.sp),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Secondary row: Questions, Toggle Done, Edit, Delete
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Questions button
                    FilledTonalButton(
                        onClick = { onOpenQuestions(appointment) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("questions_button_${appointment.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QuestionAnswer,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val questionCount = if (appointment.questionsForDoctor.isBlank()) 0
                        else appointment.questionsForDoctor.lines().filter { it.isNotBlank() }.size
                        Text(
                            text = "Questions ($questionCount)",
                            fontSize = scale.scale(14.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Mark Completed Toggle
                        IconButton(
                            onClick = { onToggleCompleted(appointment) },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (appointment.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (appointment.isCompleted) "Mark Upcoming" else "Mark Done",
                                tint = if (appointment.isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Edit Button
                        IconButton(
                            onClick = { onEdit(appointment) },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit appointment",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Delete Button
                        IconButton(
                            onClick = { onDelete(appointment) },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete appointment",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
