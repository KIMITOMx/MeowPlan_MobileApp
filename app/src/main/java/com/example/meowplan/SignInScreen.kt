package com.example.meowplan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val SignInPink = Color(0xFFF4A7BB)
private val SignInCream = Color(0xFFFFF9F5)
private val SignInText = Color(0xFF51434A)

@Composable
fun SignInScreen(
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    vm: MeowPlanViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authMessage by vm.authMessage.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SignInCream)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.catsignin),
            contentDescription = "MeowPlan Cat Mascot",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "🐱 MeowPlan",
            color = SignInText,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Your cute little study buddy!",
            color = SignInText,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onForgotPasswordClick) {
                Text("Forgot Password?", color = SignInText, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    vm.loginUser(email, password)
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SignInPink,
                contentColor = SignInText
            )
        ) {
            Text(if (isLoading) "Signing In..." else "Sign In")
        }

        if (authMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = authMessage,
                color = SignInText,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSignUpClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = SignInText
            )
        ) {
            Text("Don't have an account? Sign Up")
        }
    }
}
