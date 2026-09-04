package com.example.smriti.ai

import com.example.smriti.data.model.GameSession
import com.example.smriti.data.model.Reminder
import kotlin.math.roundToInt

object CognitiveAI {

    data class AnalysisResult(
        val status: String,
        val recommendation: String,
        val nextDifficulty: Int,
        val trend: String
    )

    fun calculateDifficulty(currentDifficulty: Int, score: Int): Int {
        return when {
            score >= 80 -> (currentDifficulty + 1).coerceAtMost(5)
            score in 50..79 -> currentDifficulty
            else -> (currentDifficulty - 1).coerceAtLeast(1)
        }
    }

    fun getSequenceLength(difficulty: Int): Int {
        return when (difficulty) {
            1 -> 3
            2 -> 4
            3 -> 5
            4 -> 6
            else -> 7
        }
    }

    fun getDisplaySpeedMs(difficulty: Int): Long {
        return when (difficulty) {
            1 -> 900L
            2 -> 750L
            3 -> 650L
            4 -> 550L
            else -> 450L
        }
    }

    fun analyzeSession(
        score: Int,
        accuracy: Double,
        reactionTime: Double,
        mistakes: Int,
        currentDifficulty: Int,
        recentSessions: List<GameSession>
    ): AnalysisResult {
        val nextDifficulty = calculateDifficulty(currentDifficulty, score)

        val status: String
        val recommendation: String

        if (score >= 85 && accuracy >= 90.0) {
            status = "Sharp & High Engagement"
            recommendation = if (nextDifficulty > currentDifficulty) {
                "Bohat badhiya focus ji! Level $nextDifficulty par chalte hain dimaag ko aur active rakhne ke liye."
            } else {
                "Shaandaar cognitive performance! Roz isi tarah thoda practice dimaag ko bilkul fit rakhta hai."
            }
        } else if (score >= 60) {
            status = "Steady & Focused"
            recommendation = "Acchi consistency hai ji! Memory aur reaction time ka balance bohot accha hai. Keep it up!"
        } else if (reactionTime > 3.0 || mistakes >= 3) {
            status = "Mild Fatigue Detected"
            recommendation = "Thoda thakaan lag rahi hai ji. Aankhon ko aaram dijiye, thoda paani pijiye, aur thodi der baad kheliye."
        } else {
            status = "Needs Gentle Support"
            recommendation = "Aapke aaram ke hisaab se Level $nextDifficulty set kiya hai. Bina kisi jaldbazi ke aaram se kheliye."
        }

        val trend = evaluateTrend(recentSessions, score)

        return AnalysisResult(
            status = status,
            recommendation = recommendation,
            nextDifficulty = nextDifficulty,
            trend = trend
        )
    }

    private fun evaluateTrend(recentSessions: List<GameSession>, currentScore: Int): String {
        if (recentSessions.isEmpty()) return "Baseline Established"
        val pastScores = recentSessions.take(5).map { it.score }
        val avgPast = pastScores.average()
        return when {
            currentScore > avgPast + 8 -> "Positive Upward Trend ↗"
            currentScore < avgPast - 8 -> "Slight Dip - Rest Suggested ↘"
            else -> "Stable & Reliable ↔"
        }
    }

    fun getCaregiverSummary(sessions: List<GameSession>): CaregiverMetrics {
        if (sessions.isEmpty()) {
            return CaregiverMetrics(
                totalGames = 0,
                avgScore = 0,
                avgAccuracy = 0.0,
                avgReactionTime = 0.0,
                bestScore = 0,
                currentLevel = 1,
                overallStatus = "Awaiting First Session",
                insight = "Encourage your loved one to play their first cognitive sequence game today!",
                trend = "No Data"
            )
        }

        val totalGames = sessions.size
        val avgScore = sessions.map { it.score }.average().roundToInt()
        val avgAccuracy = ((sessions.map { it.accuracy }.average() * 10).roundToInt()) / 10.0
        val avgReactionTime = ((sessions.map { it.reactionTimeSeconds }.average() * 10).roundToInt()) / 10.0
        val bestScore = sessions.maxOf { it.score }
        val latest = sessions.first()
        val currentLevel = latest.difficulty

        val status = when {
            avgScore >= 80 -> "Optimal Cognitive Engagement"
            avgScore >= 60 -> "Stable & Active Cognitive Function"
            avgScore >= 45 -> "Moderate Engagement - Encouragement Helpful"
            else -> "Mild Fatigue Pattern - Gentle Routine Advised"
        }

        val insight = when {
            latest.reactionTimeSeconds < 1.8 && latest.accuracy > 85 ->
                "Working memory speed is swift and confident. Excellent response times across recent sessions."
            latest.mistakes > 2 ->
                "Noticed slightly higher error rate in late rounds. Best to schedule sessions in the morning after breakfast."
            else ->
                "Overall cognitive indicators remain stable. Consistent participation reinforces brain neuroplasticity."
        }

        val trend = if (sessions.size >= 2) {
            val recent3 = sessions.take(3).map { it.score }.average()
            val older3 = sessions.drop(3).take(3).map { it.score }
            if (older3.isNotEmpty()) {
                val olderAvg = older3.average()
                if (recent3 >= olderAvg + 5) "Improving (+${(recent3 - olderAvg).roundToInt()} pts)"
                else if (recent3 <= olderAvg - 5) "Gentle Decrease (-${(olderAvg - recent3).roundToInt()} pts)"
                else "Consistent"
            } else "Normal"
        } else "Initial Phase"

        return CaregiverMetrics(
            totalGames = totalGames,
            avgScore = avgScore,
            avgAccuracy = avgAccuracy,
            avgReactionTime = avgReactionTime,
            bestScore = bestScore,
            currentLevel = currentLevel,
            overallStatus = status,
            insight = insight,
            trend = trend
        )
    }

