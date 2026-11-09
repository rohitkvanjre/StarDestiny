package com.example.stardestiny.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun getLatLongFromAddress(addressString: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context)
        try {
            val addresses: List<Address>? = geocoder.getFromLocationName(addressString, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                return@withContext Pair(address.latitude, address.longitude)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    suspend fun getAddressSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        if (query.length < 3) return@withContext emptyList()
        val geocoder = Geocoder(context)
        try {
            val addresses = geocoder.getFromLocationName(query, 5)
            return@withContext addresses?.mapNotNull { it.getAddressLine(0) } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
