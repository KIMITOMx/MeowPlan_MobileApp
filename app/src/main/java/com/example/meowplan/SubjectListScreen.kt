package com.example.meowplan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun SubjectListScreen(
    vm: MeowPlanViewModel,
    onAddSubjectClick: () -> Unit,
    onEditSubjectClick: (Subject) -> Unit,
    onBackClick: () -> Unit
) {
    val subjects by vm.subjects.collectAsState()

    var subjectToDelete by remember { mutableStateOf<Subject?>(null) }
    var deleteErrorDialogMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vm.loadSubjects()
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
                    onClick = onAddSubjectClick,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PinkPrimary,
                        contentColor = TextDark
                    )
                ) {
                    Text("+ Add Subject", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "My Subjects 🎨",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (subjects.isEmpty()) {
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
                        Text("ยังไม่มีรายวิชา 🐱", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("กดปุ่ม + Add Subject เพื่อเพิ่มรายวิชาใหม่", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(subjects, key = { it.id }) { subject ->
                        SubjectCard(
                            subject = subject,
                            onEdit = { onEditSubjectClick(subject) },
                            onDelete = { subjectToDelete = subject }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    subjectToDelete?.let { subject ->
        AlertDialog(
            onDismissRequest = { subjectToDelete = null },
            title = { Text("ลบวิชา") },
            text = { Text("คุณต้องการลบวิชา \"${subject.name}\" หรือไม่?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = subject.id
                        subjectToDelete = null
                        vm.deleteSubject(id) { success, msg ->
                            if (!success) {
                                deleteErrorDialogMessage = msg
                            }
                        }
                    }
                ) {
                    Text("ลบ", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToDelete = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    // Error Alert Dialog if deletion was prevented due to references
    if (deleteErrorDialogMessage.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { deleteErrorDialogMessage = "" },
            title = { Text("ไม่สามารถลบวิชาได้") },
            text = { Text(deleteErrorDialogMessage) },
            confirmButton = {
                TextButton(onClick = { deleteErrorDialogMessage = "" }) {
                    Text("ตกลง")
                }
            }
        )
    }
}

@Composable
private fun SubjectCard(
    subject: Subject,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val subjectColor = try {
        Color(android.graphics.Color.parseColor(subject.colorHex))
    } catch (_: Exception) {
        PinkPrimary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, BorderPink)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(subjectColor)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject.name,
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (subject.teacher.isNotBlank()) {
                    Text(
                        text = "ผู้สอน: ${subject.teacher}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            TextButton(onClick = onEdit) {
                Text("Edit", fontSize = 13.sp)
            }

            TextButton(onClick = onDelete) {
                Text("Delete", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
        }
    }
}