    data class CaregiverMetrics(
        val totalGames: Int,
        val avgScore: Int,
        val avgAccuracy: Double,
        val avgReactionTime: Double,
        val bestScore: Int,
        val currentLevel: Int,
        val overallStatus: String,
        val insight: String,
        val trend: String
    )

    fun processVoiceQuery(
        query: String,
        reminders: List<Reminder>,
        sessions: List<GameSession>
    ): VoiceResponse {
        val clean = query.trim().lowercase()
        return when {
            clean.contains("hello") || clean.contains("hi") || clean.contains("namaste") || clean.contains("नमस्ते") || clean.contains("kaise ho") || clean.contains("kaisa") || clean.contains("kya haal") || clean.contains("good morning") || clean.contains("pranam") -> {
                VoiceResponse(
                    text = "Namaste ji! Main aapki Smriti AI assistant hoon. Aaj aap kaisa feel kar rahe hain? Aap brain exercise khel sakte hain ya medicines check kar sakte hain.",
                    action = VoiceAction.NONE
                )
            }
            clean.contains("medicine") || clean.contains("medication") || clean.contains("remind") || clean.contains("दवा") || clean.contains("dawa") || clean.contains("dawai") || clean.contains("goli") || clean.contains("tablet") || clean.contains("dose") -> {
                val active = reminders.filter { it.isEnabled }
                if (active.isEmpty()) {
                    VoiceResponse(
                        text = "Aapke paas abhi koi pending medicine reminder nahi hai ji. Sab schedule par hai!",
                        action = VoiceAction.SHOW_REMINDERS
                    )
                } else {
                    val summary = active.take(3).joinToString(", ") { "${it.title} (${it.time})" }
                    VoiceResponse(
                        text = "Aapke ${active.size} active medicine reminders hain: $summary. Time par dawa zaroor le lijiye ji!",
                        action = VoiceAction.SHOW_REMINDERS
                    )
                }
            }
            clean.contains("timeline") || clean.contains("memories") || clean.contains("reminisce") || clean.contains("cherish") || clean.contains("यादें") || clean.contains("yaad") || clean.contains("yaadein") || clean.contains("life story") || clean.contains("photo") || clean.contains("picture") || clean.contains("purani") -> {
                VoiceResponse(
                    text = "Aapki Stored Memories timeline open kar rahe hain. Chaliye purani pyari yaadein aur parivaar ke moments dekhte hain!",
                    action = VoiceAction.SHOW_MEMORIES
                )
            }
            clean.contains("game") || clean.contains("exercise") || clean.contains("puzzle") || clean.contains("sequence") || clean.contains("start") || clean.contains("खेल") || clean.contains("khel") || clean.contains("shuru") || clean.contains("play") || clean.contains("khelo") -> {
                VoiceResponse(
                    text = "Bohat badhiya! Chaliye aapka memory sequence exercise shuru karte hain. Glowing blocks ko dhyaan se yaad rakhiye!",
                    action = VoiceAction.START_GAME
                )
            }
            clean.contains("score") || clean.contains("performance") || clean.contains("how am i doing") || clean.contains("प्रगति") || clean.contains("kaisa") || clean.contains("progress") || clean.contains("marks") || clean.contains("report") || clean.contains("dimag") || clean.contains("health") -> {
                if (sessions.isEmpty()) {
                    VoiceResponse(
                        text = "Aapne abhi tak koi session nahi khela hai ji. 'Start Memory Exercise' par tap karke score banayein!",
                        action = VoiceAction.NONE
                    )
                } else {
                    val latest = sessions.first()
                    VoiceResponse(
                        text = "Aapke last session ka score tha ${latest.score} with ${latest.accuracy}% accuracy Level ${latest.difficulty} par. Shaandaar performance ji, bilkul active dimaag!",
                        action = VoiceAction.NONE
                    )
                }
            }
            clean.contains("dashboard") || clean.contains("caregiver") || clean.contains("doctor") || clean.contains("family") || clean.contains("beta") || clean.contains("beti") -> {
                VoiceResponse(
                    text = "Caregiver Dashboard open kar rahe hain, jahan par aapki poori cognitive health report saved hai.",
                    action = VoiceAction.SHOW_DASHBOARD
                )
            }
            clean.contains("water") || clean.contains("paani") || clean.contains("pani") || clean.contains("pyaas") -> {
                VoiceResponse(
                    text = "Haan ji, thoda taaza paani zaroor pijiye! Hydrated rehne se dimaag aur shareer dono active rehte hain.",
                    action = VoiceAction.NONE
                )
            }
            clean.contains("thank") || clean.contains("shukriya") || clean.contains("dhanyawad") || clean.contains("dhanyavaad") -> {
                VoiceResponse(
                    text = "Aapka bohat bohat swagat hai ji! Main hamesha aapki madad ke liye yahan hoon.",
                    action = VoiceAction.NONE
                )
            }
            else -> {
                VoiceResponse(
                    text = "Maine suna: '$query'. Aap pooch sakte hain: 'Meri medicines kya hain?', 'Memory game khelo', 'Mera score kaisa hai', ya 'Purani yaadein dikhao'.",
                    action = VoiceAction.NONE
                )
            }
        }
    }

    enum class VoiceAction {
        NONE, START_GAME, SHOW_REMINDERS, SHOW_DASHBOARD, SHOW_MEMORIES
    }

    data class VoiceResponse(
        val text: String,
        val action: VoiceAction
    )
}
