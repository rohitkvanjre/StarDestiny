package com.example.stardestiny.util

import com.example.stardestiny.model.HoroscopeResult
import kotlin.math.*

/**
 * VedicCalculator: High-precision astronomical engine.
 * Improved for Mercury (Budha) accuracy and full geocentric projection.
 */
object VedicCalculator {

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
        "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    )

    fun calculate(day: Int, month: Int, year: Int, hour: Int, min: Int, lat: Double, lon: Double, tzone: Double = 5.5): HoroscopeResult {
        val localDecimalHour = hour + min / 60.0
        val utcDecimalHour = localDecimalHour - tzone
        val jd = calculateJulianDayUniversal(year, month, day, utcDecimalHour)
        val d = jd - 2451545.0
        val t = d / 36525.0

        // 1. High-Precision Lahiri Ayanamsha
        val ayanamsha = 23.8570925 + (50.2785 / 3600.0) * (t * 100)

        val planetLongitudes = mutableMapOf<String, Double>()

        // 2. High-Precision Moon (Chandra)
        val moonTropical = calculateMoonHighPrecision(d, t)
        planetLongitudes["Chandra"] = normalize(moonTropical - ayanamsha)

        // 3. High-Precision Sun (Surya)
        val sunTropical = calculateSunHighPrecision(d, t)
        planetLongitudes["Surya"] = normalize(sunTropical - ayanamsha)

        // 4. Rahu/Ketu
        val rahuTropical = normalize(125.1228 - 0.0529538083 * d)
        val rahuSidereal = normalize(rahuTropical - ayanamsha)
        planetLongitudes["Rahu"] = rahuSidereal
        planetLongitudes["Ketu"] = normalize(rahuSidereal + 180.0)

        // 5. Geocentric Positions for Planets
        planetLongitudes["Mangala"] = normalize(calculatePlanetGeocentric(d, t, "Mars") - ayanamsha)
        planetLongitudes["Budha"] = normalize(calculatePlanetGeocentric(d, t, "Mercury") - ayanamsha)
        planetLongitudes["Guru"] = normalize(calculatePlanetGeocentric(d, t, "Jupiter") - ayanamsha)
        planetLongitudes["Shukra"] = normalize(calculatePlanetGeocentric(d, t, "Venus") - ayanamsha)
        planetLongitudes["Shani"] = normalize(calculatePlanetGeocentric(d, t, "Saturn") - ayanamsha)

        // 6. Ascendant (Lagna)
        val siderealLagna = calculateLagna(jd, utcDecimalHour, lon, lat, ayanamsha)

        val moonSidereal = planetLongitudes["Chandra"] ?: 0.0
        val rashiIdx = (moonSidereal / 30.0).toInt() % 12
        val nakshatraIdx = (moonSidereal / (360.0 / 27.0)).toInt() % 27
        val pada = (((moonSidereal % (360.0 / 27.0)) / (360.0 / 108.0)).toInt() % 4) + 1
        val ascendantIdx = (siderealLagna / 30.0).toInt() % 12

        val rashi = RASHIS[rashiIdx]
        val nakshatra = NAKSHATRAS[nakshatraIdx]
        val planetRashis = planetLongitudes.mapValues { (it.value / 30.0).toInt() % 12 }

        val namingLetters = AvakahadaChakra.getNamingLetters(nakshatra, pada)

        return HoroscopeResult(
            rashi = rashi,
            nakshatra = nakshatra,
            pada = pada,
            luckyNumber = calculateLuckyNumber(day, month, year),
            luckyColor = getLuckyColor(rashi),
            basicDescription = "Born in $rashi Rashi under $nakshatra Nakshatra (Pada $pada). Lagna is ${RASHIS[ascendantIdx]}.",
            planetPositions = planetRashis,
            ascendantIndex = ascendantIdx,
            primaryNamingLetter = namingLetters.first,
            allNamingLetters = namingLetters.second
        )
    }

