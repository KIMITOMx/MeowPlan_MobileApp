package com.example.meowplan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.meowplan.ui.theme.PinkPrimary
import com.example.meowplan.ui.theme.TextDark
import com.example.meowplan.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    vm: MeowPlanViewModel,
    onNavigateToNotificationSettings: () -> Unit,
    onBackClick: () -> Unit
) {
    val userProfile by vm.userProfile.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PageCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                text = "Settings ⚙️",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            // Account Section
            Text("Account Details", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, BorderPink)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Email: ${userProfile?.email ?: ""}", fontSize = 13.sp, color = TextDark)
                    Text("UID: ${userProfile?.uid ?: ""}", fontSize = 12.sp, color = TextSecondary)
                }
            }

            // Notification Preference Button
            Button(
                onClick = onNavigateToNotificationSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PinkPrimary,
                    contentColor = TextDark
                )
            ) {
                Text("🔔 Notification Settings", fontWeight = FontWeight.SemiBold)
            }

            // About App
            Text("About MeowPlan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
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
                    Text("MeowPlan Student Planner v1.0", fontWeight = FontWeight.Bold, color = TextDark)
                    Text("Your cute pastel study planner companion 🐱🩷", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}
