package com.example.smartbraidai

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartbraidai.ui.theme.*
import com.example.smartbraidai.ui.viewmodels.SalonViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    navController: NavController, 
    viewModel: SalonViewModel,
    onToggleDarkMode: () -> Unit = {}
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userProfile by viewModel.userProfile.collectAsState()
    val userBookings by viewModel.userBookings.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(currentUser) {
        currentUser?.let {
            viewModel.observeUserProfile(it.uid)
            viewModel.observeUserBookings(it.uid)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CommonHeader(title = "Loyalty Rewards", onToggleDarkMode = onToggleDarkMode)
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
            Spacer(modifier = Modifier.height(8.dp))

            // Points Progress Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val points = userProfile?.rewardPoints ?: 0
                    val targetPoints = 1500
                    val progress = (points.toFloat() / targetPoints).coerceAtMost(1f)

                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = Primary,
                            strokeWidth = 10.dp,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                String.format("%,d", points),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "POINTS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Next Reward: Free AI Braid Design",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val remaining = (targetPoints - points).coerceAtLeast(0)
                    Text(
                        if (remaining > 0) "$remaining points until your next unlock" else "Reward Unlocked! Check your email.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Activity Section
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Activity",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { navController.navigate("profile") }) {
                    Text("View History", color = Primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val relevantHistory = userBookings.filter { !it.status.equals("Cancelled", ignoreCase = true) }
                
                if (relevantHistory.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                        Text("No activity yet. Your points will appear here!", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    relevantHistory.take(10).forEach { booking ->
                        val isPaid = booking.paymentStatus.equals("Paid", ignoreCase = true)
                        val isCompleted = booking.status.equals("Completed", ignoreCase = true)
                        
                        ActivityItem(
                            icon = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            title = booking.serviceSelected,
                            date = booking.date,
                            points = if (isPaid) "+150 pts" else "Pending",
                            status = if (isPaid) "VERIFIED" else "UNPAID",
                            iconBg = if (isPaid) Color(0xFF10B981).copy(alpha = 0.15f) else Primary.copy(alpha = 0.15f),
                            iconTint = if (isPaid) Color(0xFF10B981) else Primary,
                            pointsColor = if (isPaid) Color(0xFF10B981) else Primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ActivityItem(
    icon: ImageVector,
    title: String,
    date: String,
    points: String,
    status: String,
    iconBg: Color,
    iconTint: Color,
    pointsColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
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
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                Text(date, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 12.sp)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(points, fontWeight = FontWeight.Bold, color = pointsColor, fontSize = 15.sp)
                Text(status, color = pointsColor.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
