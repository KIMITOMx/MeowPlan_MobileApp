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
fun EmailVerificationScreen(
    onContinueClick: () -> Unit
) {
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
            text = "ยืนยันอีเมลของคุณ ✉️",
            color = TextDark,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "เราได้ส่งลิงก์ยืนยันตัวตนไปที่อีเมลของคุณแล้ว กรุณาตรวจสอบกล่องข้อความ",
            color = TextDark,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onContinueClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PinkPrimary,
                contentColor = TextDark
            )
        ) {
            Text("ฉันยืนยันอีเมลแล้ว / ดำเนินการต่อ")
        }
    }
}
