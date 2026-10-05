package com.example.data.model

data class Country(
    val code: String,
    val name: String,
    val flagEmoji: String,
    val isP5: Boolean = false,
    val bloc: String,
    val standardStance: String
)

object StandardCountries {
    val ALL = listOf(
        Country(
            code = "USA",
            name = "United States of America",
            flagEmoji = "🇺🇸",
            isP5 = true,
            bloc = "Western Bloc",
            standardStance = "Champions rules-based international order, democratic institutions, targeted sanctions, and multilateral verification mechanisms."
        ),
        Country(
            code = "GBR",
            name = "United Kingdom",
            flagEmoji = "🇬🇧",
            isP5 = true,
            bloc = "Western Bloc",
            standardStance = "Emphasizes international humanitarian law, diplomatic de-escalation, maritime security, and coordinated coalition response."
        ),
        Country(
            code = "FRA",
            name = "France",
            flagEmoji = "🇫🇷",
            isP5 = true,
            bloc = "European Union / P5",
            standardStance = "Advocates strategic autonomy, robust diplomatic engagement, cultural multilateralism, and strict humanitarian safeguards."
        ),
        Country(
            code = "CHN",
            name = "People's Republic of China",
            flagEmoji = "🇨🇳",
            isP5 = true,
            bloc = "Global South / P5",
            standardStance = "Strict adherence to national sovereignty, non-interference in internal affairs, infrastructure development, and win-win diplomatic solutions."
        ),
        Country(
            code = "RUS",
            name = "Russian Federation",
            flagEmoji = "🇷🇺",
            isP5 = true,
            bloc = "Eurasian / P5",
            standardStance = "Demands multi-polar security architecture, opposes unilateral coercive measures and Western hegemony, insists on strict procedural mandates."
        ),
        Country(
            code = "IND",
            name = "Republic of India",
            flagEmoji = "🇮🇳",
            isP5 = true,
            bloc = "Global South / BRICS",
            standardStance = "Voice of the Global South, strategic autonomy, equitable multilateralism, reformed international governance, and peaceful dialogue."
        ),
        Country(
            code = "BRA",
            name = "Federative Republic of Brazil",
            flagEmoji = "🇧🇷",
            isP5 = false,
            bloc = "Latin America / G20",
            standardStance = "Champions sustainable development, climate justice, non-proliferation, and peaceful dispute settlement without armed intervention."
        ),
        Country(
            code = "DEU",
            name = "Federal Republic of Germany",
            flagEmoji = "🇩🇪",
            isP5 = false,
            bloc = "European Union",
            standardStance = "Focuses on rules-based multilateralism, green energy transition, international court jurisdiction, and humanitarian aid distribution."
        ),
        Country(
            code = "JPN",
            name = "Japan",
            flagEmoji = "🇯🇵",
            isP5 = false,
            bloc = "Asia-Pacific",
            standardStance = "Commitment to pacifist diplomacy, nuclear disarmament, disaster resilience, and Indo-Pacific regional stability."
        ),
        Country(
            code = "ZAF",
            name = "Republic of South Africa",
            flagEmoji = "🇿🇦",
            isP5 = false,
            bloc = "African Union / BRICS",
            standardStance = "Human rights universalism, African multilateral solutions to African challenges, opposition to historical economic disparities."
        ),
        Country(
            code = "ARE",
            name = "United Arab Emirates",
            flagEmoji = "🇦🇪",
            isP5 = false,
            bloc = "Middle East / Arab League",
            standardStance = "Bridge-building diplomacy, regional de-escalation, economic diversification, and technological governance frameworks."
        )
    )

    fun find(code: String): Country = ALL.firstOrNull { it.code == code } ?: ALL.first()
}
