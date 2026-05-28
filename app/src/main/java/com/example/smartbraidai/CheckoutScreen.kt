package com.example.smartbraidai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartbraidai.ui.theme.*
import com.example.smartbraidai.ui.viewmodels.SalonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    viewModel: SalonViewModel,
    onToggleDarkMode: () -> Unit = {}
) {
    val booking = viewModel.pendingBooking.value
    val isLoading by viewModel.loading
    
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    val isFormValid = cardName.isNotBlank() && 
                      cardNumber.length >= 16 && 
                      expiry.isNotBlank() && 
                      cvv.length >= 3

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CommonHeader(title = "Checkout", onToggleDarkMode = onToggleDarkMode)
        },
        bottomBar = {
            BottomNavBar(navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (booking == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No pending booking found.", color = MaterialTheme.colorScheme.onBackground)
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))

                Text("Order Summary", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("Review your appointment", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), modifier = Modifier.padding(top = 4.dp, bottom = 20.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        OrderSummaryItem(
                            title = booking.serviceSelected,
                            subtitle = "Date: ${booking.date} | Time: ${booking.time}",
                            price = "$${booking.amount}"
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(20.dp))

                        PriceRow("Subtotal", "$${booking.amount}")
                        PriceRow("Service Fee", "$10.00")

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Amount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Surface(color = Secondary.copy(alpha = 0.1f), shape = RoundedCornerShape(50)) {
                                Text("$${booking.amount + 10.0}", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Secondary)
                            }
                        }
                    }
                }

                // Payment Form
                Spacer(modifier = Modifier.height(40.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Payment Information", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        
                        CheckoutTextField(label = "CARDHOLDER NAME", value = cardName, onValueChange = { cardName = it }, placeholder = "Jane Doe")
                        CheckoutTextField(label = "CARD NUMBER", value = cardNumber, onValueChange = { if (it.length <= 16) cardNumber = it }, placeholder = "xxxx xxxx xxxx xxxx")

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                CheckoutTextField(label = "EXPIRY", value = expiry, onValueChange = { expiry = it }, placeholder = "04 / 28")
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                CheckoutTextField(label = "CVV", value = cvv, onValueChange = { if (it.length <= 3) cvv = it }, placeholder = "***", visualTransformation = PasswordVisualTransformation())
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = {
                                viewModel.confirmBooking {
                                    navController.navigate("confirmation") {
                                        popUpTo("artists") { inclusive = false }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            enabled = !isLoading && isFormValid
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Pay Now", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun OrderSummaryItem(title: String, subtitle: String, price: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(Primary.copy(alpha = 0.1f))) {
            Icon(Icons.Default.Image, null, modifier = Modifier.align(Alignment.Center), tint = Primary.copy(alpha = 0.5f))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(price, fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutTextField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
            shape = RoundedCornerShape(18.dp), visualTransformation = visualTransformation,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), 
                focusedBorderColor = Primary,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}
