
package com.example.meowplan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meowplan.ui.theme.BorderPink
import com.example.meowplan.ui.theme.CardWhite
import com.example.meowplan.ui.theme.PageCream
import com.example.meowplan.ui.theme.PinkPrimary
import com.example.meowplan.ui.theme.TextDark
import com.example.meowplan.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    vm: MeowPlanViewModel,
    onAddTaskClick: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onBackClick: () -> Unit
) {
    val tasks by vm.tasks.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var selectedPriorityFilter by remember { mutableStateOf("All") }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    val statusTabs = listOf("All", "To Do", "Completed", "Overdue")
    val priorityOptions = listOf("All", "Low", "Medium", "High")

    val now = System.currentTimeMillis()

    val filteredSortedTasks = remember(
        tasks,
        searchQuery,
        selectedStatusFilter,
        selectedPriorityFilter,
        now
    ) {
        tasks.filter { task ->
            val matchesSearch = searchQuery.isBlank() ||
                    task.title.contains(searchQuery, ignoreCase = true) ||
                    task.subjectId.contains(searchQuery, ignoreCase = true)

            val isOverdue = !task.isDone &&
                    task.dueAt > 0 &&
                    task.dueAt < now

            val matchesStatus = when (selectedStatusFilter) {
                "To Do" -> !task.isDone
                "Completed" -> task.isDone
                "Overdue" -> isOverdue
                else -> true
            }

            val matchesPriority =
                selectedPriorityFilter == "All" ||
                        task.priority.equals(
                            selectedPriorityFilter,
                            ignoreCase = true
                        )

            matchesSearch && matchesStatus && matchesPriority
        }.sortedBy {
            if (it.dueAt == 0L) Long.MAX_VALUE else it.dueAt
        }
    }

    LaunchedEffect(Unit) {
        vm.loadTasks()
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
                    onClick = onAddTaskClick,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PinkPrimary,
                        contentColor = TextDark
                    )
                ) {
                    Text(
                        "+ Add Task",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tasks 📝",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("ค้นหางานหรือวิชา...") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecondaryScrollableTabRow(
                selectedTabIndex = statusTabs
                    .indexOf(selectedStatusFilter)
                    .coerceAtLeast(0),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                contentColor = TextDark
            ) {
                statusTabs.forEach { tab ->
                    Tab(
                        selected = selectedStatusFilter == tab,
                        onClick = { selectedStatusFilter = tab },
                        text = {
                            Text(
                                text = tab,
                                fontWeight = if (selectedStatusFilter == tab) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Priority:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                priorityOptions.forEach { priority ->
                    FilterChip(
                        selected = selectedPriorityFilter == priority,
                        onClick = {
                            selectedPriorityFilter = priority
                        },
                        label = {
                            Text(priority, fontSize = 11.sp)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredSortedTasks.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardWhite
                    ),
                    border = BorderStroke(1.dp, BorderPink)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "ไม่พบรายการงาน 🐱",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        Text(
                            "กดปุ่ม + Add Task เพื่อเพิ่มงานใหม่",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filteredSortedTasks,
                        key = { it.id }
                    ) { task ->
                        TaskCardItem(
                            task = task,
                            now = now,
                            onToggleDone = { newValue ->
                                vm.setTaskDone(
                                    taskId = task.id,
                                    newValue = newValue
                                )
                            },
                            onClick = { onTaskClick(task) },
                            onDelete = { taskToDelete = task }
                        )
                    }
                }
            }
        }
    }

    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("ลบงาน") },
            text = {
                Text("คุณต้องการลบงาน \"${task.title}\" หรือไม่?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = task.id
                        taskToDelete = null
                        vm.deleteTask(id) { _, _ -> }
                    }
                ) {
                    Text(
                        "ลบ",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

@Composable
private fun TaskCardItem(
    task: Task,
    now: Long,
    onToggleDone: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isOverdue = !task.isDone &&
            task.dueAt > 0 &&
            task.dueAt < now

    val bangkokTimeZone = TimeZone.getTimeZone("Asia/Bangkok")

    val dueDateString = if (task.dueAt > 0) {
        SimpleDateFormat(
            "yyyy-MM-dd HH:mm",
            Locale.getDefault()
        ).apply {
            timeZone = bangkokTimeZone
        }.format(Date(task.dueAt))
    } else {
        "No due date"
    }

    val priorityColor = when (task.priority.lowercase()) {
        "high" -> Color(0xFFE57373)
        "medium" -> Color(0xFFFFCC80)
        else -> Color(0xFFA8D5BA)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        border = BorderStroke(1.dp, BorderPink)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { newValue ->
                    onToggleDone(newValue)
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClick() }
            ) {
                Text(
                    text = task.title,
                    color = if (task.isDone) {
                        TextSecondary
                    } else {
                        TextDark
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = if (task.isDone) {
                        TextStyle(
                            textDecoration = TextDecoration.LineThrough
                        )
                    } else {
                        TextStyle.Default
                    }
                )

                if (task.subjectId.isNotBlank()) {
                    Text(
                        text = task.subjectId,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dueDateString,
                        color = if (isOverdue) {
                            Color(0xFFE57373)
                        } else {
                            TextSecondary
                        },
                        fontSize = 11.sp,
                        fontWeight = if (isOverdue) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        }
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = priorityColor
                    ) {
                        Text(
                            text = task.priority,
                            modifier = Modifier.padding(
                                horizontal = 6.dp,
                                vertical = 2.dp
                            ),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    if (isOverdue) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE57373)
                        ) {
                            Text(
                                text = "OVERDUE",
                                modifier = Modifier.padding(
                                    horizontal = 6.dp,
                                    vertical = 2.dp
                                ),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            TextButton(onClick = onDelete) {
                Text(
                    "Delete",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
        }
    }
}
