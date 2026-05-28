package com.example.smartbraidai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.smartbraidai.ui.theme.*
import com.example.smartbraidai.ui.viewmodels.SalonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffScreen(
    navController: NavController, 
    viewModel: SalonViewModel,
    onToggleDarkMode: () -> Unit = {}
) {
    val staffList by viewModel.artists
    val isLoading by viewModel.loading

    LaunchedEffect(Unit) {
        viewModel.fetchArtists()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CommonHeader(title = "Team Members", onToggleDarkMode = onToggleDarkMode)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("admin_add_stylist") },
                containerColor = Primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
            }
        },
        bottomBar = {
            AdminBottomNavBar(navController, "staff")
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Primary)
            } else if (staffList.isEmpty()) {
                Text(
                    "No team members found. Click + to add.", 
                    modifier = Modifier.align(Alignment.Center), 
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
                ) {
                    items(staffList) { staff ->
                        StaffCard(
                            navController = navController,
                            staffId = staff.id,
                            name = staff.name,
                            role = staff.role,
                            rating = staff.rating,
                            imageUrl = staff.imageUrl
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StaffCard(
    navController: NavController,
    staffId: String,
    name: String,
    role: String,
    rating: Double,
    imageUrl: String
) {
    Surface(
        shape = RoundedCornerShape(22.dp), 
        color = MaterialTheme.colorScheme.surface, 
        shadowElevation = 2.dp, 
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(70.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background)) {
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Person, 
                        null, 
                        modifier = Modifier.align(Alignment.Center).size(30.dp), 
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    role,
                    fontSize = 12.sp,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, null, tint = Color(0xFFFFB800), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        rating.toString(), 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            IconButton(
                onClick = { navController.navigate("admin_edit_stylist/$staffId") },
                modifier = Modifier.background(Primary.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Primary, modifier = Modifier.size(20.dp))
            }
        }
    }
}
