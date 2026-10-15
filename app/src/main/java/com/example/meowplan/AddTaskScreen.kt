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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    vm: MeowPlanViewModel,
    onBackClick: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedSubjectName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var reminderDaysBefore by remember { mutableStateOf("0") }

    val bangkokTimeZone = TimeZone.getTimeZone("Asia/Bangkok")
    val subjects by vm.subjects.collectAsState()
    val classScheduleState by vm.classScheduleState.collectAsState()

    // Load subjects and classes from Firebase Firestore
    LaunchedEffect(Unit) {
        vm.loadSubjects()
        vm.loadClasses()
    }

    // Merge unique subject names from Firestore subjects & class sessions
    val availableSubjectNames = remember(subjects, classScheduleState.classes) {
        (subjects.map { it.name } + classScheduleState.classes.map { it.subjectId })
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
    }

    // Filter dropdown options based on typed text (case-insensitive)
    val filteredSubjectOptions = remember(availableSubjectNames, selectedSubjectName) {
        if (selectedSubjectName.isBlank()) {
            availableSubjectNames
        } else {
            availableSubjectNames.filter {
                it.contains(selectedSubjectName.trim(), ignoreCase = true)
            }
        }
    }

    var dropdownExpanded by remember { mutableStateOf(false) }

    // Due Date Selection
    val calendar = remember { Calendar.getInstance(bangkokTimeZone) }
    var selectedDateText by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
            timeZone = bangkokTimeZone
        }.format(calendar.time))
    }
    var dueAtMillis by remember { mutableStateOf(calendar.timeInMillis) }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val priorities = listOf("Low", "Medium", "High")

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
            text = "Add Task 📝",
            fontSize = 22.sp,
            style = MaterialTheme.typography.titleLarge,
            color = TextDark
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task Title") },
            placeholder = { Text("e.g. Finish Assignment 1") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        // Editable Autocomplete Subject Dropdown
        Text("Subject", color = TextDark, fontSize = 14.sp)

        ExposedDropdownMenuBox(
            expanded = dropdownExpanded && availableSubjectNames.isNotEmpty(),
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedSubjectName,
                onValueChange = { newValue ->
                    selectedSubjectName = newValue
                    dropdownExpanded = true
                },
                label = { Text("Select or type Subject") },
                placeholder = { Text("e.g. Mobile Application, Database") },
                trailingIcon = {
                    if (availableSubjectNames.isNotEmpty()) {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            if (availableSubjectNames.isNotEmpty() && filteredSubjectOptions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    filteredSubjectOptions.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name, fontSize = 15.sp) },
                            onClick = {
                                selectedSubjectName = name
                                dropdownExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            placeholder = { Text("Details or notes about this task...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            minLines = 3
        )

        // Due Date Format yyyy-MM-dd HH:mm
        OutlinedTextField(
            value = selectedDateText,
            onValueChange = { text ->
                selectedDateText = text
                try {
                    val sdfDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
                        timeZone = bangkokTimeZone
                    }
                    val sdfDateOnly = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                        timeZone = bangkokTimeZone
                    }
                    val parsed = sdfDateTime.parse(text) ?: sdfDateOnly.parse(text)
                    if (parsed != null) {
                        dueAtMillis = parsed.time
                    }
                } catch (_: Exception) {}
            },
            label = { Text("Due Date & Time (YYYY-MM-DD HH:mm)") },
            placeholder = { Text("2026-03-15 18:00") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Text("Priority Level", color = TextDark, fontSize = 14.sp)
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            priorities.forEachIndexed { index, p ->
                SegmentedButton(
                    selected = priority == p,
                    onClick = { priority = p },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = priorities.size
                    )
                ) {
                    Text(p)
                }
            }
        }

        OutlinedTextField(
            value = reminderDaysBefore,
            onValueChange = { reminderDaysBefore = it.filter { c -> c.isDigit() } },
            label = { Text("Reminder (Days Before Due Date)") },
            placeholder = { Text("0") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        if (errorMessage.isNotBlank()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Button(
            onClick = {
                if (title.isBlank()) {
                    errorMessage = "กรุณากรอกชื่อหัวข้องาน"
                    return@Button
                }

                isLoading = true
                errorMessage = ""

                val task = Task(
                    title = title.trim(),
                    subjectId = selectedSubjectName.trim(),
                    description = description.trim(),
                    dueAt = dueAtMillis,
                    priority = priority,
                    reminderDaysBefore = reminderDaysBefore.toIntOrNull() ?: 0,
                    isDone = false
                )

                vm.addTask(task) { success, msg ->
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
            Text(if (isLoading) "Saving..." else "Save Task")
        }
    }
}
