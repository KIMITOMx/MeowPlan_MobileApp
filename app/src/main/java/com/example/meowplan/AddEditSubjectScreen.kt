package com.example.meowplan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meowplan.ui.theme.PageCream
import com.example.meowplan.ui.theme.PinkPrimary
import com.example.meowplan.ui.theme.TextDark

@Composable
fun AddEditSubjectScreen(
    vm: MeowPlanViewModel,
    subjectToEdit: Subject? = null,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf(subjectToEdit?.name ?: "") }
    var teacher by remember { mutableStateOf(subjectToEdit?.teacher ?: "") }
    var selectedColorHex by remember { mutableStateOf(subjectToEdit?.colorHex ?: "#F4A7BB") }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val presetColors = MeowPlanColors.SubjectColors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageCream)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
            text = if (subjectToEdit == null) "Add Subject 📚" else "Edit Subject ✏️",
            fontSize = 22.sp,
            style = MaterialTheme.typography.titleLarge,
            color = TextDark
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Subject Name") },
            placeholder = { Text("e.g. Mathematics, Mobile App") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = teacher,
            onValueChange = { teacher = it },
            label = { Text("Teacher / Instructor") },
            placeholder = { Text("e.g. Dr. Smith") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Text("Select Subject Color", color = TextDark, fontSize = 14.sp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            presetColors.forEach { hex ->
                val colorVal = try {
                    Color(android.graphics.Color.parseColor(hex))
                } catch (_: Exception) {
                    PinkPrimary
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colorVal)
                        .border(
                            width = if (selectedColorHex.equals(hex, ignoreCase = true)) 3.dp else 1.dp,
                            color = if (selectedColorHex.equals(hex, ignoreCase = true)) TextDark else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { selectedColorHex = hex }
                )
            }
        }

        if (errorMessage.isNotBlank()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isBlank()) {
                    errorMessage = "กรุณากรอกชื่อวิชา"
                    return@Button
                }
                isLoading = true
                errorMessage = ""

                val subject = Subject(
                    id = subjectToEdit?.id ?: "",
                    name = name.trim(),
                    colorHex = selectedColorHex,
                    teacher = teacher.trim()
                )

                if (subjectToEdit == null) {
                    vm.addSubject(subject) { success, msg ->
                        isLoading = false
                        if (success) {
                            onBackClick()
                        } else {
                            errorMessage = msg
                        }
                    }
                } else {
                    vm.updateSubject(subject) { success, msg ->
                        isLoading = false
                        if (success) {
                            onBackClick()
                        } else {
                            errorMessage = msg
                        }
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
            Text(if (isLoading) "Saving..." else "Save Subject")
        }
    }
}
