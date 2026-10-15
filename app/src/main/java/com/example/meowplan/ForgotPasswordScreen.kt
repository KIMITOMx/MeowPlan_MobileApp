package com.example.meowplan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meowplan.ui.theme.PageCream
import com.example.meowplan.ui.theme.PinkPrimary
import com.example.meowplan.ui.theme.TextDark

@Composable
fun ForgotPasswordScreen(
    vm: MeowPlanViewModel,
    onBackToSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageCream)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.cat_mascot),
            contentDescription = "MeowPlan Cat Mascot",
            modifier = Modifier.size(160.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ลืมรหัสผ่าน? 🐱",
            color = TextDark,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "กรอกอีเมลของคุณ เพื่อรับลิงก์สำหรับรีเซ็ตรหัสผ่าน",
            color = TextDark,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                isLoading = true
                vm.sendPasswordResetEmail(email) { success, resultMessage ->
                    isLoading = false
                    isSuccess = success
                    message = resultMessage
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PinkPrimary,
                contentColor = TextDark
            )
        ) {
            Text(if (isLoading) "กำลังส่ง..." else "ส่งลิงก์รีเซ็ตรหัสผ่าน")
        }

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onBackToSignInClick) {
            Text("← กลับไปหน้า Sign In", color = TextDark)
        }
    }
}
