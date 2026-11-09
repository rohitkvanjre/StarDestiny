package com.example.stardestiny.util

import com.example.stardestiny.data.local.UserProfile
import com.example.stardestiny.model.MatchmakingResult
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

object MatchmakingCalculator {

    private val RASHIS = arrayOf(
        "Mesha", "Vrishabha", "Mithuna", "Karka",
        "Simha", "Kanya", "Tula", "Vrischika",
        "Dhanu", "Makara", "Kumbha", "Meena"
    )

    private val NAKSHATRAS = arrayOf(
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashirsha", "Ardra",
        "Punarvasu", "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni",
        "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha",
        "Mula", "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishtha", "Shatabhisha",
        "Purva Bhadrapada", "Uttara Bhadrapada", "Revati",
    )

    fun calculateMatch(bride: UserProfile, groom: UserProfile): MatchmakingResult {
        // In a real implementation, we would re-calculate these from birth data 
        // to ensure accuracy, but for this task we assume we can derive them.
        val brideMoonData = getMoonData(bride)
        val groomMoonData = getMoonData(groom)

        val varna = calculateVarna(brideMoonData, groomMoonData)
        val vashya = calculateVashya(brideMoonData, groomMoonData)
        val tara = calculateTara(brideMoonData, groomMoonData)
        val yoni = calculateYoni(brideMoonData, groomMoonData)
        val maitri = calculateMaitri(brideMoonData, groomMoonData)
        val gana = calculateGana(brideMoonData, groomMoonData)
        val bhakoot = calculateBhakoot(brideMoonData, groomMoonData)
        val nadi = calculateNadi(brideMoonData, groomMoonData)

        val total = varna + vashya + tara + yoni + maitri + gana + bhakoot + nadi

        return MatchmakingResult(
            brideName = bride.name,
            groomName = groom.name,
            varnaScore = varna,
            vashyaScore = vashya,
            taraScore = tara,
            yoniScore = yoni,
            maitriScore = maitri,
            ganaScore = gana,
            bhakootScore = bhakoot,
            nadiScore = nadi,
            totalScore = total,
            recommendations = getRecommendation(total)
        )
    }

    private data class MoonData(
        val nakshatraIdx: Int, // 0-26
        val rashiIdx: Int,      // 0-11
        val pada: Int           // 1-4
    )

