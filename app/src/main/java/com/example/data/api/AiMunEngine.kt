package com.example.data.api

import android.util.Log
import com.example.data.model.AgeRubricHelper
import com.example.data.model.Committee
import com.example.data.model.Country
import com.example.data.model.DebatePhase
import com.example.data.model.EvaluationResult
import com.example.data.model.MotionType
import com.example.data.model.MunMotion
import com.example.data.model.MunSpeech
import com.example.data.model.StandardCountries
import org.json.JSONArray
import org.json.JSONObject

object AiMunEngine {
    private const val TAG = "AiMunEngine"

    suspend fun generateChairpersonIntro(committee: Committee, topic: String): String {
        val prompt = """
            You are the Chairperson (President) of the ${committee.name} (${committee.acronym}).
            The agenda topic before the committee is: "$topic".
            Welcome the honorable delegates in formal UN Model United Nations diplomatic decorum.
            State the topic, announce that the General Speakers' List is now open, remind delegates of the Rules of Procedure, and invite delegations to take their seats.
            Keep it strictly between 2 to 3 sentences, extremely dignified and authoritative.
        """.trimIndent()

        val aiResponse = GeminiApiClient.generateContent(
            prompt = prompt,
            systemInstruction = "You are an experienced United Nations committee chairperson presiding over a formal MUN session.",
            temperature = 0.5
        )

        return aiResponse?.trim() ?: "The Dais calls the ${committee.name} to order. The agenda before the committee is \"$topic\". The General Speakers' List is now officially open; delegates wishing to be placed on the list are requested to raise their placards."
    }

    suspend fun generateDelegateSpeech(
        speakerCountry: Country,
        userCountry: Country,
        committee: Committee,
        topic: String,
        lastSpeeches: List<MunSpeech>,
        phase: DebatePhase,
        caucusTopic: String? = null
    ): String {
        val recentContext = lastSpeeches.takeLast(3).joinToString("\n") { 
            "${it.speakerName}: \"${it.text.take(150)}\"" 
        }

        val prompt = """
            You are the distinguished delegate of ${speakerCountry.name} (${speakerCountry.code}) in the ${committee.acronym}.
            Topic: "$topic"
            ${if (caucusTopic != null) "Current Moderated Caucus Topic: \"$caucusTopic\"" else "Phase: General Speakers' List"}
            Your country's official geopolitical foreign policy stance: "${speakerCountry.standardStance}"
            
            Recent debate context from the floor:
            $recentContext
            
            Write a formal, powerful Model United Nations speech (approx 60 to 90 words).
            Rules:
            1. Never use first-person singular pronouns like "I" or "my". Use third-person or collective ("The delegation of ${speakerCountry.name} firmly believes...", "Our government maintains...").
            2. Address the honorable Chairperson and fellow delegates.
            3. Highlight your country's national interest, address the arguments raised by other nations (including ${userCountry.name} if relevant), and propose concrete diplomatic measures.
            4. Conclude by yielding the floor (e.g., "The delegate yields the remaining time to the Chair").
        """.trimIndent()

        val aiResponse = GeminiApiClient.generateContent(
            prompt = prompt,
            systemInstruction = "You are an elite diplomatic delegate representing ${speakerCountry.name} in an official UN debate.",
            temperature = 0.7
        )

        if (!aiResponse.isNullOrBlank()) {
            return aiResponse.trim()
        }

        // Contextual Fallback
        return getFallbackDelegateSpeech(speakerCountry, topic, caucusTopic)
    }

