package com.example.smartbraidai

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartbraidai.ui.theme.*
import com.example.smartbraidai.ui.viewmodels.UserState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    userState: UserState = UserState.Idle,
    onLoginClick: () -> Unit = {},
    onCreateAccountClick: (String, String, String) -> Unit = { _, _, _ -> },
    onToggleDarkMode: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
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
        topBar = { CommonHeader(title = "Join Us", onToggleDarkMode = onToggleDarkMode) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Create Account", 
                style = MaterialTheme.typography.headlineMedium, 
                fontWeight = FontWeight.ExtraBold, 
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Start your premium salon experience", 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    SignupTextField(value = fullName, onValueChange = { fullName = it }, label = "FULL NAME", placeholder = "Jane Doe", leadingIcon = Icons.Default.Person)
                    Spacer(modifier = Modifier.height(20.dp))
                    SignupTextField(value = email, onValueChange = { email = it }, label = "EMAIL ADDRESS", placeholder = "jane@example.com", leadingIcon = Icons.Default.Mail)
                    Spacer(modifier = Modifier.height(20.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "PASSWORD", 
                            style = MaterialTheme.typography.labelLarge, 
                            fontWeight = FontWeight.Bold, 
                            color = AccentTan, 
                            fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = password, onValueChange = { password = it }, modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("••••••••", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, 
                                        contentDescription = null, 
                                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface, 
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface, 
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { if (fullName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) onCreateAccountClick(fullName, email, password) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = userState !is UserState.Loading
                    ) {
                        if (userState is UserState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Create Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            TextButton(onClick = onLoginClick) {
                Text("Already have an account? ", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Text("Log In", color = Primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupTextField(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String, leadingIcon: ImageVector) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label, 
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp), 
            style = MaterialTheme.typography.labelLarge, 
            fontWeight = FontWeight.Bold, 
            color = AccentTan, 
            fontSize = 12.sp
        )
        OutlinedTextField(
            value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(leadingIcon, null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface, 
                unfocusedContainerColor = MaterialTheme.colorScheme.surface, 
                focusedBorderColor = Primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            )
        )
    }
}
