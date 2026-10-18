package com.focusly.app.ui.blocking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusly.app.domain.model.AppLimit
import com.focusly.app.domain.model.BlockSchedule
import com.focusly.app.ui.theme.GreenPrimary

@Composable
fun AppLimitsScreen(
    viewModel: AppLimitsViewModel,
    onNavigateToAppPicker: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val appLimits by viewModel.appLimits.collectAsState()
    val blockSchedules by viewModel.blockSchedules.collectAsState()

    var showAddScheduleDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        onNavigateToAppPicker()
                    } else {
                        showAddScheduleDialog = true
                    }
                },
                containerColor = GreenPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("App Limits", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Block Schedules", fontWeight = FontWeight.SemiBold) }
                )
            }

            if (selectedTab == 0) {
                LimitsListTab(
                    limits = appLimits,
                    onToggle = { pkg, enabled -> viewModel.toggleLimit(pkg, enabled) },
                    onDelete = { pkg -> viewModel.deleteLimit(pkg) }
                )
            } else {
                SchedulesListTab(
                    schedules = blockSchedules,
                    onDelete = { id -> viewModel.deleteSchedule(id) }
                )
            }
        }

        if (showAddScheduleDialog) {
            AddScheduleDialog(
                onDismiss = { showAddScheduleDialog = false },
                onConfirm = { title, startH, startM, endH, endM ->
                    viewModel.addSchedule(
                        title = title,
                        startH = startH,
                        startM = startM,
                        endH = endH,
                        endM = endM,
                        days = listOf(1, 2, 3, 4, 5, 6, 7),
                        pkgs = emptyList()
                    )
                    showAddScheduleDialog = false
                }
            )
        }
    }
}

@Composable
fun LimitsListTab(
    limits: List<AppLimit>,
    onToggle: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    if (limits.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No App Limits Set",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the + button to select an application and set your daily maximum usage limit.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(limits, key = { it.packageName }) { limit ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = limit.appName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${limit.currentUsedMinutes}m used of ${limit.dailyLimitMinutes}m limit",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Switch(
                                checked = limit.isEnabled,
                                onCheckedChange = { onToggle(limit.packageName, it) }
                            )
                            IconButton(onClick = { onDelete(limit.packageName) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = limit.progress,
                            color = if (limit.isExceeded) Color(0xFFFF3B30) else GreenPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SchedulesListTab(
    schedules: List<BlockSchedule>,
    onDelete: (Long) -> Unit
) {
    if (schedules.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Block Schedules",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the + button to create a scheduled focus routine (e.g. Work hours 9AM - 5PM).",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(schedules, key = { it.id }) { schedule ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = schedule.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = schedule.timeRangeString,
                                fontSize = 14.sp,
                                color = GreenPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(onClick = { onDelete(schedule.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddScheduleDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, startH: Int, startM: Int, endH: Int, endM: Int) -> Unit
) {
    var title by remember { mutableStateOf("Work Hours") }
    var startHour by remember { mutableIntStateOf(9) }
    var endHour by remember { mutableIntStateOf(17) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Block Schedule") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Schedule Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text("Start Hour: ${startHour}:00")
                Slider(
                    value = startHour.toFloat(),
                    onValueChange = { startHour = it.toInt() },
                    valueRange = 0f..23f,
                    steps = 22
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("End Hour: ${endHour}:00")
                Slider(
                    value = endHour.toFloat(),
                    onValueChange = { endHour = it.toInt() },
                    valueRange = 0f..23f,
                    steps = 22
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(title, startHour, 0, endHour, 0)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
