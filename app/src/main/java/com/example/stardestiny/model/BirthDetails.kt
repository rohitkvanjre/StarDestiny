package com.example.stardestiny.model

data class BirthDetails(
    val name: String,
    val date: String,
    val time: String,
    val place: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class HoroscopeResult(
    val rashi: String,
    val nakshatra: String,
    val pada: Int,
    val luckyNumber: Int,
    val luckyColor: String,
    val basicDescription: String,
    val planetPositions: Map<String, Int> = emptyMap(), // Planet name to Rashi Index (0-11)
    val ascendantIndex: Int = 0, // Lagna Rashi Index (0-11)
    val primaryNamingLetter: String = "",
    val allNamingLetters: List<String> = emptyList()
)