    suspend fun evaluateDelegatePerformance(
        userAge: Int,
        userName: String,
        userCountry: Country,
        committee: Committee,
        topic: String,
        userSpeeches: List<MunSpeech>,
        totalConferenceSpeeches: Int,
        raisedMotionsCount: Int
    ): EvaluationResult {
        val userSpeechText = userSpeeches.joinToString("\n---\n") { it.text }
        val bracket = AgeRubricHelper.getBracket(userAge)
        val rubricDesc = AgeRubricHelper.getRubricDescription(userAge)

        val prompt = """
            Evaluate a Model United Nations delegate following strict UN scoring criteria, calibrated for their age.
            
            Delegate Profile:
            - Name: $userName
            - Country: ${userCountry.name} (${userCountry.code})
            - Age: $userAge (Bracket: $bracket)
            - Committee: ${committee.name} (${committee.acronym})
            - Topic: $topic
            - Age Rubric Guidance: $rubricDesc
            
            Delegate Speeches delivered:
            $userSpeechText
            
            Conference Activity:
            - Speeches delivered: ${userSpeeches.size}
            - Motions / Points raised: $raisedMotionsCount
            
            Return a JSON object strictly adhering to this structure:
            {
              "overallScore": (integer between 65 and 98),
              "awardTier": (string: "Best Delegate (Gavel)" if 93+, "Outstanding Delegate" if 88-92, "Honorable Mention" if 80-87, "Verbal Commendation" if 73-79, or "Distinguished Delegate"),
              "substantivePolicyScore": (integer 60-100),
              "parliamentaryScore": (integer 60-100),
              "rhetoricScore": (integer 60-100),
              "timeManagementScore": (integer 60-100),
              "strategicConsensusScore": (integer 60-100),
              "strengths": [(3 concise bullet points praising specific diplomatic strengths)],
              "areasForImprovement": [(3 actionable recommendations for improvement)],
              "chairSummaryRemarks": (A 2-3 sentence formal diplomatic evaluation by the Chairperson)
            }
        """.trimIndent()

        val aiResponse = GeminiApiClient.generateContent(
            prompt = prompt,
            systemInstruction = "You are the Head Judge and Committee Director of an international Model United Nations conference. Return pure valid JSON only.",
            temperature = 0.4
        )

        if (!aiResponse.isNullOrBlank()) {
            try {
                val cleanJson = aiResponse.replace("```json", "").replace("```", "").trim()
                val obj = JSONObject(cleanJson)

                val overallScore = obj.optInt("overallScore", 85)
                val awardTier = obj.optString("awardTier", calculateAwardTier(overallScore))
                val subScore = obj.optInt("substantivePolicyScore", 84)
                val ropScore = obj.optInt("parliamentaryScore", 86)
                val rhetoricScore = obj.optInt("rhetoricScore", 83)
                val timeScore = obj.optInt("timeManagementScore", 87)
                val stratScore = obj.optInt("strategicConsensusScore", 82)

                val strengthsList = mutableListOf<String>()
                val sArray = obj.optJSONArray("strengths")
                if (sArray != null) {
                    for (i in 0 until sArray.length()) strengthsList.add(sArray.getString(i))
                }

                val improveList = mutableListOf<String>()
                val iArray = obj.optJSONArray("areasForImprovement")
                if (iArray != null) {
                    for (i in 0 until iArray.length()) improveList.add(iArray.getString(i))
                }

                val remarks = obj.optString("chairSummaryRemarks", "The Dais commends the delegate of ${userCountry.name} for their dedicated defense of sovereign interests and coherent presentation.")

                return EvaluationResult(
                    overallScore = overallScore,
                    userAge = userAge,
                    ageBracket = bracket,
                    ageRubricContext = rubricDesc,
                    awardTier = awardTier,
                    awardIcon = getAwardIcon(awardTier),
                    substantivePolicyScore = subScore,
                    parliamentaryScore = ropScore,
                    rhetoricScore = rhetoricScore,
                    timeManagementScore = timeScore,
                    strategicConsensusScore = stratScore,
                    strengths = if (strengthsList.isNotEmpty()) strengthsList else listOf("Articulate articulation of national interests", "Accurate representation of foreign policy", "Active participation during caucus sessions"),
                    areasForImprovement = if (improveList.isNotEmpty()) improveList else listOf("Avoid first-person personal pronouns (\"I\", \"me\") in accordance with RoP", "Provide more specific citations of international treaties and resolutions", "Engage in more aggressive coalition sponsorship during informal consultation"),
                    chairSummaryRemarks = remarks
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse AI evaluation JSON: ${e.message}")
            }
        }

        // Offline deterministic evaluation based on user age, speeches, and decorum
        return computeFallbackEvaluation(userAge, userCountry, userSpeeches, raisedMotionsCount)
    }

    private fun computeFallbackEvaluation(
        userAge: Int,
        userCountry: Country,
        userSpeeches: List<MunSpeech>,
        raisedMotionsCount: Int
    ): EvaluationResult {
        val totalWords = userSpeeches.sumOf { it.text.split("\\s+".toRegex()).size }
        val allText = userSpeeches.joinToString(" ") { it.text.lowercase() }

        // Check diplomatic RoP markers
        val hasDiplomaticPronouns = allText.contains("delegation") || allText.contains("this delegation") || allText.contains("government")
        val hasFirstPersonSlip = allText.contains(" i ") || allText.contains("my ") || allText.contains("me ")
        val hasResolutionKeywords = allText.contains("clause") || allText.contains("resolution") || allText.contains("sovereignty") || allText.contains("treaty") || allText.contains("framework")
        val hasFormalOpening = allText.contains("chair") || allText.contains("delegates") || allText.contains("honorable")

        var baseScore = 78
        if (userSpeeches.size >= 2) baseScore += 5
        if (totalWords >= 80) baseScore += 4
        if (hasDiplomaticPronouns) baseScore += 4
        if (hasFormalOpening) baseScore += 3
        if (hasResolutionKeywords) baseScore += 3
        if (raisedMotionsCount >= 1) baseScore += 2
        if (hasFirstPersonSlip) baseScore -= 4

        // Age bracket adjustment:
        // Younger delegates evaluated on clarity and courage; older on geopolitical nuance
        if (userAge <= 16 && totalWords >= 40) baseScore += 3
        if (userAge >= 20 && !hasResolutionKeywords) baseScore -= 3

        val overallScore = baseScore.coerceIn(68, 97)
        val award = calculateAwardTier(overallScore)

        val strengths = mutableListOf<String>()
        val improvements = mutableListOf<String>()

        if (hasDiplomaticPronouns) {
            strengths.add("Flawless adherence to diplomatic third-person decorum (\"This delegation asserts...\")")
        } else {
            improvements.add("Maintain strict third-person phrasing; replace personal pronouns (\"I\", \"we\") with \"The delegation of ${userCountry.name}\"")
        }

        if (totalWords >= 60) {
            strengths.add("Robust substantive exposition detailing core national priorities and legal justifications")
        } else {
            improvements.add("Elaborate further on concrete operative clauses and implementation frameworks in speeches")
        }

        if (raisedMotionsCount > 0) {
            strengths.add("Proactive parliamentary initiative in shaping the debate through motions and points")
        } else {
            improvements.add("Seize committee momentum by introducing motions for moderated caucuses on targeted topics")
        }

        if (strengths.isEmpty()) {
            strengths.add("Poised delivery and clear articulation before the committee chamber")
            strengths.add("Clear identification with national geopolitical posture")
        }
        if (improvements.isEmpty()) {
            improvements.add("Reference specific past UN General Assembly or Security Council resolutions to bolster legal standing")
            improvements.add("Challenge opposition bloc assertions with direct rebuttals during moderated caucus rounds")
        }

        val remarks = "The Dais recognizes the distinguished efforts of the delegate of ${userCountry.name}. With continued focus on precise treaty citations and caucus coalition building, this delegate shows outstanding promise for top conference honors."

        return EvaluationResult(
            overallScore = overallScore,
            userAge = userAge,
            ageBracket = AgeRubricHelper.getBracket(userAge),
            ageRubricContext = AgeRubricHelper.getRubricDescription(userAge),
            awardTier = award,
            awardIcon = getAwardIcon(award),
            substantivePolicyScore = (overallScore - 2).coerceIn(65, 99),
            parliamentaryScore = if (hasFirstPersonSlip) (overallScore - 5).coerceIn(60, 95) else (overallScore + 2).coerceIn(70, 99),
            rhetoricScore = (overallScore + 1).coerceIn(65, 99),
            timeManagementScore = (overallScore + 3).coerceIn(70, 99),
            strategicConsensusScore = (overallScore - 1).coerceIn(65, 99),
            strengths = strengths,
            areasForImprovement = improvements,
            chairSummaryRemarks = remarks
        )
    }

    private fun calculateAwardTier(score: Int): String {
        return when {
            score >= 94 -> "Best Delegate (The Gavel)"
            score >= 88 -> "Outstanding Delegate"
            score >= 81 -> "Honorable Mention"
            score >= 74 -> "Verbal Commendation"
            else -> "Distinguished Diplomatic Participation"
        }
    }

    private fun getAwardIcon(award: String): String {
        return when {
            award.contains("Best") -> "🏆"
            award.contains("Outstanding") -> "🥇"
            award.contains("Honorable") -> "🥈"
            award.contains("Verbal") -> "🎖️"
            else -> "📜"
        }
    }

    private fun getFallbackDelegateSpeech(country: Country, topic: String, caucusTopic: String?): String {
        return when (country.code) {
            "USA" -> "Honorable Chair and fellow delegates, the United States asserts that stability and the preservation of international norms must take precedence. On this matter of \"${caucusTopic ?: topic}\", we urge all delegations to adopt transparent verification mechanisms and hold rogue actors accountable. Unilateral violations cannot be normalized. The United States stands ready to lead multilateral consensus, and we yield the floor back to the Chair."
            "CHN" -> "Distinguished Dais and delegates, the People's Republic of China reiterates that state sovereignty and non-interference are the foundational pillars of the UN Charter. Concerning \"${caucusTopic ?: topic}\", solutions must not be imposed through external sanctions, but achieved through mutual respect and win-win dialogue. We urge developing nations to unite in safeguarding equitable global governance. The delegate yields the remainder of time to the Chair."
            "RUS" -> "Mr. President, the Russian Federation finds the prevailing narrative on \"${caucusTopic ?: topic}\" fundamentally one-sided. Sustainable peace cannot be attained while ignoring the legitimate security guarantees of all sovereign states. We demand strict adherence to procedural mandates rather than politically motivated rhetoric. Russia remains committed to constructive, multipolar negotiations. The delegate yields to the Chair."
            "GBR" -> "Honorable Chair, the United Kingdom emphasizes our unwavering commitment to international humanitarian law and collective security. The crisis regarding \"${caucusTopic ?: topic}\" demands coordinated multilateral resolve, robust sanctions enforcement, and unhindered humanitarian access. We invite like-minded member states to join us in drafting comprehensive operative language. The delegation yields the floor to the Chair."
            "FRA" -> "Monsieur le Président, la France believes that genuine diplomacy requires multilateral autonomy and decisive action. In addressing \"${caucusTopic ?: topic}\", we cannot allow humanitarian corridors to become pawns of geopolitical stalemate. We urge the Council to adopt a balanced, binding resolution incorporating independent oversight. France yields its remaining time to points of information."
            "IND" -> "Honorable Chairperson, the delegation of India emphasizes that no global framework on \"${caucusTopic ?: topic}\" can succeed without centering the voices and developmental priorities of the Global South. Strategic autonomy and equitable burden-sharing must guide our deliberations. India urges constructive dialogue over confrontation, and yields the remaining time to the Dais."
            else -> "Honorable Chairperson and esteemed delegates, the delegation of ${country.name} expresses its grave concern regarding \"${caucusTopic ?: topic}\". We urge this committee to prioritize peaceful settlement, respect for multilateral agreements, and tangible relief for affected populations. We call upon all member states to support our upcoming working paper, and yield our time to the Chair."
        }
    }
}
