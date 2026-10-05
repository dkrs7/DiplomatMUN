package com.example.data.model

enum class SpeakerRole {
    CHAIR,
    DELEGATE,
    USER
}

enum class DebatePhase {
    ROLL_CALL,
    GENERAL_SPEAKERS_LIST,
    MODERATED_CAUCUS,
    UNMODERATED_CAUCUS,
    VOTING_PROCEDURE,
    ADJOURNED
}

data class MunSpeech(
    val id: String,
    val speakerRole: SpeakerRole,
    val speakerName: String,
    val countryCode: String? = null,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val phase: DebatePhase = DebatePhase.GENERAL_SPEAKERS_LIST,
    val timeUsedSeconds: Int = 0,
    val yieldChoice: String? = null
)

data class MunMotion(
    val id: String,
    val type: MotionType,
    val proposedByCountryCode: String,
    val topic: String = "",
    val totalMinutes: Int = 10,
    val speakingSeconds: Int = 60,
    var votesInFavor: Int = 0,
    var votesAgainst: Int = 0,
    var passed: Boolean? = null
)

enum class MotionType(val title: String, val description: String) {
    MODERATED_CAUCUS("Moderated Caucus", "Structured debate on a specific sub-issue with fixed speaking time."),
    UNMODERATED_CAUCUS("Unmoderated Caucus", "Informal consultation where delegates lobby and draft resolutions."),
    POINT_OF_ORDER("Point of Order", "Raised when the Rules of Procedure are not being properly observed."),
    POINT_OF_PARLIAMENTARY_INQUIRY("Point of Parliamentary Inquiry", "Question to the Chair regarding procedural rules."),
    POINT_OF_PERSONAL_PRIVILEGE("Point of Personal Privilege", "Raised due to personal discomfort (e.g. audibility)."),
    INTRODUCE_DRAFT_RESOLUTION("Introduce Draft Resolution", "Move to introduce and debate a sponsored draft resolution.")
}
