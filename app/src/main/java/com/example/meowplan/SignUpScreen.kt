package com.example.meowplan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource

private val Pink = Color(0xFFF4A7BB)
private val Cream = Color(0xFFFFF9F5)
private val MainText = Color(0xFF51434A)

@Composable
fun SignUpScreen(
    onSignInClick: () -> Unit = {},
    vm: MeowPlanViewModel = viewModel()
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authMessage by vm.authMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.cat_mascot),
            contentDescription = "MeowPlan Cat Mascot",
            modifier = Modifier.size(240.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "🐱 MeowPlan",
            color = MainText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your cute little study buddy!",
            color = MainText
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

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
            label = { Text("Password (อย่างน้อย 6 ตัวอักษร)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                when {
                    username.isBlank() ->
                        Unit

                    email.isBlank() ->
                        Unit

                    password.length < 6 ->
                        Unit

                    else ->
                        vm.registerUser(username, email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Pink,
                contentColor = MainText
            )
        ) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = authMessage, color = MainText)

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSignInClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MainText
            )
        ) {
            Text("Already have an account? Sign In")
        }
    }
}
