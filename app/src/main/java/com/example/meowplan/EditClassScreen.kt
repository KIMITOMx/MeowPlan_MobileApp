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
fun EditClassScreen(
    vm: MeowPlanViewModel,
    classSession: ClassSession,
    onBackClick: () -> Unit
) {
    var subjectName by remember { mutableStateOf(classSession.subjectId) }
    var dayOfWeek by remember { mutableStateOf(classSession.dayOfWeek.ifBlank { "Monday" }) }
    var startTime by remember { mutableStateOf(classSession.startTime) }
    var endTime by remember { mutableStateOf(classSession.endTime) }
    var room by remember { mutableStateOf(classSession.room) }
    var repeatWeekly by remember { mutableStateOf(classSession.repeatWeekly) }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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
                Text("← Back", color = TextDark)
            }
        }

        Text(
            text = "Edit Class Session ✏️",
            fontSize = 22.sp,
            style = MaterialTheme.typography.titleLarge,
            color = TextDark
        )

        Text("Subject", color = TextDark, fontSize = 14.sp)

        OutlinedTextField(
            value = subjectName,
            onValueChange = {
                subjectName = it
                if (errorMessage.isNotBlank()) errorMessage = ""
            },
            label = { Text("Subject Name") },
            placeholder = { Text("e.g. Database, Mobile App Dev") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Text("Day of week", color = TextDark, fontSize = 14.sp)
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            val shortDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            days.forEachIndexed { index, day ->
                SegmentedButton(
                    selected = dayOfWeek.equals(day, ignoreCase = true),
                    onClick = { dayOfWeek = day },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = days.size
                    )
                ) {
                    Text(shortDays[index])
                }
            }
        }

        OutlinedTextField(
            value = startTime,
            onValueChange = { startTime = it },
            label = { Text("Start time (HH:mm)") },
            placeholder = { Text("09:00") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = endTime,
            onValueChange = { endTime = it },
            label = { Text("End time (HH:mm)") },
            placeholder = { Text("12:00") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = room,
            onValueChange = { room = it },
            label = { Text("Room") },
            placeholder = { Text("e.g. 4-301") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Repeat every week", color = TextDark)
            Switch(
                checked = repeatWeekly,
                onCheckedChange = { repeatWeekly = it }
            )
        }

        if (errorMessage.isNotBlank()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
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

                isLoading = true
                errorMessage = ""

                val updatedSession = classSession.copy(
                    subjectId = subjectName.trim(),
                    dayOfWeek = dayOfWeek,
                    startTime = startTime.trim(),
                    endTime = endTime.trim(),
                    room = room.trim(),
                    repeatWeekly = repeatWeekly
                )

                vm.updateClassSession(updatedSession) { success, msg ->
                    isLoading = false
                    if (success) {
                        onBackClick()
                    } else {
                        errorMessage = msg
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PinkPrimary,
                contentColor = TextDark
            )
        ) {
            Text(if (isLoading) "Saving..." else "Save Changes")
        }
    }
}
