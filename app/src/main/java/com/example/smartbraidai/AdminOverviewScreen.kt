package com.example.smartbraidai

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.smartbraidai.ui.viewmodels.SalonViewModel
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("NewApi")
@Composable
fun AdminOverviewScreen(
    navController: NavController, 
    viewModel: SalonViewModel,
    onToggleDarkMode: () -> Unit = {}
) {
    val allBookings by viewModel.allBookings.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val clients by viewModel.clients
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            currentUser?.uid?.let { uid ->
                viewModel.updateProfilePicture(context, uid, it)
            }
        }
    }

    LaunchedEffect(currentUser) {
        viewModel.observeAllBookings()
        viewModel.fetchClients()
        currentUser?.uid?.let { viewModel.observeUserProfile(it) }
    }

    val totalRevenue = allBookings.sumOf { it.amount }
    val activeCount = allBookings.count { 
        val s = it.status.trim().lowercase()
        s != "completed" && s != "cancelled" && s.isNotEmpty()
    }

    val serviceRevenueMap = allBookings.groupBy { it.serviceSelected }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
        .toMap()
    
    val overallTrends = calculateChronologicalTrends(allBookings)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { 
            OverviewHeader(
                profilePic = userProfile?.profilePic,
                onAvatarClick = { imageLauncher.launch("image/*") },
                onToggleDarkMode = onToggleDarkMode
            ) 
        },
        bottomBar = { AdminBottomNavBar(navController, "admin_overview") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KPICard(
                    title = "Live Bookings",
                    value = activeCount.toString(),
                    percentage = if (allBookings.isNotEmpty()) "+${(activeCount * 100) / allBookings.size}%" else "0%",
                    icon = Icons.Default.EventAvailable,
                    iconBg = Primary.copy(alpha = 0.1f),
                    iconTint = Primary,
                    modifier = Modifier.weight(1f)
                )
                KPICard(
                    title = "Total Revenue",
                    value = if (totalRevenue >= 1000) "$${String.format(Locale.ENGLISH, "%.1f", totalRevenue / 1000)}k" else "$${totalRevenue.toInt()}",
                    percentage = "+18%",
                    icon = Icons.Default.Payments,
                    iconBg = AccentTan.copy(alpha = 0.1f),
                    iconTint = AccentTan,
                    modifier = Modifier.weight(1f)
                )
            }

            AIOptimizationCard()

            SectionHeader(title = "Overall Booking Trends")
            BookingTrendsChart(overallTrends)

            SectionHeader(title = "Revenue by Service (Top 5)")
            RevenueByServiceChart(serviceRevenueMap)

            AIInsightsCard(allBookings)
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@SuppressLint("NewApi")
private fun calculateChronologicalTrends(bookings: List<Booking>): List<Pair<String, Int>> {
    if (bookings.isEmpty()) return emptyList()
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
    return bookings.groupBy { it.date.trim() }
        .mapNotNull { entry ->
            runCatching { LocalDate.parse(entry.key, formatter) }.getOrNull()?.let { it to entry.value.size }
        }
        .sortedBy { it.first }
        .map { (date, count) -> date.format(DateTimeFormatter.ofPattern("d/M")) to count }
}

@Composable
fun KPICard(title: String, value: String, percentage: String, icon: ImageVector, iconBg: Color, iconTint: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier, 
        color = MaterialTheme.colorScheme.surface, 
        shape = RoundedCornerShape(24.dp), 
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.size(32.dp).background(iconBg, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
                }
                Text(percentage, color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 11.sp)
            Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OverviewHeader(profilePic: String?, onAvatarClick: () -> Unit, onToggleDarkMode: () -> Unit) {
    val isDark = MaterialTheme.colorScheme.background == Color.Black || MaterialTheme.colorScheme.background == Color(0xFF000000)

    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp), shadowElevation = 2.dp) {
        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BackgroundDark).clickable { onAvatarClick() }, contentAlignment = Alignment.Center) {
                if (!profilePic.isNullOrBlank()) {
                    AsyncImage(model = profilePic, contentDescription = "Admin", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(24.dp), tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("SmartBraid AI", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("ANALYTICS PORTAL", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
            }
            IconButton(onClick = onToggleDarkMode) {
                Icon(
                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun BookingTrendsChart(data: List<Pair<String, Int>>) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(200.dp), 
        color = MaterialTheme.colorScheme.surface, 
        shape = RoundedCornerShape(24.dp), 
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (data.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                    Text("No data yet", color = Color.Gray) 
                }
            } else {
                Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    val width = size.width
                    val height = size.height
                    val maxVal = data.maxByOrNull { it.second }?.second?.coerceAtLeast(1) ?: 1
                    if (data.size < 2) {
                        val y = height - (data[0].second.toFloat() / maxVal * height * 0.7f) - (height * 0.15f)
                        drawCircle(color = Primary, radius = 6.dp.toPx(), center = androidx.compose.ui.geometry.Offset(width / 2, y))
                    } else {
                        val stepX = width / (data.size - 1)
                        val path = Path().apply {
                            data.forEachIndexed { i, p ->
                                val x = i * stepX
                                val y = height - (p.second.toFloat() / maxVal * height * 0.7f) - (height * 0.15f)
                                if (i == 0) moveTo(x, y) else lineTo(x, y)
                            }
                        }
                        drawPath(path, Primary, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                        val fill = Path().apply { addPath(path); lineTo(width, height); lineTo(0f, height); close() }
                        drawPath(fill, brush = Brush.verticalGradient(listOf(Primary.copy(alpha = 0.2f), Color.Transparent)))
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    val labelData = if (data.size > 5) listOf(data.first(), data[data.size/2], data.last()) else data
                    labelData.forEach { Text(it.first, fontSize = 9.sp, color = Color.Gray) }
                }
            }
        }
    }
}

@Composable
fun RevenueByServiceChart(serviceRevenue: Map<String, Double>) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(200.dp), 
        color = MaterialTheme.colorScheme.surface, 
        shape = RoundedCornerShape(24.dp), 
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        if (serviceRevenue.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data", color = Color.Gray) }
        } else {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                val max = (serviceRevenue.values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0) * 1.5
                serviceRevenue.entries.take(5).forEach { (svc, rev) ->
                    ServiceBar(label = svc.take(8), fraction = (rev / max).toFloat(), modifier = Modifier.width(44.dp))
                }
            }
        }
    }
}

@Composable
fun ServiceBar(label: String, fraction: Float, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Secondary.copy(alpha = 0.1f), RoundedCornerShape(50.dp)), contentAlignment = Alignment.BottomCenter) {
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(fraction.coerceIn(0.1f, 1f)).background(Secondary, RoundedCornerShape(50.dp)))
        }
        Text(label, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1)
    }
}

@Composable
fun AIOptimizationCard() {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Primary, Secondary))).padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("AI Efficiency Score", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Text("94.8/100", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.Bolt, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun AIInsightsCard(bookings: List<Booking>) {
    Surface(
        modifier = Modifier.fillMaxWidth(), 
        color = MaterialTheme.colorScheme.surface, 
        shape = RoundedCornerShape(24.dp), 
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.AutoAwesome, null, tint = Primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                val insight = if (bookings.isEmpty()) "Waiting for activity..." else "High demand for weekend slots detected."
                Text("AI Insight", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(insight, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 8.dp))
}
