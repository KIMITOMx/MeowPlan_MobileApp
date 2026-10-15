package com.example.meowplan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

@Composable
fun ProfileScreen(
    vm: MeowPlanViewModel,
    onLogoutClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val userProfile by vm.userProfile.collectAsState()
    val username by vm.username.collectAsState()
    val tasks by vm.tasks.collectAsState()
    val classScheduleState by vm.classScheduleState.collectAsState()
    val subjects by vm.subjects.collectAsState()

    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(username) }
    var message by remember { mutableStateOf("") }

    val now = System.currentTimeMillis()
    val completedTasksCount = tasks.count { it.isDone }
    val pendingTasksCount = tasks.count { !it.isDone }
    val overdueTasksCount = tasks.count { !it.isDone && it.dueAt > 0 && it.dueAt < now }
    val classesCount = classScheduleState.classes.size
    val subjectsCount = subjects.size

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PageCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
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

                TextButton(onClick = onSettingsClick) {
                    Text("⚙️ Settings", color = TextDark)
                }
            }

            // Cat Mascot Profile Avatar
            Image(
                painter = painterResource(id = R.drawable.cat_mascot),
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(PinkLight)
                    .padding(8.dp)
            )

            if (isEditingName) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Button(
                        onClick = {
                            if (editedName.isNotBlank()) {
                                vm.updateUserProfile(editedName) { success, msg ->
                                    message = msg
                                    if (success) isEditingName = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PinkPrimary,
                            contentColor = TextDark
                        )
                    ) {
                        Text("Save")
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = username.ifBlank { "Student" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    IconButton(onClick = {
                        editedName = username
                        isEditingName = true
                    }) {
                        Text("✏️", fontSize = 14.sp)
                    }
                }
            }

            Text(
                text = userProfile?.email ?: "",
                fontSize = 14.sp,
                color = TextSecondary
            )

            if (message.isNotBlank()) {
                Text(message, fontSize = 12.sp, color = TextDark)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Real Statistics Section
            Text(
                text = "Study Statistics 📊",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.fillMaxWidth()
            )

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
                    StatRow(label = "Tasks Completed (ทำเสร็จแล้ว)", value = "$completedTasksCount")
                    HorizontalDivider(color = BorderPink)
                    StatRow(label = "Pending Tasks (งานที่ยังไม่เสร็จ)", value = "$pendingTasksCount")
                    HorizontalDivider(color = BorderPink)
                    StatRow(label = "Overdue Tasks (งานเกินกำหนด)", value = "$overdueTasksCount")
                    HorizontalDivider(color = BorderPink)
                    StatRow(label = "Enrolled Subjects (จำนวนวิชา)", value = "$subjectsCount")
                    HorizontalDivider(color = BorderPink)
                    StatRow(label = "Class Sessions (ตารางเรียน)", value = "$classesCount")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign Out Button
            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PinkPrimary,
                    contentColor = TextDark
                )
            ) {
                Text("Sign Out", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
        Text(text = value, color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
