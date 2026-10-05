package com.example.data.model

data class Committee(
    val id: String,
    val name: String,
    val acronym: String,
    val description: String,
    val defaultSpeakingTimeSeconds: Int,
    val agendaOptions: List<String>,
    val allowedCountries: List<Country> = StandardCountries.ALL
)

object StandardCommittees {
    val ALL = listOf(
        Committee(
            id = "unsc",
            name = "United Nations Security Council",
            acronym = "UNSC",
            description = "The primary organ responsible for maintaining international peace and security. Features 15 member states and veto power for permanent members.",
            defaultSpeakingTimeSeconds = 90,
            agendaOptions = listOf(
                "Regulation and Verification of Lethal Autonomous Weapon Systems (LAWS)",
                "Security Ramifications of State-Sponsored Cyberwarfare & Critical Infrastructure Threats",
                "Non-Proliferation and Crisis De-escalation in Maritime Chokepoints"
            )
        ),
        Committee(
            id = "unhrc",
            name = "United Nations Human Rights Council",
            acronym = "UNHRC",
            description = "Fostering global human rights protection, addressing systemic violations, and developing normative international humanitarian standards.",
            defaultSpeakingTimeSeconds = 60,
            agendaOptions = listOf(
                "Protection and Legal Status of Persons Displaced by Rapid Climate Disasters",
                "Human Rights Safeguards Regarding Artificial Intelligence Facial Surveillance",
                "Protecting Freedom of the Press and Whistleblowers in Conflict Zones"
            )
        ),
        Committee(
            id = "disec",
            name = "General Assembly 1st Committee: DISEC",
            acronym = "DISEC",
            description = "Disarmament and International Security Committee dealing with disarmament, global threats, and peace challenges.",
            defaultSpeakingTimeSeconds = 75,
            agendaOptions = listOf(
                "Prevention of an Arms Race in Outer Space (PAROS)",
                "Combating the Illicit Trafficking of Small Arms and Light Weapons (SALW)",
                "Nuclear Non-Proliferation in Regions of Heightened Geopolitical Tension"
            )
        ),
        Committee(
            id = "unep",
            name = "United Nations Environment Programme",
            acronym = "UNEP",
            description = "Coordinating responses to environmental issues within the United Nations system.",
            defaultSpeakingTimeSeconds = 60,
            agendaOptions = listOf(
                "Establishing Legally Binding Treaties on Global Plastic Waste and Ocean Dumping",
                "Financing Loss and Damage Mechanisms for Vulnerable Developing Economies"
            )
        )
    )
}
