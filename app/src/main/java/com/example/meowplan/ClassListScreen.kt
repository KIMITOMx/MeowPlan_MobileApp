package com.example.meowplan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meowplan.ui.theme.BorderPink
import com.example.meowplan.ui.theme.CardWhite
import com.example.meowplan.ui.theme.PageCream
import com.example.meowplan.ui.theme.PinkPrimary
import com.example.meowplan.ui.theme.TextDark
import com.example.meowplan.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassListScreen(
    vm: MeowPlanViewModel,
    onAddClassClick: () -> Unit,
    onEditClassClick: (ClassSession) -> Unit,
    onBackClick: () -> Unit
) {
    val classScheduleState by vm.classScheduleState.collectAsState()
    val classes = classScheduleState.classes

    var selectedDay by remember { mutableStateOf("All") }
    var classToDelete by remember { mutableStateOf<ClassSession?>(null) }

    val daysFilter = listOf("All", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    val filteredClasses = remember(classes, selectedDay) {
        if (selectedDay == "All") classes
        else classes.filter { it.dayOfWeek.equals(selectedDay, ignoreCase = true) }
    }

    LaunchedEffect(Unit) {
        vm.loadClasses()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PageCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBackClick) {
                    Text("← Back", color = TextDark)
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onAddClassClick,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PinkPrimary,
                        contentColor = TextDark
                    )
                ) {
                    Text("+ Add Class", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Class Schedule 📚",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Day Filter Tabs
            SecondaryScrollableTabRow(
                selectedTabIndex = daysFilter.indexOf(selectedDay).coerceAtLeast(0),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                contentColor = TextDark
            ) {
                daysFilter.forEach { day ->
                    Tab(
                        selected = selectedDay == day,
                        onClick = { selectedDay = day },
                        text = {
                            Text(
                                text = if (day == "All") "ทั้งหมด" else day.take(3),
                                fontWeight = if (selectedDay == day) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredClasses.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, BorderPink)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("ยังไม่มีตารางเรียน 🐱", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("กดปุ่ม + Add Class เพื่อเพิ่มคลาสเรียนใหม่", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredClasses, key = { it.id }) { session ->
                        ClassSessionCard(
                            session = session,
                            onEdit = { onEditClassClick(session) },
                            onDelete = { classToDelete = session }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    classToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { classToDelete = null },
            title = { Text("ลบตารางเรียน") },
            text = { Text("คุณต้องการลบตารางเรียนวิชา \"${session.subjectId}\" (${session.dayOfWeek}) หรือไม่?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = session.id
                        classToDelete = null
                        vm.deleteClassSession(id) { _, _ -> }
                    }
                ) {
                    Text("ลบ", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { classToDelete = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

@Composable
private fun ClassSessionCard(
    session: ClassSession,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, BorderPink)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.subjectId,
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    TextButton(onClick = onEdit) {
                        Text("Edit", fontSize = 13.sp)
                    }
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }

            Text(
                text = "${session.dayOfWeek} · ${session.startTime}–${session.endTime}",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            if (session.room.isNotBlank()) {
                Text(
                    text = "Room: ${session.room}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            if (session.repeatWeekly) {
                Text(
                    text = "Repeats every week",
                    color = Color(0xFF79B895),
                    fontSize = 11.sp
                )
            }
        }
    }
}
