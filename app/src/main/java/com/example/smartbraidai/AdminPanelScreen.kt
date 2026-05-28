package com.example.smartbraidai

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.smartbraidai.data.models.Booking
import com.example.smartbraidai.ui.theme.*
import com.example.smartbraidai.ui.viewmodels.AuthViewModel
import com.example.smartbraidai.ui.viewmodels.SalonViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

@Composable
fun AdminPanelScreen(
    navController: NavController, 
    viewModel: SalonViewModel,
    authViewModel: AuthViewModel,
    onToggleDarkMode: () -> Unit = {}
) {
    val allBookings by viewModel.allBookings.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    var selectedBookingForStatus by remember { mutableStateOf<Booking?>(null) }

    val adminImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            currentUser?.uid?.let { uid -> viewModel.updateProfilePicture(context, uid, it) }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.observeAllBookings()
        currentUser?.uid?.let { viewModel.observeUserProfile(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { 
            AdminHeader(
                profilePic = userProfile?.profilePic,
                onAvatarClick = { adminImageLauncher.launch("image/*") },
                onLogoutClick = { authViewModel.logout() },
                onToggleDarkMode = onToggleDarkMode
            ) 
        },
        bottomBar = { AdminBottomNavBar(navController, "admin") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            AIInsightsBanner()

            val totalRevenue = allBookings.sumOf { it.amount }
            
            Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryCard(title = "Total Revenue", value = "$${String.format(Locale.US, "%.0f", totalRevenue)}", icon = Icons.Default.Payments, modifier = Modifier.weight(1f))
                SummaryCard(title = "Total Bookings", value = allBookings.size.toString(), icon = Icons.AutoMirrored.Filled.EventNote, modifier = Modifier.weight(1f))
            }

            Text(
                "Upcoming Appointments", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onBackground, 
                modifier = Modifier.padding(16.dp)
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (allBookings.isEmpty()) {
                    Text("No bookings found yet.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                } else {
                    allBookings.filter { it.status != "Completed" && it.status != "Cancelled" }.take(10).forEach { booking ->
                        AppointmentItem(
                            name = booking.userName,
                            style = "${booking.serviceSelected} • ${booking.artistName}",
                            time = booking.time,
                            status = booking.status,
                            onClick = { selectedBookingForStatus = booking }
                        )
                    }
                }
            }
        }

        // Status Change Dialog
        if (selectedBookingForStatus != null) {
            AlertDialog(
                onDismissRequest = { selectedBookingForStatus = null },
                title = { Text("Update Status") },
                text = { Text("Update status for ${selectedBookingForStatus?.userName}'s booking?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateStatus(selectedBookingForStatus!!.id, "Completed")
                            selectedBookingForStatus = null
                            Toast.makeText(context, "Marked as Completed!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) { Text("Complete", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.updateStatus(selectedBookingForStatus!!.id, "Cancelled")
                        selectedBookingForStatus = null
                        Toast.makeText(context, "Booking Cancelled", Toast.LENGTH_SHORT).show()
                    }) { Text("Cancel Booking", color = Color.Red) }
                }
            )
        }
    }
}

@Composable
fun AdminHeader(
    profilePic: String? = null,
    onAvatarClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onToggleDarkMode: () -> Unit = {}
) {
    val isDark = MaterialTheme.colorScheme.background == Color.Black || MaterialTheme.colorScheme.background == Color(0xFF000000)

    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(BackgroundDark).border(1.dp, Primary.copy(alpha = 0.15f), CircleShape).clickable(onClick = onAvatarClick), contentAlignment = Alignment.Center) {
                if (profilePic.isNullOrBlank()) { Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(22.dp)) } 
                else { AsyncImage(model = profilePic, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("SmartBraid AI", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("ADMIN PORTAL", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = AccentTan)
            }
            
            IconButton(onClick = onToggleDarkMode) {
                Icon(
                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = onLogoutClick) { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.onSurface) }
        }
    }
}

@Composable
fun AIInsightsBanner() {
    Surface(modifier = Modifier.padding(16.dp).fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.1f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, null, tint = AccentTan)
            Spacer(modifier = Modifier.width(12.dp))
            Text("AI Insight: Saturday bookings are 40% higher.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.height(120.dp), shape = RoundedCornerShape(16.dp), color = Color.Transparent) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Primary, Secondary))).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Column {
                    Text(title, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AppointmentItem(name: String, style: String, time: String, status: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }, 
        shape = RoundedCornerShape(12.dp), 
        color = MaterialTheme.colorScheme.surface, 
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.background(Primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(time.split(" ")[0], fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                Text(time.split(" ").getOrElse(1){""}, fontSize = 9.sp, color = Primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(style, fontSize = 11.sp, color = Color.Gray)
            }
            Text(status.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentTan)
        }
    }
}
