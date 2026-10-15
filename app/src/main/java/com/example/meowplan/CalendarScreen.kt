package com.example.meowplan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun CalendarScreen(
    vm: MeowPlanViewModel,
    onBackClick: () -> Unit
) {
    val tasks by vm.tasks.collectAsState()
    val classScheduleState by vm.classScheduleState.collectAsState()
    val classes = classScheduleState.classes

    val currentCalendar = remember { mutableStateOf(Calendar.getInstance()) }
    val selectedDate = remember { mutableStateOf(Calendar.getInstance()) }

    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayOfWeekFormat = remember { SimpleDateFormat("EEEE", Locale.ENGLISH) }

    // Compute Days for Current Month Grid
    val daysInMonth = remember(currentCalendar.value.timeInMillis) {
        val cal = currentCalendar.value.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed offset (Sunday=0)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val list = mutableListOf<Calendar?>()
        repeat(firstDayOfWeek) { list.add(null) }
        for (day in 1..maxDays) {
            val dayCal = cal.clone() as Calendar
            dayCal.set(Calendar.DAY_OF_MONTH, day)
            list.add(dayCal)
        }
        list
    }

    // Daily items for selected date
    val selectedDayName = remember(selectedDate.value.timeInMillis) {
        dayOfWeekFormat.format(selectedDate.value.time)
    }

    val selectedDateStartMillis = remember(selectedDate.value.timeInMillis) {
        val c = selectedDate.value.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        c.timeInMillis
    }

    val selectedDateEndMillis = remember(selectedDate.value.timeInMillis) {
        val c = selectedDate.value.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        c.timeInMillis
    }

    val dailyClasses = remember(classes, selectedDayName) {
        classes.filter { it.dayOfWeek.equals(selectedDayName, ignoreCase = true) }
    }

    val dailyTasks = remember(tasks, selectedDateStartMillis, selectedDateEndMillis) {
        tasks.filter { task ->
            task.dueAt in selectedDateStartMillis..selectedDateEndMillis
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PageCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBackClick) {
                    Text("← Back", color = TextDark)
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Calendar 📅",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            // Month Header with Prev / Next Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val cal = currentCalendar.value.clone() as Calendar
                    cal.add(Calendar.MONTH, -1)
                    currentCalendar.value = cal
                }) {
                    Text("◀", fontSize = 16.sp, color = TextDark)
                }

                Text(
                    text = monthYearFormat.format(currentCalendar.value.time),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                IconButton(onClick = {
                    val cal = currentCalendar.value.clone() as Calendar
                    cal.add(Calendar.MONTH, 1)
                    currentCalendar.value = cal
                }) {
                    Text("▶", fontSize = 16.sp, color = TextDark)
                }
            }

            // Days of Week Header (Sun M T W T F Sat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }

            // Calendar Grid Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, BorderPink)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val rows = daysInMonth.chunked(7)
                    rows.forEach { week ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            week.forEach { dayCal ->
                                if (dayCal == null) {
                                    Spacer(modifier = Modifier.weight(1f))
                                } else {
                                    val isSelected = isSameDay(dayCal, selectedDate.value)
                                    val isToday = isSameDay(dayCal, Calendar.getInstance())

                                    val dayName = dayOfWeekFormat.format(dayCal.time)
                                    val dayStart = dayCal.clone() as Calendar
                                    dayStart.set(Calendar.HOUR_OF_DAY, 0)
                                    dayStart.set(Calendar.MINUTE, 0)
                                    dayStart.set(Calendar.SECOND, 0)

                                    val dayEnd = dayCal.clone() as Calendar
                                    dayEnd.set(Calendar.HOUR_OF_DAY, 23)
                                    dayEnd.set(Calendar.MINUTE, 59)
                                    dayEnd.set(Calendar.SECOND, 59)

                                    val hasClass = classes.any { it.dayOfWeek.equals(dayName, ignoreCase = true) }
                                    val hasTask = tasks.any { it.dueAt in dayStart.timeInMillis..dayEnd.timeInMillis }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> PinkPrimary
                                                    isToday -> PinkLight
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable {
                                                selectedDate.value = dayCal
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = dayCal.get(Calendar.DAY_OF_MONTH).toString(),
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = TextDark
                                            )
                                            if (hasClass || hasTask) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (hasClass) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(0xFF81D4FA))
                                                        )
                                                    }
                                                    if (hasTask) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(0xFFE57373))
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Date Summary
            val selectedDateText = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(selectedDate.value.time)

            Text(
                text = "Schedule for $selectedDateText",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            // Classes for Selected Date
            Text(
                text = "Classes ($selectedDayName)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )

            if (dailyClasses.isEmpty()) {
                Text(
                    text = "ไม่มีคลาสเรียนในวันนี้ 🐱",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            } else {
                dailyClasses.forEach { session ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, BorderPink)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(session.subjectId, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 14.sp)
                            Text("${session.startTime} - ${session.endTime}", color = TextSecondary, fontSize = 12.sp)
                            if (session.room.isNotBlank()) {
                                Text("Room: ${session.room}", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Tasks for Selected Date
            Text(
                text = "Tasks Due On This Date",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )

            if (dailyTasks.isEmpty()) {
                Text(
                    text = "ไม่มีงานที่ต้องส่งในวันที่เลือก 😸",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            } else {
                dailyTasks.forEach { task ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, BorderPink)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isDone,
                                onCheckedChange = { vm.toggleTaskDone(task) }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextDark
                                )
                                if (task.subjectId.isNotBlank()) {
                                    Text(
                                        text = task.subjectId,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
