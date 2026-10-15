package com.example.meowplan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meowplan.ui.theme.BorderPink
import com.example.meowplan.ui.theme.CardWhite
import com.example.meowplan.ui.theme.PageCream
import com.example.meowplan.ui.theme.TextDark
import com.example.meowplan.ui.theme.TextSecondary

@Composable
fun NotificationSettings(
    onBackClick: () -> Unit
) {
    var taskRemindersEnabled by remember { mutableStateOf(true) }
    var classRemindersEnabled by remember { mutableStateOf(true) }
    var dailySummaryEnabled by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PageCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                text = "Notification Preferences 🔔",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, BorderPink)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Task Reminders", fontWeight = FontWeight.SemiBold, color = TextDark)
                            Text("Notify before task due date", fontSize = 12.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = taskRemindersEnabled,
                            onCheckedChange = { taskRemindersEnabled = it }
                        )
                    }

                    HorizontalDivider(color = BorderPink)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Class Reminders", fontWeight = FontWeight.SemiBold, color = TextDark)
                            Text("Notify before class session starts", fontSize = 12.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = classRemindersEnabled,
                            onCheckedChange = { classRemindersEnabled = it }
                        )
                    }

                    HorizontalDivider(color = BorderPink)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Daily Summary", fontWeight = FontWeight.SemiBold, color = TextDark)
                            Text("Morning study overview notification", fontSize = 12.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = dailySummaryEnabled,
                            onCheckedChange = { dailySummaryEnabled = it }
                        )
                    }
                }
            }
        }
    }
}
