package com.example.smartbraidai

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartbraidai.ui.theme.*
import com.example.smartbraidai.ui.viewmodels.SalonViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController, 
    viewModel: SalonViewModel,
    onToggleDarkMode: () -> Unit = {}
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val notifications by viewModel.notifications.collectAsState()
    val bookings by viewModel.userBookings.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.let {
            viewModel.observeNotifications(it.uid)
            viewModel.observeUserBookings(it.uid)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { CommonHeader(title = "Notifications & Alerts", onToggleDarkMode = onToggleDarkMode) },
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            // --- UPCOMING BOOKINGS SECTION ---
            if (bookings.isNotEmpty()) {
                item {
                    Text(
                        "YOUR APPOINTMENTS", 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = Primary, 
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(bookings.take(5)) { booking ->
                    UrgentNotificationCard(
                        icon = Icons.Default.CalendarMonth,
                        title = "Booking: ${booking.serviceSelected}",
                        date = booking.date,
                        time = booking.time,
                        description = "With ${booking.artistName}. Status: ${booking.status}"
                    )
                }
            }

            // --- NOTIFICATIONS SECTION ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "RECENT ALERTS", 
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), 
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (notifications.isEmpty() && bookings.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxHeight(0.6f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No alerts at the moment.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                items(notifications) { notification ->
                    RegularNotificationItem(
                        icon = if (notification.type == "AI Insight") Icons.Default.AutoAwesome else Icons.Default.Notifications,
                        title = notification.title,
                        description = notification.description,
                        time = formatTimestamp(notification.timestamp)
                    )
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun UrgentNotificationCard(icon: ImageVector, title: String, date: String, time: String, description: String) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.15f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp), 
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.1f)), 
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Primary, modifier = Modifier.size(24.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = title, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 15.sp, 
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = date.uppercase(), 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.ExtraBold, 
                            color = Primary
                        )
                        Text(
                            text = time, 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Medium, 
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                
                Text(
                    text = description, 
                    fontSize = 13.sp, 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), 
                    modifier = Modifier.padding(top = 8.dp),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun RegularNotificationItem(icon: ImageVector, title: String, description: String, time: String) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp), 
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)), 
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(22.dp))
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(time, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Text(
                description, 
                fontSize = 13.sp, 
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        }
    }
}
