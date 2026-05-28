package com.example.smartbraidai

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartbraidai.ui.theme.*
import com.example.smartbraidai.ui.viewmodels.SalonViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AIPatternScreen(
    navController: NavController,
    viewModel: SalonViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onToggleDarkMode: () -> Unit = {}
) {
    // Selection States
    var selectedCategory by viewModel.aiCategory
    
    val isLoading by viewModel.loading
    val aiStatusMessage by viewModel.aiStatusMessage
    val showDialog by viewModel.showSuggestionDialog
    val suggestionTitle by viewModel.suggestionTitle
    val suggestionDesc by viewModel.suggestionDesc
    val aiTips by viewModel.aiTips
    val aiConfidence by viewModel.aiConfidence
    val aiMaintenance by viewModel.aiMaintenance

    val isDark = MaterialTheme.colorScheme.background == Color.Black || MaterialTheme.colorScheme.background == Color(0xFF000000)

    // Popup Dialog for Suggestion
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSuggestionDialog() },
            title = { 
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        color = Primary.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(60.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Primary, modifier = Modifier.padding(12.dp).size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = suggestionTitle, 
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        color = Primary
                    )
                }
            },
            text = { 
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    // Score & Maintenance Tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SuggestionTag(label = "$aiConfidence% Match", icon = Icons.Default.CheckCircle, color = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionTag(label = "$aiMaintenance Maintenance", icon = Icons.Default.Build, color = Color(0xFFFF9800))
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Expert Analysis",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = suggestionDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Smart Tips",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        aiTips.forEach { tip ->
                            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(tip, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissSuggestionDialog() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Got it, Thanks!", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(32.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Text(
                    text = buildAnnotatedString {
                        append("SmartBraid ")
                        withStyle(style = SpanStyle(color = Primary)) { append("AI") }
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onToggleDarkMode) {
                    Icon(imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode, contentDescription = null)
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp
            ) {
                Button(
                    onClick = { viewModel.onGenerateStyleClicked() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(20.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("AI is analyzing...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Analyze Style", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // AI Status & Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Primary.copy(alpha = 0.05f))
                    .border(2.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = aiStatusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Primary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (suggestionTitle.isNotEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Style Recommendation Ready", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text(suggestionTitle, color = Primary, fontWeight = FontWeight.Bold)
                        Text("Click 'Analyze Style' to see details", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = Primary.copy(alpha = 0.1f)
                        ) {
                            Icon(Icons.Default.Insights, contentDescription = null, tint = Primary, modifier = Modifier.padding(16.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Select options below for analysis", color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Category Selection
            Text(
                "SELECT CATEGORY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Hair", "Makeup", "Haircut").forEach { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedCategory = category },
                        color = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Dynamic Option Sections
            AnimatedContent(
                targetState = selectedCategory,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "CategoryTransition"
            ) { targetCategory ->
                when (targetCategory) {
                    "Hair" -> HairCategoryOptions(viewModel)
                    "Makeup" -> MakeupCategoryOptions(viewModel)
                    "Haircut" -> HaircutCategoryOptions(viewModel)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SuggestionTag(label: String, icon: ImageVector, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HairCategoryOptions(viewModel: SalonViewModel) {
    var texture by viewModel.hairTexture
    var color by viewModel.hairColor
    var length by viewModel.hairLength
    var volume by viewModel.hairVolume

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SelectionCategory(title = "Hair Texture", options = listOf("Straight", "Curly", "Wavy", "Coily"), selectedOption = texture, onOptionSelected = { texture = it })
            SelectionCategory(title = "Hair Color", options = listOf("Black", "Brown", "Blonde", "Red"), selectedOption = color, onOptionSelected = { color = it })
            SelectionCategory(title = "Hair Length", options = listOf("Short", "Medium", "Long"), selectedOption = length, onOptionSelected = { length = it })
            SelectionCategory(title = "Hair Volume", options = listOf("Thin", "Normal", "Thick"), selectedOption = volume, onOptionSelected = { volume = it })
        }
    }
}

@Composable
fun MakeupCategoryOptions(viewModel: SalonViewModel) {
    var type by viewModel.makeupType
    var finish by viewModel.skinFinish
    var eye by viewModel.eyeMakeup
    var lip by viewModel.lipStyle

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SelectionCategory(title = "Makeup Style", options = listOf("Natural", "Party", "Bridal"), selectedOption = type, onOptionSelected = { type = it })
            SelectionCategory(title = "Skin Finish", options = listOf("Matte", "Dewy", "Glossy"), selectedOption = finish, onOptionSelected = { finish = it })
            SelectionCategory(title = "Eye Glam", options = listOf("Soft", "Smokey", "Bold"), selectedOption = eye, onOptionSelected = { eye = it })
            SelectionCategory(title = "Lip Finish", options = listOf("Nude", "Glossy", "Matte", "Bold"), selectedOption = lip, onOptionSelected = { lip = it })
        }
    }
}

@Composable
fun HaircutCategoryOptions(viewModel: SalonViewModel) {
    var faceShape by viewModel.faceShape
    var cutLength by viewModel.cutLength

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SelectionCategory(title = "Face Shape", options = listOf("Oval", "Round", "Square", "Heart"), selectedOption = faceShape, onOptionSelected = { faceShape = it })
            SelectionCategory(title = "Target Length", options = listOf("Short", "Medium", "Long"), selectedOption = cutLength, onOptionSelected = { cutLength = it })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectionCategory(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Primary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOptionSelected(option) },
                    color = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
