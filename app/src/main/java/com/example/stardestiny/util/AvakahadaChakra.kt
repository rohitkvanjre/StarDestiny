package com.example.stardestiny.util

/**
 * Avakahada Chakra Utility for mapping Nakshatra and Pada to traditional syllables.
 */
object AvakahadaChakra {

    private val MAPPING = mapOf(
        "Ashwini" to listOf("Chu", "Che", "Cho", "La"),
        "Bharani" to listOf("Lee", "Lu", "Le", "Lo"),
        "Krittika" to listOf("A", "Ee", "U", "E"),
        "Rohini" to listOf("O", "Va", "Vi", "Vu"),
        "Mrigashirsha" to listOf("Ve", "Vo", "Ka", "Ke"),
        "Ardra" to listOf("Ku", "Gha", "Ng", "Chha"),
        "Punarvasu" to listOf("Ke", "Ko", "Ha", "Hi"),
        "Pushya" to listOf("Hu", "He", "Ho", "Da"),
        "Ashlesha" to listOf("Dee", "Du", "De", "Do"),
        "Magha" to listOf("Ma", "Mi", "Mu", "Me"),
        "Purva Phalguni" to listOf("Mo", "Ta", "Ti", "Tu"),
        "Uttara Phalguni" to listOf("Te", "To", "Pa", "Pi"),
        "Hasta" to listOf("Pu", "Sha", "Na", "Tha"),
        "Chitra" to listOf("Pe", "Po", "Ra", "Ri"),
        "Swati" to listOf("Ru", "Re", "Ro", "Ta"),
        "Vishakha" to listOf("Ti", "Tu", "Te", "To"),
        "Anuradha" to listOf("Na", "Ni", "Nu", "Ne"),
        "Jyeshtha" to listOf("No", "Ya", "Yi", "Yu"),
        "Mula" to listOf("Ye", "Yo", "Ba", "Bi"),
        "Purva Ashadha" to listOf("Bu", "Dha", "Pha", "Dha"),
        "Uttara Ashadha" to listOf("Bhe", "Bho", "Ja", "Ji"),
        "Shravana" to listOf("Khi", "Khu", "Khe", "Kho"),
        "Dhanishtha" to listOf("Ga", "Gi", "Gu", "Ge"),
        "Shatabhisha" to listOf("Go", "Sa", "Si", "Su"),
        "Purva Bhadrapada" to listOf("Se", "So", "Da", "Di"),
        "Uttara Bhadrapada" to listOf("Du", "Tha", "Jha", "Na"),
        "Revati" to listOf("De", "Do", "Cha", "Chi")
    )

    /**
     * Returns the recommended naming letters for a given nakshatra and pada.
     * @param nakshatra The name of the Nakshatra.
     * @param pada The Pada number (1-4).
     * @return A pair where the first is the primary syllable (for the specific pada) 
     *         and the second is the list of all syllables for that Nakshatra.
     */
    fun getNamingLetters(nakshatra: String, pada: Int): Pair<String, List<String>> {
        val syllables = MAPPING[nakshatra] ?: listOf("A", "B", "C", "D")
        val primary = syllables.getOrElse(pada - 1) { syllables[0] }
        return Pair(primary, syllables)
    }
}
