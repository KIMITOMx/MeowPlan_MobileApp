package com.example.meowplan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meowplan.ui.theme.PageCream
import com.example.meowplan.ui.theme.PinkPrimary
import com.example.meowplan.ui.theme.TextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClassScreen(
    vm: MeowPlanViewModel,
    onBackClick: () -> Unit
) {
    var subjectName by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableStateOf("Monday") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("12:00") }
    var room by remember { mutableStateOf("") }
    var repeatWeekly by remember { mutableStateOf(true) }

    var errorMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val state by vm.classScheduleState.collectAsState()
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageCream)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            TextButton(onClick = onBackClick) {
                Text("← Back", color = TextDark, fontSize = 16.sp)
            }
        }

        Text(
            text = "Add Class 📚",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineMedium,
            color = TextDark
        )

        Text(
            text = "Let's plan your study schedule!",
            fontSize = 15.sp,
            color = TextDark
        )

        Text("Subject", color = TextDark, fontSize = 16.sp)

        OutlinedTextField(
            value = subjectName,
            onValueChange = {
                subjectName = it
                if (errorMessage.isNotBlank()) errorMessage = ""
            },
            label = { Text("Subject Name") },
            placeholder = { Text("e.g. Database, Mobile App Dev") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Text("Day of week", color = TextDark, fontSize = 16.sp)
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            val shortDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            days.forEachIndexed { index, day ->
                SegmentedButton(
                    selected = dayOfWeek == day,
                    onClick = { dayOfWeek = day },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = days.size
                    )
                ) {
                    Text(shortDays[index], fontSize = 12.sp)
                }
            }
        }

        OutlinedTextField(
            value = startTime,
            onValueChange = { startTime = it },
            label = { Text("Start time (HH:mm)") },
            placeholder = { Text("09:00") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = endTime,
            onValueChange = { endTime = it },
            label = { Text("End time (HH:mm)") },
            placeholder = { Text("12:00") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = room,
            onValueChange = { room = it },
            label = { Text("Room") },
            placeholder = { Text("e.g. 4-301") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Repeat every week", color = TextDark, fontSize = 16.sp)
            Switch(
                checked = repeatWeekly,
                onCheckedChange = { repeatWeekly = it }
            )
        }

        if (errorMessage.isNotBlank()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        } else if (state.message.isNotBlank()) {
            Text(state.message, color = TextDark, fontSize = 14.sp)
        }

        Button(
            onClick = {
                if (subjectName.isBlank()) {
                    errorMessage = "กรุณาระบุชื่อวิชา"
                    return@Button
                }
                if (!startTime.matches(Regex("^([01]\\d|2[0-3]):[0-5]\\d$")) ||
                    !endTime.matches(Regex("^([01]\\d|2[0-3]):[0-5]\\d$"))
                ) {
                    errorMessage = "รูปแบบเวลาต้องเป็น HH:mm เช่น 09:00"
                    return@Button
                }
                if (startTime >= endTime) {
                    errorMessage = "เวลาเริ่มเรียนต้องก่อนเวลาเลิกเรียน"
                    return@Button
                }

                errorMessage = ""
                isSubmitting = true

                val session = ClassSession(
                    subjectId = subjectName.trim(),
                    dayOfWeek = dayOfWeek,
                    startTime = startTime.trim(),
                    endTime = endTime.trim(),
                    room = room.trim(),
                    repeatWeekly = repeatWeekly
                )

                vm.addClassSession(session) { success, msg ->
                    isSubmitting = false
                    if (success) {
                        onBackClick()
                    } else {
                        errorMessage = msg
                    }
                }
            },
            enabled = !state.isLoading && !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PinkPrimary,
                contentColor = TextDark
            )
        ) {
            Text(if (state.isLoading || isSubmitting) "กำลังตรวจสอบและบันทึก..." else "Save Class", fontSize = 16.sp)
        }
    }
}
