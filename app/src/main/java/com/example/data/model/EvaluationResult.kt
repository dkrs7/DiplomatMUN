package com.example.data.model

data class EvaluationResult(
    val overallScore: Int,
    val userAge: Int,
    val ageBracket: String,
    val ageRubricContext: String,
    val awardTier: String,
    val awardIcon: String,
    val substantivePolicyScore: Int,
    val parliamentaryScore: Int,
    val rhetoricScore: Int,
    val timeManagementScore: Int,
    val strategicConsensusScore: Int,
    val strengths: List<String>,
    val areasForImprovement: List<String>,
    val chairSummaryRemarks: String
)

object AgeRubricHelper {
    fun getBracket(age: Int): String {
        return when {
            age <= 14 -> "Junior Delegate Division (Ages 12-14)"
            age in 15..18 -> "High School / Secondary Division (Ages 15-18)"
            age in 19..23 -> "University / Collegiate Division (Ages 19-23)"
            else -> "Senior / Professional Division (Ages 24+)"
        }
    }

    fun getRubricDescription(age: Int): String {
        return when {
            age <= 14 -> "Evaluation calibrated for Junior Delegates: Emphasizes core decorum, courage of delivery, basic RoP comprehension, and clear national position representation."
            age in 15..18 -> "Evaluation calibrated for High School Delegates: Assesses adherence to third-person diplomatic address, structured argumentation (problem-solution-action), caucus leadership, and crisp time management."
            age in 19..23 -> "Evaluation calibrated for Collegiate Delegates: Demands deep geopolitical realism, subtle multilateral negotiations, strategic resolution drafting, and counter-rhetorical agility."
            else -> "Evaluation calibrated for Senior/Professional Delegates: Strict international law standards, nuanced multilateral statecraft, coalition diplomacy, and rigorous procedural mastery."
        }
    }
}
