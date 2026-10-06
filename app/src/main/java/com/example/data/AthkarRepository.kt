package com.example.data

import com.example.model.DhikrCategory
import com.example.model.DhikrItem

object AthkarRepository {

    fun getAthkar(): List<DhikrItem> = listOf(
        // Post Prayer Athkar
        DhikrItem(
            id = "pp_1",
            category = DhikrCategory.POST_PRAYER,
            arabicText = "أَسْتَغْفِرُ اللهَ، أَسْتَغْفِرُ اللهَ، أَسْتَغْفِرُ اللهَ، اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالإِكْرَامِ",
            transliteration = "Astaghfirullah (x3), Allahumma Antas-Salam wa minkas-salam, tabarakta ya dhal-Jalali wal-Ikram.",
            englishTranslation = "I seek Allah's forgiveness (3x). O Allah, You are Peace and from You comes peace. Blessed are You, O Owner of majesty and honor.",
            benefitOrSource = "Sahih Muslim 591 - Recited right after concluding Fard prayer.",
            targetCount = 1
        ),
        DhikrItem(
            id = "pp_2",
            category = DhikrCategory.POST_PRAYER,
            arabicText = "سُبْحَانَ اللهِ (٣٣)، الحَمْدُ للهِ (٣٣)، اللهُ أَكْبَرُ (٣٣)",
            transliteration = "Subhanallah (33x), Alhamdulillah (33x), Allahu Akbar (33x)",
            englishTranslation = "Glory be to Allah (33x), Praise be to Allah (33x), Allah is the Greatest (33x).",
            benefitOrSource = "Sahih Muslim 597 - Sins will be forgiven even if like the foam of the sea.",
            targetCount = 33
        ),
        DhikrItem(
            id = "pp_3",
            category = DhikrCategory.POST_PRAYER,
            arabicText = "لا إِلَهَ إِلاَّ اللهُ وَحْدَهُ لا شَرِيكَ لَهُ، لَهُ المُلْكُ وَلَهُ الحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "La ilaha illallahu wahdahu la shareeka lah, lahul-mulku wa lahul-hamdu wa Huwa 'ala kulli shay'in Qadeer.",
            englishTranslation = "There is no deity worthy of worship except Allah alone, without partner. To Him belongs all sovereignty and praise, and He has power over all things.",
            benefitOrSource = "Sahih Bukhari & Muslim - Seal the 100th dhikr after prayer.",
            targetCount = 1
        ),
        DhikrItem(
            id = "pp_4",
            category = DhikrCategory.POST_PRAYER,
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ",
            transliteration = "Allahu la ilaha illa Huwal-Hayyul-Qayyum...",
            englishTranslation = "Ayat al-Kursi (The Throne Verse - Surah Al-Baqarah 2:255).",
            benefitOrSource = "An-Nasa'i - Whoever recites it after each obligatory prayer, nothing stands between him and Paradise except death.",
            targetCount = 1
        ),

        // Morning Athkar
        DhikrItem(
            id = "m_1",
            category = DhikrCategory.MORNING,
            arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "Asbahna wa-asbahal-mulku lillah, wal-hamdu lillah, la ilaha illallahu wahdahu la shareeka lah...",
            englishTranslation = "We have entered a new morning and to Allah belongs the kingdom. All praise is for Allah. None has the right to be worshipped except Allah alone...",
            benefitOrSource = "Sahih Muslim 2723 - Morning remembrance of sovereignty.",
            targetCount = 1
        ),
        DhikrItem(
            id = "m_2",
            category = DhikrCategory.MORNING,
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu...",
            englishTranslation = "Sayyid al-Istighfar (The Master Supplication for Forgiveness). O Allah, You are my Lord, none has the right to be worshipped except You...",
            benefitOrSource = "Sahih Bukhari 6306 - Whoever recites it during the morning with conviction and dies will be among the people of Paradise.",
            targetCount = 1
        ),
        DhikrItem(
            id = "m_3",
            category = DhikrCategory.MORNING,
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahil-ladhi la yadurru ma'as-mihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Alim.",
            englishTranslation = "In the Name of Allah, with Whose Name nothing can cause harm in the earth nor in the heavens, and He is the All-Hearing, the All-Knowing.",
            benefitOrSource = "Abu Dawud & Tirmidhi - Recite 3 times; no harm will touch the believer.",
            targetCount = 3
        ),
        DhikrItem(
            id = "m_4",
            category = DhikrCategory.MORNING,
            arabicText = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالإِسْلاَمِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
            transliteration = "Raditu billahi Rabba, wa bil-Islami deena, wa bi-Muhammadin sallallahu 'alayhi wa sallama Nabiyya.",
            englishTranslation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad (ﷺ) as my Prophet.",
            benefitOrSource = "Abu Dawud 5072 - Allah takes it upon Himself to please whoever says this 3 times.",
            targetCount = 3
        ),

        // Evening Athkar
        DhikrItem(
            id = "e_1",
            category = DhikrCategory.EVENING,
            arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "Amsayna wa-amsal-mulku lillah, wal-hamdu lillah, la ilaha illallahu wahdahu la shareeka lah...",
            englishTranslation = "We have reached the evening and to Allah belongs the kingdom. All praise is for Allah...",
            benefitOrSource = "Sahih Muslim 2723 - Evening supplication.",
            targetCount = 1
        ),
        DhikrItem(
            id = "e_2",
            category = DhikrCategory.EVENING,
            arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            transliteration = "A'udhu bi-kalimatil-lahit-tammati min sharri ma khalaq.",
            englishTranslation = "I seek refuge in the perfect words of Allah from the evil of what He has created.",
            benefitOrSource = "Sahih Muslim 2709 - Recite 3 times at evening; protected against venomous and harmful creatures.",
            targetCount = 3
        ),
        DhikrItem(
            id = "e_3",
            category = DhikrCategory.EVENING,
            arabicText = "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لاَ إِلَهَ إِلاَّ أَنْتَ",
            transliteration = "Allahumma 'afini fi badani, Allahumma 'afini fi sam'i, Allahumma 'afini fi basari, la ilaha illa Anta.",
            englishTranslation = "O Allah, grant soundness to my body; O Allah, grant soundness to my hearing; O Allah, grant soundness to my sight. None has the right to be worshipped except You.",
            benefitOrSource = "Abu Dawud 5090 - Recited morning and evening for health & wellbeing.",
            targetCount = 3
        ),

        // Sleep Athkar
        DhikrItem(
            id = "s_1",
            category = DhikrCategory.SLEEP,
            arabicText = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي وَبِكَ أَرْفَعُهُ، إِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ",
            transliteration = "Bismika Rabbi wada'tu janbi wa bika arfa'uh, in amsakta nafsi farhamha, wa in arsaltaha fahfazha...",
            englishTranslation = "In Your Name, my Lord, I lay down my side and by You I raise it up. If You take my soul, have mercy upon it; and if You send it back, protect it as You protect Your righteous slaves.",
            benefitOrSource = "Sahih Bukhari 6320 & Muslim 2714 - Sleeping protection.",
            targetCount = 1
        ),
        DhikrItem(
            id = "s_2",
            category = DhikrCategory.SLEEP,
            arabicText = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            transliteration = "Bismikallahumma amutu wa ahya.",
            englishTranslation = "In Your Name, O Allah, I die and I live.",
            benefitOrSource = "Sahih Bukhari 6312 - When preparing for rest.",
            targetCount = 1
        ),

        // Daily Duas
        DhikrItem(
            id = "d_1",
            category = DhikrCategory.DAILY_DUA,
            arabicText = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            transliteration = "Rabbana atina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina 'adhaban-nar.",
            englishTranslation = "Our Lord! Grant us that which is good in this life and that which is good in the Hereafter, and save us from the torment of the Fire.",
            benefitOrSource = "Surah Al-Baqarah 2:201 - Most frequent supplication of Prophet Muhammad (ﷺ).",
            targetCount = 3
        ),
        DhikrItem(
            id = "d_2",
            category = DhikrCategory.DAILY_DUA,
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلاً مُتَقَبَّلاً",
            transliteration = "Allahumma inni as'aluka 'ilman nafi'an, wa rizqan tayyiban, wa 'amalan mutaqabbala.",
            englishTranslation = "O Allah, I ask You for beneficial knowledge, wholesome sustenance, and accepted deeds.",
            benefitOrSource = "Ibn Majah 925 - Recommended daily after Fajr prayer.",
            targetCount = 1
        )
    )
}
