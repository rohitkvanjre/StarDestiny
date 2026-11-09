package com.example.stardestiny.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class BirthRequest(
    val day: Int,
    val month: Int,
    val year: Int,
    val hour: Int,
    val min: Int,
    val lat: Double,
    val lon: Double,
    val tzone: Double = 5.5
)

data class NakshatraResponse(
    @SerializedName("nakshatra") val nakshatra: String,
    @SerializedName("rashi") val rashi: String,
    @SerializedName("charan") val pada: Int
)

interface AstrologyApi {
    @POST("v1/nakshatra_details")
    suspend fun getNakshatraDetails(
        @Header("Authorization") auth: String,
        @Body request: BirthRequest
    ): NakshatraResponse
}