    private fun normalize(angle: Double): Double = (angle % 360 + 360) % 360

    private fun calculateJulianDayUniversal(year: Int, month: Int, day: Int, utcHour: Double): Double {
        var y = year
        var m = month
        if (m <= 2) { y -= 1; m += 12 }
        val a = y / 100
        val b = 2 - a + (a / 4)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5 + (utcHour / 24.0)
    }

    private fun calculateSunHighPrecision(d: Double, t: Double): Double {
        val l = normalize(280.46646 + 36000.76983 * t)
        val m = normalize(357.52911 + 35999.05029 * t)
        val c = (1.914602 - 0.004817 * t) * sin(Math.toRadians(m)) +
                (0.019993 - 0.000101 * t) * sin(Math.toRadians(2 * m))
        return normalize(l + c)
    }

    private fun calculateMoonHighPrecision(d: Double, t: Double): Double {
        val lp = normalize(218.3164477 + 481267.8812307 * t)
        val dMoon = normalize(297.8501921 + 445267.1114034 * t)
        val mSun = normalize(357.5291092 + 35999.0502909 * t)
        val mMoon = normalize(134.9633964 + 477198.8675055 * t)
        val f = normalize(93.2720950 + 483202.0175233 * t)
        
        val e = 1 - 0.002516 * t
        var sumLon = 6.288774 * sin(Math.toRadians(mMoon))
        sumLon += 1.274027 * sin(Math.toRadians(2 * dMoon - mMoon))
        sumLon += 0.658314 * sin(Math.toRadians(2 * dMoon))
        sumLon += 0.213618 * sin(Math.toRadians(2 * mMoon))
        sumLon -= 0.185116 * e * sin(Math.toRadians(mSun))
        sumLon -= 0.114332 * sin(Math.toRadians(2 * f))
        sumLon += 0.058793 * sin(Math.toRadians(2 * dMoon - 2 * mMoon))
        sumLon += 0.057066 * e * sin(Math.toRadians(2 * dMoon - mSun - mMoon))
        sumLon += 0.053322 * sin(Math.toRadians(2 * dMoon + mMoon))
        sumLon += 0.045758 * e * sin(Math.toRadians(2 * dMoon - mSun))

        return normalize(lp + sumLon)
    }

