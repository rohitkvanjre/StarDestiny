package com.example.stardestiny.model

data class MatchmakingResult(
    val brideName: String,
    val groomName: String,
    val varnaScore: Double,
    val vashyaScore: Double,
    val taraScore: Double,
    val yoniScore: Double,
    val maitriScore: Double,
    val ganaScore: Double,
    val bhakootScore: Double,
    val nadiScore: Double,
    val totalScore: Double,
    val recommendations: String
)

data class KutaDetail(
    val name: String,
    val score: Double,
    val maxScore: Double,
    val description: String
)
