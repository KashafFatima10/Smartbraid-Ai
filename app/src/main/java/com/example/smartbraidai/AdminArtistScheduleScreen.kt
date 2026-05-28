package com.example.smartbraidai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.smartbraidai.data.models.Artist
import com.example.smartbraidai.ui.theme.Primary
import com.example.smartbraidai.ui.theme.Secondary
import com.example.smartbraidai.ui.viewmodels.SalonViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminArtistScheduleScreen(
    navController: NavController, 
    viewModel: SalonViewModel,
    onToggleDarkMode: () -> Unit = {}
) {
    val artists by remember { derivedStateOf { viewModel.artists.value } }
    val isLoading by viewModel.loading
    val coroutineScope = rememberCoroutineScope()

    var editingArtist by remember { mutableStateOf<Artist?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { CommonHeader(title = "Artist Schedules", onToggleDarkMode = onToggleDarkMode) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { AdminBottomNavBar(navController, "admin_artist_schedule") }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Primary)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(artists) { artist ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth(),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = artist.name, 
                                            fontWeight = FontWeight.Bold, 
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = artist.role, 
                                            fontSize = 12.sp, 
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            editingArtist = artist
                                            showEditor = true
                                        },
                                        modifier = Modifier.background(Primary.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit schedule", tint = Primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Text(
                                    text = formatAvailabilitySummary(artist.availability),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            if (showEditor && editingArtist != null) {
                ArtistScheduleDialog(
                    artist = editingArtist!!,
                    onDismiss = {
                        showEditor = false
                        editingArtist = null
                    },
                    onSave = { availability ->
                        val artistId = editingArtist?.id.orEmpty()
                        viewModel.updateArtistAvailability(artistId, availability) { success, _ ->
                            coroutineScope.launch {
                                if (success) {
                                    showEditor = false
                                    editingArtist = null
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ArtistScheduleDialog(artist: Artist, onDismiss: () -> Unit, onSave: (Map<String, Any>) -> Unit) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val state = remember { mutableStateMapOf<String, MutableState<Map<String, String>>>() }
    val scrollState = rememberScrollState()

    LaunchedEffect(artist.id) {
        state.clear()
        for (day in days) {
            val existing = artist.availability[day]
            val map = mutableMapOf<String, String>()
            if (existing != null) {
                map.putAll(existing)
            } else {
                map["enabled"] = "false"
                map["start"] = "09:00"
                map["end"] = "17:00"
            }
            state[day] = mutableStateOf(map)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Set availability for ${artist.name}", 
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Choose working days and hours.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                days.forEach { day ->
                    val dayState = state[day] ?: return@forEach
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(day, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        formatAvailabilityWindow(dayState.value),
                                        fontSize = 11.sp,
                                        color = Primary
                                    )
                                }
                                var enabled by remember(artist.id, day) {
                                    mutableStateOf((dayState.value["enabled"] == "true"))
                                }
                                Switch(
                                    checked = enabled,
                                    onCheckedChange = {
                                        enabled = it
                                        dayState.value = dayState.value.toMutableMap().also { map ->
                                            map["enabled"] = if (it) "true" else "false"
                                        }
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ScheduleTimeField(
                                    value = dayState.value["start"] ?: "09:00",
                                    label = "Start",
                                    onValueChange = { value ->
                                        dayState.value = dayState.value.toMutableMap().also { map -> map["start"] = value }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                ScheduleTimeField(
                                    value = dayState.value["end"] ?: "17:00",
                                    label = "End",
                                    onValueChange = { value ->
                                        dayState.value = dayState.value.toMutableMap().also { map -> map["end"] = value }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { 
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) 
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val out = mutableMapOf<String, Any>()
                            for ((key, value) in state) out[key] = value.value
                            onSave(out)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTimeField(value: String, label: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 10.sp) },
        modifier = modifier,
        textStyle = MaterialTheme.typography.bodySmall,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

fun formatAvailabilitySummary(availability: Map<String, Any>): String {
    val enabledDays = availability.filter {
        val details = it.value as? Map<*, *>
        details?.get("enabled") == "true"
    }.keys
    return if (enabledDays.isEmpty()) "No availability set" else "Available: ${enabledDays.joinToString(", ")}"
}

fun formatAvailabilityWindow(details: Map<String, String>): String {
    if (details["enabled"] != "true") return "Unavailable"
    return "${details["start"]} - ${details["end"]}"
}
