package com.example.smartbraidai

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartbraidai.data.models.Booking
import com.example.smartbraidai.ui.theme.*
import com.example.smartbraidai.ui.viewmodels.SalonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScheduleScreen(
    navController: NavController, 
    viewModel: SalonViewModel,
    onToggleDarkMode: () -> Unit = {}
) {
    val allBookings by viewModel.allBookings.collectAsState()
    val context = LocalContext.current
    var selectedBookingForAction by remember { mutableStateOf<Booking?>(null) }

    LaunchedEffect(Unit) {
        viewModel.observeAllBookings()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { CommonHeader(title = "Daily Schedule", onToggleDarkMode = onToggleDarkMode) },
        bottomBar = { AdminBottomNavBar(navController, "admin_schedule") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            val activeSchedules = allBookings.filter { 
                it.status.equals("Upcoming", ignoreCase = true) || 
                it.status.equals("Confirmed", ignoreCase = true) 
            }

            if (activeSchedules.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No active appointments for today.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(activeSchedules) { booking ->
                        ScheduleItem(booking) {
                            selectedBookingForAction = booking
                        }
                    }
                }
            }
        }

        if (selectedBookingForAction != null) {
            AlertDialog(
                onDismissRequest = { selectedBookingForAction = null },
                title = { Text("Update Appointment") },
                text = { Text("What would you like to do with ${selectedBookingForAction?.userName}'s booking?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateStatus(selectedBookingForAction!!.id, "Completed")
                            selectedBookingForAction = null
                            Toast.makeText(context, "Marked as Completed! Points awarded.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Mark Completed", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.updateStatus(selectedBookingForAction!!.id, "Cancelled")
                        selectedBookingForAction = null
                        Toast.makeText(context, "Booking Cancelled", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Cancel Booking", color = Color.Red)
                    }
                }
            )
        }
    }
}

@Composable
fun ScheduleItem(booking: Booking, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Schedule, null, tint = Primary, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(booking.userName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("${booking.serviceSelected} • ${booking.time}", fontSize = 13.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AccentTan))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(booking.status.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = AccentTan)
                }
            }

            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}
