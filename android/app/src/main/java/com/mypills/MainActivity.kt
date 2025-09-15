package com.mypills

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    
    private val viewModel: PillViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PillReminderTheme {
                PillReminderApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PillReminderApp(viewModel: PillViewModel) {
    val pills by viewModel.pills.collectAsStateWithLifecycle()
    val currentTime by viewModel.currentTime.collectAsStateWithLifecycle()
    val showAddDialog by viewModel.showAddPillDialog.collectAsStateWithLifecycle()
    val showNotificationDialog by viewModel.showNotificationDialog.collectAsStateWithLifecycle()
    val activeNotificationPill by viewModel.activeNotificationPill.collectAsStateWithLifecycle()
    
    val currentDate = remember {
        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        dateFormat.format(Date())
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            HeaderSection(
                currentDate = currentDate,
                currentTime = currentTime
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Status Cards
            StatusCardsSection(
                takenCount = viewModel.takenCount,
                remainingCount = viewModel.remainingCount
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Add Pill Button
            AddPillButton(
                onClick = { viewModel.showAddPillDialog() }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Pills List
            PillsListSection(
                pills = pills,
                onMarkAsTaken = { pillId -> viewModel.markPillAsTaken(pillId) },
                onMarkAsNotTaken = { pillId -> viewModel.markPillAsNotTaken(pillId) },
                onDeletePill = { pillId -> viewModel.deletePill(pillId) }
            )
        }
        
        // Floating Action Button for Demo Notification
        FloatingActionButton(
            onClick = { viewModel.triggerDemoNotification() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF8B5CF6)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Test Notification",
                tint = Color.White
            )
        }
    }
    
    // Add Pill Dialog
    if (showAddDialog) {
        AddPillDialog(
            onDismiss = { viewModel.hideAddPillDialog() },
            onAddPill = { pill -> 
                viewModel.addPill(pill)
                viewModel.hideAddPillDialog()
            }
        )
    }
    
    // Notification Dialog
    if (showNotificationDialog && activeNotificationPill != null) {
        NotificationDialog(
            pill = activeNotificationPill!!,
            onMarkAsTaken = { 
                viewModel.markPillAsTaken(activeNotificationPill!!.id)
                viewModel.hideNotificationDialog()
            },
            onSnooze = { viewModel.hideNotificationDialog() },
            onDismiss = { viewModel.hideNotificationDialog() }
        )
    }
}

@Composable
fun HeaderSection(
    currentDate: String,
    currentTime: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💊",
                fontSize = 48.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "My Pills",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = currentDate,
                fontSize = 20.sp,
                color = Color(0xFF6B7280)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = currentTime,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB)
            )
        }
    }
}

@Composable
fun StatusCardsSection(
    takenCount: Int,
    remainingCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Taken Card
        StatusCard(
            count = takenCount,
            label = "Taken",
            backgroundColor = Color(0xFFDCFCE7),
            textColor = Color(0xFF166534),
            modifier = Modifier.weight(1f)
        )
        
        // Remaining Card
        StatusCard(
            count = remainingCount,
            label = "Remaining",
            backgroundColor = Color(0xFFFED7AA),
            textColor = Color(0xFFC2410C),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatusCard(
    count: Int,
    label: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = label,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun AddPillButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "+ Add New Medication",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun PillsListSection(
    pills: List<Pill>,
    onMarkAsTaken: (Long) -> Unit,
    onMarkAsNotTaken: (Long) -> Unit,
    onDeletePill: (Long) -> Unit
) {
    Text(
        text = "Today's Medications",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1F2937),
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    if (pills.isEmpty()) {
        EmptyState()
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(pills) { pill ->
                PillCard(
                    pill = pill,
                    onMarkAsTaken = { onMarkAsTaken(pill.id) },
                    onMarkAsNotTaken = { onMarkAsNotTaken(pill.id) },
                    onDeletePill = { onDeletePill(pill.id) }
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💊",
                fontSize = 48.sp,
                color = Color(0xFF9CA3AF)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No medications added yet",
                fontSize = 20.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tap \"Add New Medication\" to get started",
                fontSize = 16.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PillCard(
    pill: Pill,
    onMarkAsTaken: () -> Unit,
    onMarkAsNotTaken: () -> Unit,
    onDeletePill: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (pill.taken) Color(0xFFECFDF5) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pill Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.size(64.dp),
                    colors = CardDefaults.cardColors(containerColor = pill.getColorValue()),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💊",
                            fontSize = 32.sp
                        )
                    }
                }
            }
            
            // Pill Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = pill.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (pill.taken) Color(0xFF166534) else Color(0xFF1F2937)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "📏 ${pill.dosage}",
                    fontSize = 18.sp,
                    color = Color(0xFF6B7280)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "🕐 ${pill.time}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
            }
            
            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = if (pill.taken) onMarkAsNotTaken else onMarkAsTaken,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pill.taken) Color(0xFFDCFCE7) else Color(0xFF2563EB)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (pill.taken) "Done ✓" else "Take",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pill.taken) Color(0xFF166534) else Color.White
                    )
                }
                
                IconButton(
                    onClick = onDeletePill,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color(0xFFEF4444)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddPillDialog(
    onDismiss: () -> Unit,
    onAddPill: (Pill) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("blue") }
    
    val colors = listOf("blue", "red", "green", "orange", "purple", "pink")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Medication",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medication Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage (e.g., 100mg)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (e.g., 8:00 AM)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Pill Color",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { color ->
                        val colorValue = when (color) {
                            "blue" -> Color(0xFF2563EB)
                            "red" -> Color(0xFFEF4444)
                            "green" -> Color(0xFF10B981)
                            "orange" -> Color(0xFFF59E0B)
                            "purple" -> Color(0xFF8B5CF6)
                            "pink" -> Color(0xFFEC4899)
                            else -> Color(0xFF2563EB)
                        }
                        
                        Card(
                            modifier = Modifier.size(48.dp),
                            colors = CardDefaults.cardColors(containerColor = colorValue),
                            onClick = { selectedColor = color },
                            border = if (selectedColor == color) {
                                androidx.compose.foundation.BorderStroke(2.dp, Color.Black)
                            } else null
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "💊",
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && dosage.isNotBlank() && time.isNotBlank()) {
                        onAddPill(
                            Pill(
                                name = name,
                                dosage = dosage,
                                time = time,
                                color = selectedColor
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && dosage.isNotBlank() && time.isNotBlank()
            ) {
                Text("Add Pill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NotificationDialog(
    pill: Pill,
    onMarkAsTaken: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Time for your pill!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDBEAFE)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pill Icon
                    Card(
                        modifier = Modifier.size(80.dp),
                        colors = CardDefaults.cardColors(containerColor = pill.getColorValue()),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "💊",
                                fontSize = 40.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Pill Info
                    Column {
                        Text(
                            text = pill.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = pill.dosage,
                            fontSize = 16.sp,
                            color = Color(0xFF6B7280)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = pill.time,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onMarkAsTaken,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("✓ Mark as Taken")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onSnooze) {
                    Text("Snooze 10m")
                }
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        }
    )
}

@Composable
fun PillReminderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2563EB),
            secondary = Color(0xFF10B981),
            surface = Color(0xFFF9FAFB)
        ),
        content = content
    )
}