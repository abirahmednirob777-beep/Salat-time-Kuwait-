package com.example.model

enum class DhikrCategory(
    val titleEn: String,
    val titleAr: String,
    val iconName: String
) {
    MORNING("Morning Athkar", "أذكار الصباح", "WbSunny"),
    EVENING("Evening Athkar", "أذكار المساء", "Nightlight"),
    POST_PRAYER("After Prayer", "أذكار بعد الصلاة", "Mosque"),
    SLEEP("Before Sleep", "أذكار النوم", "Bedtime"),
    DAILY_DUA("Daily Duas", "أدعية يومية", "Favorite")
}

data class DhikrItem(
    val id: String,
    val category: DhikrCategory,
    val arabicText: String,
    val transliteration: String,
    val englishTranslation: String,
    val benefitOrSource: String,
    val targetCount: Int,
    var currentCount: Int = 0
)
