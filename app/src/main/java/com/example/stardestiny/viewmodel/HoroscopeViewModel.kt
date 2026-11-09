package com.example.stardestiny.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stardestiny.data.local.ProfileDao
import com.example.stardestiny.data.local.UserProfile
import com.example.stardestiny.model.BirthDetails
import com.example.stardestiny.model.HoroscopeResult
import com.example.stardestiny.model.MatchmakingResult
import com.example.stardestiny.util.LocationService
import com.example.stardestiny.util.MatchmakingCalculator
import com.example.stardestiny.util.VedicCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HoroscopeViewModel @Inject constructor(
    private val profileDao: ProfileDao,
    private val locationService: LocationService
) : ViewModel() {

    enum class Screen {
        Welcome, Home, Profiles, Input, Result, Matchmaking
    }

    enum class MatchmakingTarget {
        NONE, BRIDE, GROOM
    }

    var currentScreen by mutableStateOf(Screen.Welcome)
        private set

    var matchmakingTarget by mutableStateOf(MatchmakingTarget.NONE)
        private set

    val savedProfiles: StateFlow<List<UserProfile>> = profileDao.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var birthDetails by mutableStateOf(BirthDetails("", "", "", ""))
        private set

    var horoscopeResult by mutableStateOf<HoroscopeResult?>(null)
        private set

    var matchmakingResult by mutableStateOf<MatchmakingResult?>(null)
        private set

    var selectedBride by mutableStateOf<UserProfile?>(null)
        private set

    var selectedGroom by mutableStateOf<UserProfile?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var locationSuggestions by mutableStateOf<List<String>>(emptyList())
        private set

    private var selectedProfileId: Int? = null

    fun onLocationQueryChange(query: String) {
        viewModelScope.launch {
            locationSuggestions = locationService.getAddressSuggestions(query)
        }
    }

    fun navigateTo(screen: Screen) {
        if (screen == Screen.Input) {
            selectedProfileId = null
            birthDetails = BirthDetails("", "", "", "")
        }
        if (screen != Screen.Input && screen != Screen.Result) {
            matchmakingTarget = MatchmakingTarget.NONE
        }
        currentScreen = screen
    }

    fun startNewProfileForMatch(target: MatchmakingTarget) {
        matchmakingTarget = target
        navigateTo(Screen.Input)
    }

    fun selectProfile(profile: UserProfile) {
        selectedProfileId = profile.id
        birthDetails = BirthDetails(profile.name, profile.dob, profile.tob, profile.place)
        calculateHoroscope()
    }

    fun selectBride(profile: UserProfile) {
        selectedBride = profile
        checkAndCalculateMatch()
    }

    fun selectGroom(profile: UserProfile) {
        selectedGroom = profile
        checkAndCalculateMatch()
    }

    private fun checkAndCalculateMatch() {
        val bride = selectedBride
        val groom = selectedGroom
        if (bride != null && groom != null) {
            matchmakingResult = MatchmakingCalculator.calculateMatch(bride, groom)
        }
    }

    fun clearMatchmaking() {
        selectedBride = null
        selectedGroom = null
        matchmakingResult = null
    }

    fun deleteProfile(profile: UserProfile) {
        viewModelScope.launch {
            profileDao.deleteProfile(profile)
        }
    }

    fun updateDetails(name: String, date: String, time: String, place: String) {
        birthDetails = BirthDetails(name, date, time, place)
    }

    fun calculateHoroscope() {
        viewModelScope.launch {
            isLoading = true

            try {
                // 1. Get Coordinates from Place Name
                val latLong = locationService.getLatLongFromAddress(birthDetails.place)
                val latitude = latLong?.first ?: 28.6139 // Default to New Delhi if not found
                val longitude = latLong?.second ?: 77.2090

                // 2. Parse Date (DD/MM/YYYY) and Time (hh:mm a)
                val dateParts = birthDetails.date.split("/")
                val sdfInput = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val timeDate = sdfInput.parse(birthDetails.time)
                
                if ((dateParts.size == 3) && (timeDate != null)) {
                    val day = dateParts[0].toInt()
                    val month = dateParts[1].toInt()
                    val year = dateParts[2].toInt()
                    
                    val cal = Calendar.getInstance()
                    cal.time = timeDate
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    val min = cal.get(Calendar.MINUTE)

                    // 3. Perform Offline Calculation
                    horoscopeResult = VedicCalculator.calculate(
                        day = day,
                        month = month,
                        year = year,
                        hour = hour,
                        min = min,
                        lat = latitude,
                        lon = longitude
                    )

                    // 4. Save Profile to Local Database (only if new or update existing)
                    val profileToSave = UserProfile(
                        id = selectedProfileId ?: 0, // 0 for new, existing ID for update
                        name = birthDetails.name,
                        dob = birthDetails.date,
                        tob = birthDetails.time,
                        place = birthDetails.place,
                        latitude = latitude,
                        longitude = longitude
                    )
                    val insertedId = profileDao.insertProfile(profileToSave)

                    // 5. Handle Matchmaking Context
                    if (matchmakingTarget != MatchmakingTarget.NONE) {
                        val savedId = if (selectedProfileId == null || selectedProfileId == 0) insertedId.toInt() else selectedProfileId!!
                        val savedProfile = profileToSave.copy(id = savedId)
                        
                        if (matchmakingTarget == MatchmakingTarget.BRIDE) {
                            selectedBride = savedProfile
                        } else {
                            selectedGroom = savedProfile
                        }
                        checkAndCalculateMatch()
                        currentScreen = Screen.Matchmaking
                        matchmakingTarget = MatchmakingTarget.NONE
                    } else {
                        currentScreen = Screen.Result
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                currentScreen = Screen.Input
            } finally {
                isLoading = false
            }
        }
    }

    fun reset() {
        horoscopeResult = null
        matchmakingResult = null
        selectedProfileId = null
        selectedBride = null
        selectedGroom = null
        currentScreen = Screen.Home // Return to Home instead of Profiles
    }
}
