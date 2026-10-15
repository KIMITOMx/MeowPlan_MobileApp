package com.example.meowplan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meowplan.ui.theme.BorderPink
import com.example.meowplan.ui.theme.CardWhite
import com.example.meowplan.ui.theme.PageCream
import com.example.meowplan.ui.theme.PinkLight
import com.example.meowplan.ui.theme.PinkPrimary
import com.example.meowplan.ui.theme.TextDark
import com.example.meowplan.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val GreenLight = Color(0xFFE0F0E7)
private val BlueLight = Color(0xFFE0EAF7)
private val RedLight = Color(0xFFFCE8E6)

@Composable
fun HomeScreen(
    vm: MeowPlanViewModel,
    onLogoutClick: () -> Unit = {},
    onAddTaskClick: () -> Unit = {},
    onAddClassClick: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToClasses: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val username by vm.username.collectAsState()
    val classScheduleState by vm.classScheduleState.collectAsState()
    val tasks by vm.tasks.collectAsState()

    val classes = classScheduleState.classes
    val now = System.currentTimeMillis()

    val bangkokTimeZone = TimeZone.getTimeZone("Asia/Bangkok")

    // Real Statistics
    val toDoCount = tasks.count { !it.isDone }
    val completedCount = tasks.count { it.isDone }
    val overdueCount = tasks.count { !it.isDone && it.dueAt > 0 && it.dueAt < now }
    val classesCount = classes.size

    // Today's Day Name (e.g. "Monday")
    val todayDayName = remember {
        SimpleDateFormat("EEEE", Locale.ENGLISH).apply {
            timeZone = bangkokTimeZone
        }.format(Date())
    }
    val currentDateFormatted = remember {
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).apply {
            timeZone = bangkokTimeZone
        }.format(Date())
    }

    // Today's Classes
    val todayClasses = remember(classes, todayDayName) {
        classes.filter { it.dayOfWeek.equals(todayDayName, ignoreCase = true) }
    }

    // Today's Tasks (Due today or overdue)
    val todayTasks = remember(tasks) {
        val calToday = Calendar.getInstance(bangkokTimeZone).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        tasks.filter { !it.isDone && (it.dueAt <= calToday || it.dueAt == 0L) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PageCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PageCream)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Hello, ${username.ifBlank { "Student" }}! 🩷",
                        color = TextDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentDateFormatted,
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }

                IconButton(
                    onClick = onNavigateToProfile,
                    modifier = Modifier.size(52.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.cat_mascot),
                        contentDescription = "Profile",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Quick Navigation Bar (3 items)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NavChip(label = "Calendar 📅", modifier = Modifier.weight(1f), onClick = onNavigateToCalendar)
                NavChip(label = "Tasks 📝", modifier = Modifier.weight(1f), onClick = onNavigateToTasks)
                NavChip(label = "Classes 📚", modifier = Modifier.weight(1f), onClick = onNavigateToClasses)
            }

            // Overview Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, BorderPink)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Today's Overview",
                        color = TextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OverviewItem(
                            number = toDoCount.toString(),
                            label = "To Do",
                            color = PinkLight,
                            onClick = onNavigateToTasks
                        )
                        OverviewItem(
                            number = completedCount.toString(),
                            label = "Completed",
                            color = GreenLight,
                            onClick = onNavigateToTasks
                        )
                        OverviewItem(
                            number = overdueCount.toString(),
                            label = "Overdue",
                            color = RedLight,
                            onClick = onNavigateToTasks
                        )
                        OverviewItem(
                            number = classesCount.toString(),
                            label = "Classes",
                            color = BlueLight,
                            onClick = onNavigateToClasses
                        )
                    }
                }
            }

            // Today's Class Schedule
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Classes 📚 ($todayDayName)",
                    color = TextDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onAddClassClick,
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PinkPrimary,
                        contentColor = TextDark
                    )
                ) {
                    Text("+ Class", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            if (todayClasses.isEmpty()) {
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
                        Text(
                            text = "ไม่มีตารางเรียนในวันนี้ 🐱",
                            color = TextDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "พักผ่อนให้สบาย หรือกด + Class เพื่อเพิ่มตารางเรียน",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                todayClasses.forEach { session ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToClasses() },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, BorderPink)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = session.subjectId,
                                color = TextDark,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "${session.startTime}–${session.endTime}",
                                color = TextSecondary,
                                fontSize = 15.sp
                            )

                            if (session.room.isNotBlank()) {
                                Text(
                                    text = "Room: ${session.room}",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Today's Tasks
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pending Tasks 📝",
                    color = TextDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onAddTaskClick,
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PinkPrimary,
                        contentColor = TextDark
                    )
                ) {
                    Text("+ Task", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            if (todayTasks.isEmpty()) {
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
                        Text(
                            text = "ไม่มีงานค้างในขณะนี้ 😸",
                            color = TextDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "ยอดเยี่ยมมาก! หรือกด + Task เพื่อเพิ่มงานใหม่",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                todayTasks.take(5).forEach { task ->
                    TaskCardHome(
                        task = task,
                        now = now,
                        onToggleDone = { vm.toggleTaskDone(task) },
                        onClick = onNavigateToTasks
                    )
                }
            }

            // Cat Encouragement Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PinkLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.cat_mascot),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "You've got this!",
                            color = TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "One task at a time. Keep going!",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToNotifications,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("🔔 Alerts", color = TextDark, fontSize = 15.sp)
                }

                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PinkPrimary,
                        contentColor = TextDark
                    )
                ) {
                    Text("Sign Out", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun NavChip(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, BorderPink)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
        }
    }
}

@Composable
private fun OverviewItem(
    number: String,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color
        ) {
            Text(
                text = number,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                color = TextDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun TaskCardHome(
    task: Task,
    now: Long,
    onToggleDone: () -> Unit,
    onClick: () -> Unit
) {
    val isOverdue = !task.isDone && task.dueAt > 0 && task.dueAt < now

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
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
                onCheckedChange = { onToggleDone() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClick() }
            ) {
                Text(
                    text = task.title,
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (task.subjectId.isNotBlank()) {
                    Text(
                        text = task.subjectId,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            if (isOverdue) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE57373)
                ) {
                    Text(
                        text = "Overdue",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
