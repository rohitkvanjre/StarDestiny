package com.example.stardestiny

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stardestiny.ui.screens.BirthInputScreen
import com.example.stardestiny.ui.screens.HomeScreen
import com.example.stardestiny.ui.screens.HoroscopeResultScreen
import com.example.stardestiny.ui.screens.MatchmakingScreen
import com.example.stardestiny.ui.screens.SavedProfilesScreen
import com.example.stardestiny.ui.screens.WelcomeScreen
import com.example.stardestiny.ui.theme.StarDestinyTheme
import com.example.stardestiny.viewmodel.HoroscopeViewModel
import com.example.stardestiny.viewmodel.HoroscopeViewModel.Screen

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StarDestinyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HoroscopeApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun HoroscopeApp(
    modifier: Modifier = Modifier,
    viewModel: HoroscopeViewModel = viewModel()
) {
    val currentScreen = viewModel.currentScreen
    val result = viewModel.horoscopeResult
    val savedProfiles by viewModel.savedProfiles.collectAsState()

    Box(modifier = modifier) {
        when (currentScreen) {
            Screen.Welcome -> {
                WelcomeScreen(onGetStarted = { viewModel.navigateTo(Screen.Home) })
            }
            Screen.Home -> {
                HomeScreen(
                    onNavigateToHoroscope = { viewModel.navigateTo(Screen.Profiles) },
                    onNavigateToMatchmaking = { viewModel.navigateTo(Screen.Matchmaking) }
                )
            }
            Screen.Profiles -> {
                SavedProfilesScreen(
                    profiles = savedProfiles,
                    onProfileClick = { viewModel.selectProfile(it) },
                    onDeleteProfileClick = { viewModel.deleteProfile(it) },
                    onAddProfileClick = { viewModel.navigateTo(Screen.Input) },
                    onMatchmakingClick = { viewModel.navigateTo(Screen.Matchmaking) },
                    onBack = { viewModel.navigateTo(Screen.Home) }
                )
            }
            Screen.Input -> {
                BirthInputScreen(
                    isLoading = viewModel.isLoading,
                    locationSuggestions = viewModel.locationSuggestions,
                    onLocationQueryChange = { viewModel.onLocationQueryChange(it) },
                    onCalculate = { name, dob, tob, pob ->
                        viewModel.updateDetails(name, dob, tob, pob)
                        viewModel.calculateHoroscope()
                    }
                )
            }
            Screen.Result -> {
                if (result != null) {
                    HoroscopeResultScreen(
                        result = result,
                        onBack = { viewModel.navigateTo(Screen.Profiles) }
                    )
                } else {
                    // Fallback if result is missing
                    viewModel.navigateTo(Screen.Profiles)
                }
            }
            Screen.Matchmaking -> {
                MatchmakingScreen(
                    profiles = savedProfiles,
                    selectedBride = viewModel.selectedBride,
                    selectedGroom = viewModel.selectedGroom,
                    result = viewModel.matchmakingResult,
                    onBrideSelected = { viewModel.selectBride(it) },
                    onGroomSelected = { viewModel.selectGroom(it) },
                    onCreateNew = { viewModel.startNewProfileForMatch(it) },
                    onBack = { viewModel.navigateTo(Screen.Home) }
                )
            }
        }
    }
}
