package com.example.stardestiny.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profiles")
    fun getAllProfiles(): Flow<List<UserProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile): Long

    @Delete
    suspend fun deleteProfile(profile: UserProfile)

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getProfileById(id: Int): UserProfile?
}
