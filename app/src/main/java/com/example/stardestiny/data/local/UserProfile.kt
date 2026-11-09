package com.example.stardestiny.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dob: String,
    val tob: String,
    val place: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String = "Asia/Kolkata"
)
