package com.example.smartbraidai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartbraidai.data.models.Booking
import com.example.smartbraidai.ui.theme.*
import com.example.smartbraidai.ui.viewmodels.SalonViewModel

@Composable
fun AdminHistoryScreen(
    navController: NavController, 
    viewModel: SalonViewModel,
    onToggleDarkMode: () -> Unit = {}
) {
    val allBookings by viewModel.allBookings.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.observeAllBookings()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CommonHeader(title = "Payment History", onToggleDarkMode = onToggleDarkMode)
        },
        bottomBar = {
            AdminBottomNavBar(navController, "admin_history")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (allBookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transaction history found.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(allBookings) { booking ->
                        HistoryTransactionCard(booking)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryTransactionCard(booking: Booking) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (booking.paymentStatus == "Paid") Color(0xFFE8F5E9).copy(alpha = 0.2f) else Color(0xFFFFF3E0).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AttachMoney, 
                    null, 
                    tint = if (booking.paymentStatus == "Paid") Color(0xFF4CAF50) else Color(0xFFFB8C00)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.userName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(booking.serviceSelected, fontSize = 12.sp, color = Color.Gray)
                Text(booking.date, fontSize = 10.sp, color = Color.Gray.copy(alpha = 0.6f))
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("$${booking.amount}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
                Surface(
                    color = if (booking.paymentStatus == "Paid") Color(0xFFE8F5E9).copy(alpha = 0.2f) else Color(0xFFFFF3E0).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        booking.paymentStatus.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (booking.paymentStatus == "Paid") Color(0xFF4CAF50) else Color(0xFFFB8C00)
                    )
                }
            }
        }
    }
}
