package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LocalFontSizeScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceHelpDialog(
    onDismiss: () -> Unit,
    onExecuteCommand: (String) -> Unit
) {
    val scale = LocalFontSizeScale.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("voice_help_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Voice Commands Guide",
                        fontSize = scale.scale(22.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Tap the big microphone button anytime, or tap any example below to try it out immediately:",
                fontSize = scale.scale(16.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            VoiceCommandExampleCard(
                icon = Icons.Default.VolumeUp,
                title = "Check Next Visit",
                examples = listOf(
                    "What is my next appointment?",
                    "When do I see the doctor?",
                    "Read my schedule"
                ),
                onSelect = { onExecuteCommand(it); onDismiss() },
                scaleMultiplier = scale.multiplier
            )

            Spacer(modifier = Modifier.height(12.dp))

            VoiceCommandExampleCard(
                icon = Icons.Default.Today,
                title = "Check Today's Schedule",
                examples = listOf(
                    "What appointments do I have today?",
                    "Do I have any visits today?"
                ),
                onSelect = { onExecuteCommand(it); onDismiss() },
                scaleMultiplier = scale.multiplier
            )

            Spacer(modifier = Modifier.height(12.dp))

            VoiceCommandExampleCard(
                icon = Icons.Default.Phone,
                title = "Call Your Doctor",
                examples = listOf(
                    "Call Dr. Chen",
                    "Call my heart doctor",
                    "Call my doctor"
                ),
                onSelect = { onExecuteCommand(it); onDismiss() },
                scaleMultiplier = scale.multiplier
            )

            Spacer(modifier = Modifier.height(12.dp))

            VoiceCommandExampleCard(
                icon = Icons.Default.People,
                title = "My Doctors Directory",
                examples = listOf(
                    "Show my doctors",
                    "Add doctor",
                    "Show appointments"
                ),
                onSelect = { onExecuteCommand(it); onDismiss() },
                scaleMultiplier = scale.multiplier
            )

            Spacer(modifier = Modifier.height(12.dp))

            VoiceCommandExampleCard(
                icon = Icons.Default.FormatSize,
                title = "Adjust Screen Text Size",
                examples = listOf(
                    "Make text bigger",
                    "Make text smaller"
                ),
                onSelect = { onExecuteCommand(it); onDismiss() },
                scaleMultiplier = scale.multiplier
            )

            Spacer(modifier = Modifier.height(20.dp))

            ElevatedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "GOT IT, CLOSE GUIDE",
                    fontSize = scale.scale(16.sp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun VoiceCommandExampleCard(
    icon: ImageVector,
    title: String,
    examples: List<String>,
    onSelect: (String) -> Unit,
    scaleMultiplier: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = (18 * scaleMultiplier).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                examples.forEach { phrase ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelect(phrase) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "“$phrase”",
                                fontSize = (15 * scaleMultiplier).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "TAP TO TRY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}
