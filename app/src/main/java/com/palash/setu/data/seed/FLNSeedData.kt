package com.palash.setu.data.seed

import com.palash.setu.data.entity.FLNDictionary

object FLNSeedData {
    val coreTerms = listOf(
        term("एक", "ᱢᱤᱫ", "मित्", "𑢨𑣉", "मित", "मिद", "math_numbers"),
        term("दो", "ᱵᱟᱨ", "बार", "𑢵𑣉𑢬", "बार", "बार", "math_numbers"),
        term("तीन", "ᱯᱮ", "पे", "𑢒𑣉", "पे", "पे", "math_numbers"),
        term("चार", "ᱯᱩᱱᱟᱹ", "पुनाः", "𑢒𑣉𑢳𑣉", "पुना", "पुना", "math_numbers"),
        term("पाँच", "ᱢᱚᱬᱮ", "मोड़े", "𑢨𑣉𑢳𑣉", "मोड़े", "मोड़े", "math_numbers"),
        term("छह", "ᱪᱷᱟᱹ", "छाः", "𑢠𑣉𑢳𑣉", "छा", "छा", "math_numbers"),
        term("सात", "ᱥᱟᱛ", "सात्", "𑢥𑣉𑢛", "सात", "सात", "math_numbers"),
        term("आठ", "ᱟᱴ", "आट्", "𑢀𑣉𑢛", "आट", "आट", "math_numbers"),
        term("नौ", "ᱱᱚᱣᱟ", "नवा", "𑢥𑣉𑢵𑣉", "नवा", "नवा", "math_numbers"),
        term("दस", "ᱜᱮᱞ", "गेल्", "𑢐𑣉𑢞", "गेल", "गेल", "math_numbers"),
        term("किताब", "ᱯᱩᱛᱷᱤ", "पुथी", "𑢒𑣉𑣈𑣉", "पुथी", "पुथी", "classroom_objects"),
        term("नमस्ते", "ᱡᱚᱦᱟᱨ", "जोहार", "𑢠𑣉𑢦𑣉𑢬", "जोहार", "जोहार", "classroom_commands"),
        term("पानी", "ᱫᱟᱜ", "दाक्", "𑢡𑣉𑢐", "दा", "दा", "environment"),
        term("पढ़ो", "ᱯᱟᱲᱦᱟᱣ", "पाढ़ाव", "𑢒𑣉𑢞𑣉𑢦𑣉𑢵", "पाढ़ाव", "पाढ़ाव", "classroom_commands"),
        term("लिखो", "ᱚᱞ", "ओल", "𑢉𑣉𑢞", "ओल", "ओल", "classroom_commands"),
        term("लाल", "ᱞᱟᱞ", "लाल", "𑢞𑣉𑢞", "लाल", "लाल", "colors"),
        term("नीला", "ᱱᱤᱞ", "नील", "𑢥𑣉𑢞", "नील", "नील", "colors"),
        term("हरा", "ᱦᱟᱹᱨᱤᱭᱟᱹ", "हारिया", "𑢦𑣉𑢬𑣉", "हरिया", "हरिया", "colors"),
        term("पीला", "ᱯᱤᱞᱟ", "पिला", "𑢒𑣉𑢞𑣉", "पिला", "पिला", "colors"),
        term("आम", "ᱟᱢ", "आम", "𑢀𑣉𑢨", "आम", "आम", "fruits"),
        term("केला", "ᱠᱮᱞᱟ", "केला", "𑢐𑣉𑢞𑣉", "केला", "केला", "fruits"),
        term("बैठो", "ᱫᱩᱞ", "दुल", "𑢡𑣉𑢞", "दुल", "दुल", "classroom_commands"),
        term("खड़े हो जाओ", "ᱛᱤᱧ ᱛᱟᱦᱮᱱ", "तिन ताहेन", "𑢛𑣉𑢥 𑢛𑣉𑢦𑣉", "तिन ताहेन", "तिन ताहेन", "classroom_commands"),
        term("सुनो", "ᱟᱭᱢᱟ", "आयमा", "𑢀𑣉𑢭𑣉", "आयमा", "आयमा", "classroom_commands"),
        term("किताब खोलो", "ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡ", "पुथी झिज", "𑢒𑣉𑣈𑣉 𑢠𑣉𑢠", "पुथी झिज", "पुथी झिज", "classroom_commands")
    )

    private fun term(
        hindi: String,
        santhaliNative: String,
        santhaliDevanagari: String,
        hoNative: String,
        hoDevanagari: String,
        mundari: String,
        category: String
    ) = FLNDictionary(
        hindiTerm = hindi,
        santhaliOlChiki = santhaliNative,
        santhaliDevanagari = santhaliDevanagari,
        hoWarangChiti = hoNative,
        hoDevanagari = hoDevanagari,
        mundariDevanagari = mundari,
        domainCategory = category,
        gradeLevel = if (category == "math_numbers") 1 else 2
    )
}
