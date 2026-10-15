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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meowplan.ui.theme.BorderPink
import com.example.meowplan.ui.theme.CardWhite
import com.example.meowplan.ui.theme.PageCream
import com.example.meowplan.ui.theme.PinkLight
import com.example.meowplan.ui.theme.TextDark
import com.example.meowplan.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationScreen(
    vm: MeowPlanViewModel,
    onBackClick: () -> Unit
) {
    val notifications by vm.notifications.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadNotifications()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PageCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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
                text = "Notifications 🔔",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            if (notifications.isEmpty()) {
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
                        Text("ไม่มีการแจ้งเตือน 🐱", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("ระบบจะแสดงการแจ้งเตือนการเรียนหรืองานของคุณที่นี่", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notifications) { notif ->
                        val id = notif["id"] as? String ?: ""
                        val title = notif["title"] as? String ?: "Notification"
                        val message = notif["message"] as? String ?: ""
                        val createdAt = (notif["createdAt"] as? Long) ?: 0L
                        val isRead = (notif["read"] as? Boolean) ?: false

                        val formattedDate = if (createdAt > 0) {
                            SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(createdAt))
                        } else ""

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!isRead && id.isNotBlank()) {
                                        vm.markNotificationRead(id)
                                    }
                                },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isRead) CardWhite else PinkLight
                            ),
                            border = BorderStroke(1.dp, BorderPink)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextDark
                                    )
                                    if (formattedDate.isNotBlank()) {
                                        Text(
                                            text = formattedDate,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                if (message.isNotBlank()) {
                                    Text(
                                        text = message,
                                        fontSize = 12.sp,
                                        color = TextDark
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
