package com.example.smartbraidai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartbraidai.ui.SplashScreen
import com.example.smartbraidai.ui.theme.SmartBraidAITheme
import com.example.smartbraidai.ui.viewmodels.AuthViewModel
import com.example.smartbraidai.ui.viewmodels.SalonViewModel
import com.example.smartbraidai.ui.viewmodels.UserState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }

            SmartBraidAITheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val salonViewModel: SalonViewModel = viewModel()
                val userState by authViewModel.userState
                
                val toggleDarkMode = { isDarkMode = !isDarkMode }

                // --- CENTRAL NAVIGATION OBSERVER ---
                LaunchedEffect(userState) {
                    when (userState) {
                        is UserState.Authenticated -> {
                            val user = (userState as UserState.Authenticated).user
                            if (user.role == "admin") {
                                navController.navigate("admin") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            } else {
                                navController.navigate("artists") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            }
                        }
                        is UserState.Idle -> {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        else -> {}
                    }
                }

                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") { SplashScreen() }

                    composable("login") {
                        LoginScreen(
                            userState = userState,
                            onLoginClick = { email, password -> authViewModel.login(email, password) },
                            onCreateAccountClick = { navController.navigate("signup") },
                            onToggleDarkMode = toggleDarkMode
                        )
                    }

                    composable("signup") {
                        SignupScreen(
                            userState = userState,
                            onLoginClick = { navController.navigate("login") },
                            onCreateAccountClick = { name, email, password -> 
                                authViewModel.signup(name, email, password, "customer")
                            },
                            onToggleDarkMode = toggleDarkMode
                        )
                    }

                    // --- ADMIN ROUTES ---
                    composable("admin") { AdminPanelScreen(navController, salonViewModel, authViewModel, toggleDarkMode) }
                    composable("staff") { StaffScreen(navController, salonViewModel, toggleDarkMode) }
                    composable("admin_add_stylist") { AdminEditStylistScreen(navController, salonViewModel) }
                    composable(
                        route = "admin_edit_stylist/{artistId}",
                        arguments = listOf(navArgument("artistId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
                        AdminEditStylistScreen(navController, salonViewModel, artistId = artistId)
                    }
                    composable("admin_overview") { AdminOverviewScreen(navController, salonViewModel, toggleDarkMode) }
                    composable("admin_clients") { AdminClientsScreen(navController, salonViewModel, toggleDarkMode) }
                    composable("admin_history") { AdminHistoryScreen(navController, salonViewModel, toggleDarkMode) }
                    composable("admin_schedule") { AdminScheduleScreen(navController, salonViewModel, toggleDarkMode) }
                    composable("admin_artist_schedule") { AdminArtistScheduleScreen(navController, salonViewModel, toggleDarkMode) }
                    
                    // --- CUSTOMER ROUTES ---
                    composable("artists") {
                        ArtistsScreen(navController, salonViewModel, onArtistClick = { artist ->
                            navController.navigate("stylist_profile/${artist.id}")
                        }, onToggleDarkMode = toggleDarkMode)
                    }
                    composable(
                        route = "stylist_profile/{artistId}",
                        arguments = listOf(navArgument("artistId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
                        StylistProfileScreen(navController, artistId, salonViewModel, onBookClick = { navController.navigate("booking/$artistId") }, onToggleDarkMode = toggleDarkMode)
                    }
                    composable("booking/{artistId}") { backStackEntry ->
                        val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
                        BookingScreen(navController, artistId, salonViewModel, onToggleDarkMode = toggleDarkMode)
                    }
                    composable("checkout") { CheckoutScreen(navController, salonViewModel, onToggleDarkMode = toggleDarkMode) }
                    composable("confirmation") { ConfirmationScreen(navController, onToggleDarkMode = toggleDarkMode) }
                    composable("ai_pattern") { AIPatternScreen(navController, salonViewModel, onToggleDarkMode = toggleDarkMode) }
                    composable("rewards") { RewardsScreen(navController, salonViewModel, onToggleDarkMode = toggleDarkMode) }
                    composable("profile") { CustomerProfileScreen(navController, authViewModel, salonViewModel, onToggleDarkMode = toggleDarkMode) }
                    composable("notifications") { NotificationScreen(navController, salonViewModel, onToggleDarkMode = toggleDarkMode) }
                }
            }
        }
    }
}