    private fun getMoonData(profile: UserProfile): MoonData {
        try {
            val dateParts = profile.dob.split("/")
            val sdfInput = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val timeDate = sdfInput.parse(profile.tob)
            
            if ((dateParts.size == 3) && (timeDate != null)) {
                val day = dateParts[0].toInt()
                val month = dateParts[1].toInt()
                val year = dateParts[2].toInt()
                
                val cal = Calendar.getInstance()
                cal.time = timeDate
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val min = cal.get(Calendar.MINUTE)

                val result = VedicCalculator.calculate(
                    day = day, month = month, year = year,
                    hour = hour, min = min,
                    lat = profile.latitude, lon = profile.longitude
                )

                val rashiIdx = RASHIS.indexOf(result.rashi).coerceAtLeast(0)
                val nakIdx = NAKSHATRAS.indexOf(result.nakshatra).coerceAtLeast(0)
                
                return MoonData(nakIdx, rashiIdx, result.pada)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return MoonData(0, 0, 1)
    }

    // 1. Varna (1 Point)
    private fun calculateVarna(b: MoonData, g: MoonData): Double {
        val bVarna = getVarnaType(b.rashiIdx)
        val gVarna = getVarnaType(g.rashiIdx)
        return if (gVarna >= bVarna) 1.0 else 0.0
    }

    private fun getVarnaType(rashi: Int): Int = when (rashi) {
        3, 7, 11 -> 4 // Brahmin
        0, 4, 8 -> 3  // Kshatriya
        1, 5, 9 -> 2  // Vaishya
        else -> 1     // Shudra
    }

    // 2. Vashya (2 Points)
    private fun calculateVashya(b: MoonData, g: MoonData): Double {
        val bVashya = getVashyaType(b.rashiIdx)
        val gVashya = getVashyaType(g.rashiIdx)
        if (bVashya == gVashya) return 2.0
        // Simplified vashya logic
        return 1.0 
    }

    private fun getVashyaType(rashi: Int): Int = when (rashi) {
        0, 1, 4, 8, 9 -> 1 // Chatushpad
        2, 5, 6, 10 -> 2 // Manav
        3, 7, 11 -> 3 // Jalchar
        else -> 1
    }

    // 3. Tara (3 Points)
    private fun calculateTara(b: MoonData, g: MoonData): Double {
        val diff1 = (g.nakshatraIdx - b.nakshatraIdx + 27) % 9
        val diff2 = (b.nakshatraIdx - g.nakshatraIdx + 27) % 9
        val score1 = if (diff1 % 3 == 0) 0.0 else 1.5
        val score2 = if (diff2 % 3 == 0) 0.0 else 1.5
        return score1 + score2
    }

    // 4. Yoni (4 Points)
    private fun calculateYoni(b: MoonData, g: MoonData): Double {
        val bYoni = getYoniType(b.nakshatraIdx)
        val gYoni = getYoniType(g.nakshatraIdx)
        if (bYoni == gYoni) return 4.0
        // Yoni compatibility table simplified
        return 2.0
    }

    private fun getYoniType(nak: Int): Int = nak % 14 // 14 animals

    // 5. Graha Maitri (5 Points)
    private fun calculateMaitri(b: MoonData, g: MoonData): Double {
        val bLord = getRashiLord(b.rashiIdx)
        val gLord = getRashiLord(g.rashiIdx)
        if (bLord == gLord) return 5.0
        return 3.0 // Simplified lord friendship
    }

    private fun getRashiLord(rashi: Int): String = when (rashi) {
        0, 7 -> "Mars"
        1, 6 -> "Venus"
        2, 5 -> "Mercury"
        3 -> "Moon"
        4 -> "Sun"
        8, 11 -> "Jupiter"
        9, 10 -> "Saturn"
        else -> "Sun"
    }

    // 6. Gana (6 Points)
    private fun calculateGana(b: MoonData, g: MoonData): Double {
        val bGana = getGanaType(b.nakshatraIdx)
        val gGana = getGanaType(g.nakshatraIdx)
        if (bGana == gGana) return 6.0
        if (bGana == 1 && gGana == 2) return 5.0
        if (bGana == 2 && gGana == 1) return 5.0
        return 1.0
    }

    private fun getGanaType(nak: Int): Int = when (nak) {
        0, 7, 12, 16, 21, 23, 26 -> 1 // Deva
        1, 3, 5, 10, 11, 19, 20, 24, 25 -> 2 // Manushya
        else -> 3 // Rakshasa
    }

    // 7. Bhakoot (7 Points)
    private fun calculateBhakoot(b: MoonData, g: MoonData): Double {
        val diff = abs(g.rashiIdx - b.rashiIdx)
        if (diff == 0) return 7.0
        if (diff == 1 || diff == 5) return 0.0 // 2/12 or 6/8
        return 7.0
    }

    // 8. Nadi (8 Points)
    private fun calculateNadi(b: MoonData, g: MoonData): Double {
        val bNadi = getNadiType(b.nakshatraIdx)
        val gNadi = getNadiType(g.nakshatraIdx)
        return if (bNadi != gNadi) 8.0 else 0.0
    }

    private fun getNadiType(nak: Int): Int = (nak % 3) // 0=Adi, 1=Madhya, 2=Antya

    private fun getRecommendation(score: Double): String = when {
        score >= 25 -> "Highly Recommended: Excellent psychological and biological compatibility."
        score >= 18 -> "Recommended: Good compatibility, minor adjustments might be needed."
        else -> "Not Recommended: Significant differences in temperament or biological factors."
    }
}
