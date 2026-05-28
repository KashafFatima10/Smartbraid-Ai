package com.example.smartbraidai

import android.net.Uri
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.smartbraidai.ui.theme.*
import com.example.smartbraidai.ui.viewmodels.AuthViewModel
import com.example.smartbraidai.ui.viewmodels.SalonViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    navController: NavController, 
    authViewModel: AuthViewModel, 
    salonViewModel: SalonViewModel,
    onToggleDarkMode: () -> Unit = {}
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    val userProfile by salonViewModel.userProfile.collectAsState()
    val userBookings by salonViewModel.userBookings.collectAsState()
    val isLoading by salonViewModel.loading

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { salonViewModel.updateProfilePicture(context, currentUser?.uid ?: "", it) }
    }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            salonViewModel.observeUserProfile(it.uid)
            salonViewModel.observeUserBookings(it.uid)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { CommonHeader(title = "My Profile", onToggleDarkMode = onToggleDarkMode) },
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Profile Section
            Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(Brush.linearGradient(listOf(Primary, Secondary)))
                )

                Column(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .border(4.dp, MaterialTheme.colorScheme.background, CircleShape)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { launcher.launch("image/*") }
                        ) {
                            if (!userProfile?.profilePic.isNullOrBlank()) {
                                AsyncImage(
                                    model = userProfile?.profilePic,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person, 
                                    null, 
                                    modifier = Modifier.fillMaxSize().padding(20.dp), 
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = Primary,
                            shadowElevation = 4.dp
                        ) {
                            Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.padding(8.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        userProfile?.name ?: "User", 
                        fontSize = 24.sp, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Member Since 2024", 
                        fontSize = 12.sp, 
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
                
                IconButton(
                    onClick = { authViewModel.logout() },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Logout, null, tint = Color.White)
                }
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Primary)
            }

            // Loyalty Progress Card
            val points = userProfile?.rewardPoints ?: 0
            val progress = (points.toFloat() / 1500f).coerceAtMost(1f)
            
            Surface(
                modifier = Modifier.fillMaxWidth().padding(24.dp), 
                shape = RoundedCornerShape(24.dp), 
                color = MaterialTheme.colorScheme.surface, 
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Loyalty Status", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("${points} pts", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress }, 
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)), 
                        color = Primary, 
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }

            Text(
                "Booking History", 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onBackground, 
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (userBookings.isEmpty()) {
                    Text("No past bookings found", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                } else {
                    userBookings.forEach { booking ->
                        HistoryItem(
                            title = booking.serviceSelected,
                            subtitle = "${booking.date} • ${booking.artistName}",
                            status = booking.status
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun HistoryItem(title: String, subtitle: String, status: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(20.dp), 
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).background(Primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.HistoryEdu, null, tint = Primary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Surface(
                color = if (status == "Completed") Color(0xFF10B981).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = status.uppercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (status == "Completed") Color(0xFF10B981) else Color.Gray
                )
            }
        }
    }
}