    private fun calculatePlanetGeocentric(d: Double, t: Double, name: String): Double {
        // Orbit elements: Node, Incl, Peri, SemiA, Ecc, MeanAnom, DailyMotion
        val params = when (name) {
            "Mercury" -> doubleArrayOf(48.3313, 3.24587E-5, 7.0047, 5.00E-8, 77.4564, 1.55447E-5, 0.387098, 0.0, 0.205635, 5.59E-10, 168.6562, 4.0923344)
            "Venus" -> doubleArrayOf(76.6799, 2.46590E-5, 3.3946, 2.75E-8, 131.5637, 1.10825E-5, 0.723330, 0.0, 0.006773, -1.302E-9, 48.0052, 1.6021302)
            "Mars" -> doubleArrayOf(49.5574, 2.11081E-5, 1.8497, -1.78E-8, 336.0408, 4.39773E-5, 1.523688, 0.0, 0.093405, 2.516E-9, 18.6021, 0.5240207)
            "Jupiter" -> doubleArrayOf(100.4542, 2.76854E-5, 1.3030, -1.557E-7, 14.7284, 7.96475E-6, 5.20256, 0.0, 0.048498, 4.469E-9, 19.8950, 0.0830853)
            "Saturn" -> doubleArrayOf(113.6634, 2.38980E-5, 2.4886, -1.081E-7, 92.4319, -1.94705E-5, 9.55475, 0.0, 0.055546, -9.499E-9, 316.9670, 0.0334442)
            else -> return 0.0
        }

        val node = normalize(params[0] + params[1] * d)
        val incl = Math.toRadians(params[2] + params[3] * d)
        val peri = normalize(params[4] + params[5] * d)
        val semiA = params[6] + params[7] * d
        val ecc = params[8] + params[9] * d
        val meanAnom = normalize(params[10] + params[11] * d)

        var eAnom = meanAnom + (180/PI) * ecc * sin(Math.toRadians(meanAnom)) * (1 + ecc * cos(Math.toRadians(meanAnom)))
        for (j in 0..2) {
            eAnom = eAnom - (eAnom - (180/PI) * ecc * sin(Math.toRadians(eAnom)) - meanAnom) / (1 - ecc * cos(Math.toRadians(eAnom)))
        }

        val xv = semiA * (cos(Math.toRadians(eAnom)) - ecc)
        val yv = semiA * (sqrt(1 - ecc * ecc) * sin(Math.toRadians(eAnom)))
        val r = sqrt(xv * xv + yv * yv)
        val v = Math.toDegrees(atan2(yv, xv))
        
        val cosN = cos(Math.toRadians(node))
        val sinN = sin(Math.toRadians(node))
        val cosVWN = cos(Math.toRadians(v + peri - node))
        val sinVWN = sin(Math.toRadians(v + peri - node))
        val cosI = cos(incl)

        // Heliocentric
        val xh = r * (cosN * cosVWN - sinN * sinVWN * cosI)
        val yh = r * (sinN * cosVWN + cosN * sinVWN * cosI)
        val zh = r * (sinVWN * sin(incl))

        // Earth Heliocentric (Precise)
        val sunL = normalize(280.46607 + 0.98564736 * d)
        val sunM = normalize(357.52910 + 0.98560028 * d)
        val sunC = 1.9148 * sin(Math.toRadians(sunM)) + 0.0200 * sin(Math.toRadians(2 * sunM))
        val sunLon = normalize(sunL + sunC)
        val distE = (1.00014 - 0.01671 * cos(Math.toRadians(sunM)))
        val xe = distE * cos(Math.toRadians(sunLon + 180))
        val ye = distE * sin(Math.toRadians(sunLon + 180))

        return normalize(Math.toDegrees(atan2(yh - ye, xh - xe)))
    }

    private fun calculateLagna(jd: Double, utcHour: Double, lon: Double, lat: Double, ayanamsha: Double): Double {
        val d = jd - 2451545.0
        val gst = normalize(6.697374558 + 0.06570982441908 * d + utcHour)
        val lst = normalize(gst + lon / 15.0)
        val ramc = lst * 15.0
        val eps = 23.4393 - 0.0000004 * d
        val top = cos(Math.toRadians(ramc))
        val bottom = -sin(Math.toRadians(ramc)) * cos(Math.toRadians(eps)) - tan(Math.toRadians(lat)) * sin(Math.toRadians(eps))
        return normalize(Math.toDegrees(atan2(top, bottom)) - ayanamsha)
    }

    private fun calculateLuckyNumber(day: Int, month: Int, year: Int): Int {
        val s = (day.toString() + month.toString() + year.toString()).filter { it.isDigit() }.sumOf { it.digitToInt() }
        var res = s
        while (res > 9) res = res.toString().map { it.digitToInt() }.sum()
        return if (res == 0) 9 else res
    }

    private fun getLuckyColor(rashi: String): String {
        return when {
            rashi.contains("Mesha") -> "Red"
            rashi.contains("Vrishabha") -> "White"
            rashi.contains("Mithuna") -> "Green"
            rashi.contains("Karka") -> "Silver"
            rashi.contains("Simha") -> "Gold"
            rashi.contains("Kanya") -> "Dark Green"
            rashi.contains("Tula") -> "Pink"
            rashi.contains("Vrischika") -> "Maroon"
            rashi.contains("Dhanu") -> "Yellow"
            rashi.contains("Makara") -> "Black"
            rashi.contains("Kumbha") -> "Blue"
            rashi.contains("Meena") -> "Sea Green"
            else -> "Orange"
        }
    }
}
