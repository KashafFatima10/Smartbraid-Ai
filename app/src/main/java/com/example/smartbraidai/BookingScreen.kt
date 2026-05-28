package com.example.smartbraidai

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.smartbraidai.data.models.Booking
import com.example.smartbraidai.ui.theme.*
import com.example.smartbraidai.ui.viewmodels.SalonViewModel
import com.google.firebase.auth.FirebaseAuth
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingScreen(
    navController: NavController,
    artistId: String,
    viewModel: SalonViewModel,
    onToggleDarkMode: () -> Unit = {}
) {
    val artist = viewModel.artists.value.find { it.id == artistId }
    val dayOptions = remember { buildBookingDayOptions() }
    var selectedDayKey by remember { mutableStateOf(dayOptions.firstOrNull()?.dayKey.orEmpty()) }
    var selectedSlot by remember { mutableStateOf("") }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userProfile by viewModel.userProfile.collectAsState()
    val allBookings by viewModel.allBookings.collectAsState()
    
    val availableSlots = remember(artist?.availability, selectedDayKey) {
        buildAvailableSlots(artist?.availability.orEmpty(), selectedDayKey)
    }
    val selectedDayOption = dayOptions.firstOrNull { it.dayKey == selectedDayKey } ?: dayOptions.firstOrNull()
    val selectedBookingDate = selectedDayOption?.date?.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)) ?: ""
    
    val bookedSlots = remember(allBookings, artistId, selectedBookingDate) {
        allBookings.filter { 
            it.artistId == artistId && it.date.trim() == selectedBookingDate.trim() && it.status != "Cancelled" 
        }.map { it.time.trim() }.toSet()
    }
    
    val freeSlots = remember(availableSlots, bookedSlots) {
        availableSlots.filterNot { bookedSlots.contains(it.trim()) }
    }
    
    val groupedSlots = remember(freeSlots) { groupSlotsByPeriod(freeSlots) }

    LaunchedEffect(currentUser) {
        currentUser?.let { viewModel.observeUserProfile(it.uid) }
    }

    LaunchedEffect(Unit) { viewModel.observeAllBookings() }

    LaunchedEffect(freeSlots) {
        if (freeSlots.isEmpty()) selectedSlot = ""
        else if (selectedSlot !in freeSlots) selectedSlot = freeSlots.first()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { CommonHeader(title = "Book Appointment", onToggleDarkMode = onToggleDarkMode) },
        bottomBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(bottom = 16.dp)) {
                BookingCTASection(
                    enabled = selectedSlot.isNotEmpty() && freeSlots.isNotEmpty(),
                    onConfirmBooking = {
                        if (selectedSlot.isNotEmpty() && artist != null && currentUser != null) {
                            val booking = Booking(
                                userId = currentUser.uid,
                                userName = userProfile?.name ?: "Customer",
                                artistId = artist.id,
                                artistName = artist.name,
                                serviceSelected = artist.services.firstOrNull() ?: "Standard Service",
                                date = selectedBookingDate,
                                time = selectedSlot,
                                amount = 220.0,
                                timestamp = System.currentTimeMillis()
                            )
                            viewModel.prepareBooking(booking)
                            navController.navigate("checkout")
                        }
                    }
                )
                BottomNavBar(navController)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Stylist Info
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = artist?.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.background),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(artist?.name ?: "Stylist", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(artist?.role ?: "Expert", fontSize = 14.sp, color = Primary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Text("Select Date", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            
            LazyRow(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dayOptions) { option ->
                    DateItem(option.dayLabel, option.dateLabel, isSelected = selectedDayKey == option.dayKey) {
                        selectedDayKey = option.dayKey
                    }
                }
            }

            Text("Available Slots", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 16.dp))

            if (artist == null) {
                Text("Loading...", color = Color.Gray, modifier = Modifier.padding(top = 12.dp))
            } else if (availableSlots.isEmpty()) {
                EmptyStateCard("No availability set for this day.")
            } else if (freeSlots.isEmpty()) {
                EmptyStateCard("All slots are booked for this day.")
            } else {
                groupedSlots.forEach { (period, slots) ->
                    SlotSection(period, slots, selectedSlot) { selectedSlot = it }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(message, modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center, color = Color.Gray, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SlotSection(title: String, slots: List<String>, selectedSlot: String, onSlotClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            slots.forEach { slot ->
                val isRecommended = slots.indexOf(slot) == 0 && title == "MORNING"
                SlotItem(slot, isSelected = selectedSlot == slot, isRecommended = isRecommended) { onSlotClick(slot) }
            }
        }
    }
}

@Composable
fun SlotItem(slot: String, isSelected: Boolean, isRecommended: Boolean, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.TopEnd) {
        Surface(
            onClick = onClick,
            modifier = Modifier.width(95.dp),
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) Primary else MaterialTheme.colorScheme.surface,
            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
            shadowElevation = if (isSelected) 4.dp else 0.dp
        ) {
            Text(
                text = slot,
                modifier = Modifier.padding(vertical = 12.dp),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
        if (isRecommended) {
            Surface(
                color = Secondary,
                shape = CircleShape,
                modifier = Modifier.offset(x = 6.dp, y = (-6).dp)
            ) {
                Text("AI", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}

@Composable
fun DateItem(day: String, dateLabel: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(64.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Primary else MaterialTheme.colorScheme.surface,
        shadowElevation = if (isSelected) 4.dp else 0.dp,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(day, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White.copy(alpha = 0.7f) else Color.Gray)
            Text(dateLabel, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun BookingCTASection(enabled: Boolean = true, onConfirmBooking: () -> Unit = {}) {
    Button(
        onClick = onConfirmBooking,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(56.dp),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Primary)
    ) {
        Text("Proceed to Checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
    }
}
