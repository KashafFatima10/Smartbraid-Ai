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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.smartbraidai.ui.theme.*
import com.example.smartbraidai.ui.viewmodels.SalonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditStylistScreen(
    navController: NavController,
    viewModel: SalonViewModel,
    artistId: String? = null
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var services by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val isLoading by viewModel.loading
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(artistId, viewModel.artists.value) {
        if (!artistId.isNullOrBlank()) {
            val existing = viewModel.artists.value.find { it.id == artistId }
            existing?.let { a ->
                name = a.name
                role = a.role
                experience = a.experience
                description = a.description
                services = a.services.joinToString(", ")
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Set Stylist Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(4.dp, Primary, CircleShape)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(40.dp), tint = Primary)
                    }
                }
            }

            Text("Tap to add photo", fontSize = 12.sp, color = Primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))

            Spacer(modifier = Modifier.height(32.dp))

            EditStylistTextField(value = name, onValueChange = { name = it }, label = "Full Name")
            Spacer(modifier = Modifier.height(16.dp))
            EditStylistTextField(value = role, onValueChange = { role = it }, label = "Role (e.g. Master Loctician)")
            Spacer(modifier = Modifier.height(16.dp))
            EditStylistTextField(value = experience, onValueChange = { experience = it }, label = "Experience (e.g. 5+ Years)")
            Spacer(modifier = Modifier.height(16.dp))
            EditStylistTextField(value = description, onValueChange = { description = it }, label = "Stylist Bio", minLines = 3)
            Spacer(modifier = Modifier.height(16.dp))
            EditStylistTextField(value = services, onValueChange = { services = it }, label = "Services (comma separated)", placeholder = "Braids, Locs, Haircut")

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    if (!artistId.isNullOrBlank()) {
                        viewModel.updateStylist(context, artistId, name, role, experience, description, services, selectedImageUri) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) navController.popBackStack()
                        }
                    } else {
                        viewModel.addStylist(context, name, role, experience, description, services, selectedImageUri) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (artistId.isNullOrBlank()) "Save Stylist Profile" else "Update Stylist Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun EditStylistTextField(
    value: String, 
    onValueChange: (String) -> Unit, 
    label: String, 
    placeholder: String = "", 
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value, 
        onValueChange = onValueChange, 
        label = { Text(label) }, 
        modifier = Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(12.dp),
        minLines = minLines,
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder, color = Color.Gray) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = Primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            focusedLabelColor = Primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    )
}
