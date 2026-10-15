package com.example.meowplan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.meowplan.ui.theme.TextDark
import com.example.meowplan.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun TaskDetailScreen(
    vm: MeowPlanViewModel,
    taskId: String,
    onEditClick: (Task) -> Unit,
    onBackClick: () -> Unit
) {
    val tasks by vm.tasks.collectAsState()
    val task = tasks.find { it.id == taskId }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (task == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PageCream)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("ไม่พบข้อมูลงาน 🐱", color = TextDark, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBackClick) {
                Text("← Back")
            }
        }
        return
    }

    val now = System.currentTimeMillis()
    val isOverdue = !task.isDone && task.dueAt > 0 && task.dueAt < now

    val bangkokTimeZone = TimeZone.getTimeZone("Asia/Bangkok")
    val formattedDueDate = if (task.dueAt > 0) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
            timeZone = bangkokTimeZone
        }.format(Date(task.dueAt))
    } else {
        "ไม่กำหนดวันส่ง"
    }

    val priorityColor = when (task.priority.lowercase()) {
        "high" -> Color(0xFFE57373)
        "medium" -> Color(0xFFFFCC80)
        else -> Color(0xFFA8D5BA)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PageCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBackClick) {
                    Text("← Back", color = TextDark)
                }

                Row {
                    TextButton(onClick = { onEditClick(task) }) {
                        Text("Edit", fontSize = 14.sp)
                    }
                    TextButton(onClick = { showDeleteDialog = true }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, BorderPink)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = priorityColor
                        ) {
                            Text(
                                text = "Priority: ${task.priority}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }

                        if (isOverdue) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE57373)
                            ) {
                                Text(
                                    text = "Overdue!",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Text(
                        text = task.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    if (task.subjectId.isNotBlank()) {
                        Text(
                            text = "วิชา: ${task.subjectId}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }

                    HorizontalDivider(color = BorderPink)

                    Text(
                        text = "กำหนดส่ง: $formattedDueDate",
                        fontSize = 13.sp,
                        color = TextDark
                    )

                    if (task.reminderDaysBefore > 0) {
                        Text(
                            text = "แจ้งเตือนล่วงหน้า: ${task.reminderDaysBefore} วัน",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "รายละเอียดเพิ่มเติม:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Text(
                            text = task.description,
                            fontSize = 13.sp,
                            color = TextDark
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = task.isDone,
                            onCheckedChange = { vm.toggleTaskDone(task) }
                        )
                        Text(
                            text = if (task.isDone) "ทำเสร็จแล้ว 😸" else "ยังไม่ได้ทำ",
                            fontWeight = FontWeight.SemiBold,
                            color = if (task.isDone) Color(0xFF79B895) else TextDark
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("ลบงาน") },
            text = { Text("คุณต้องการลบงาน \"${task.title}\" หรือไม่?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        vm.deleteTask(task.id) { success, _ ->
                            if (success) {
                                onBackClick()
                            }
                        }
                    }
                ) {
                    Text("ลบ", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}
