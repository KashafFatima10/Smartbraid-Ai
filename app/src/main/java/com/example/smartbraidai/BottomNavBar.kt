package com.example.smartbraidai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.smartbraidai.ui.theme.Primary

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(80.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(Icons.Outlined.Home, "Home", currentRoute == "artists") {
                navController.navigate("artists") { popUpTo("artists") { inclusive = true }; launchSingleTop = true }
            }
            BottomNavItem(Icons.Outlined.Notifications, "Alerts", currentRoute == "notifications") {
                navController.navigate("notifications") { launchSingleTop = true }
            }
            Box(
                modifier = Modifier.size(54.dp).clip(CircleShape).background(Primary).clickable { 
                    navController.navigate("ai_pattern") { launchSingleTop = true }
                },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.AutoAwesome, "AI", tint = Color.White, modifier = Modifier.size(26.dp))
            }
            BottomNavItem(Icons.Outlined.Style, "Rewards", currentRoute == "rewards") {
                navController.navigate("rewards") { launchSingleTop = true }
            }
            BottomNavItem(Icons.Outlined.Person, "Profile", currentRoute == "profile") {
                navController.navigate("profile") { launchSingleTop = true }
            }
        }
    }
}

@Composable
fun AdminBottomNavBar(navController: NavController, currentRoute: String? = null) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AdminNavItem(Icons.Default.Dashboard, "Home", currentRoute == "admin") { navController.navigate("admin") }
            AdminNavItem(Icons.Default.Group, "Staff", currentRoute == "staff") { navController.navigate("staff") }
            AdminNavItem(Icons.Default.CalendarMonth, "Schedules", currentRoute == "admin_artist_schedule") { navController.navigate("admin_artist_schedule") }
            AdminNavItem(Icons.Default.Person, "Clients", currentRoute == "admin_clients") { navController.navigate("admin_clients") }
            AdminNavItem(Icons.Default.Analytics, "Overview", currentRoute == "admin_overview") { navController.navigate("admin_overview") }
            AdminNavItem(Icons.Default.History, "History", currentRoute == "admin_history") { navController.navigate("admin_history") }
        }
    }
}

@Composable
private fun BottomNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val contentColor = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)) {
        Icon(icon, label, modifier = Modifier.size(24.dp), tint = contentColor)
        Text(label.uppercase(), fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = contentColor)
    }
}

@Composable
fun AdminNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val contentColor = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Icon(icon, null, tint = contentColor)
        Text(label, fontSize = 10.sp, color = contentColor)
    }
}
