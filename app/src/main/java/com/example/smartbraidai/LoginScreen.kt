package com.example.smartbraidai

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartbraidai.ui.theme.*
import com.example.smartbraidai.ui.viewmodels.UserState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    userState: UserState = UserState.Idle,
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onCreateAccountClick: () -> Unit = {},
    onToggleDarkMode: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(userState) {
        if (userState is UserState.Error) {
            Toast.makeText(context, userState.message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { CommonHeader(title = "Login", onToggleDarkMode = onToggleDarkMode) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                "Welcome back", 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.SemiBold, 
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Sign in to your personalized hair profile", 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), 
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            CustomTextField(
                value = email, 
                onValueChange = { newValue -> email = newValue }, 
                label = "EMAIL ADDRESS", 
                placeholder = "your@email.com", 
                leadingIcon = Icons.Default.Mail
            )
            
            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "PASSWORD",
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = password, 
                    onValueChange = { newValue -> password = newValue },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("••••••••", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onBackground) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, 
                                contentDescription = null, 
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface, 
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                        focusedBorderColor = Primary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { if (email.isNotEmpty() && password.isNotEmpty()) onLoginClick(email, password) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = userState !is UserState.Loading
            ) {
                if (userState is UserState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Login to Studio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            TextButton(onClick = onCreateAccountClick) {
                Text("Don't have an account? ", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Text("Create Account", color = Secondary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
            leadingIcon = {
                Icon(leadingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                focusedBorderColor = Primary
            )
        )
    }
}
