package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class SscRepository(private val dao: SscPrepDao) {

    val allMockTestsFlow: Flow<List<MockTest>> = dao.getAllMockTestsFlow()
    val allAttemptsFlow: Flow<List<MockAttempt>> = dao.getAllAttemptsFlow()
    val allStudyMaterialsFlow: Flow<List<StudyMaterial>> = dao.getAllStudyMaterialsFlow()
    val preferencesFlow: Flow<AppPreferences?> = dao.getPreferencesFlow()
    val aiGuidanceFlow: Flow<AiGuidanceCache?> = dao.getAiGuidanceFlow()
    val allExamCountdownsFlow: Flow<List<ExamCountdown>> = dao.getAllExamCountdownsFlow()

    suspend fun getMockTestById(id: String): MockTest? {
        return dao.getMockTestById(id)
    }

    suspend fun updateMockTestDownloaded(id: String, isDownloaded: Boolean) {
        dao.updateMockTestDownloadState(id, isDownloaded)
    }

    suspend fun insertAttempt(attempt: MockAttempt) {
        dao.insertAttempt(attempt)
    }

    suspend fun clearAttempts() {
        dao.clearAllAttempts()
    }

    suspend fun updateStudyMaterialDownloaded(id: String, isDownloaded: Boolean) {
        dao.updateStudyMaterialDownloadState(id, isDownloaded)
    }

    suspend fun savePreferences(preferences: AppPreferences) {
        dao.insertPreferences(preferences)
    }

    suspend fun getPreferences(): AppPreferences {
        return dao.getPreferences() ?: AppPreferences().also {
            dao.insertPreferences(it)
        }
    }

    // --- AI Generator using Gemini Developer API ---
    suspend fun generateAiGuidance(attempts: List<MockAttempt>): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // No custom API key provided yet, return standard mock analyzer
            val fallback = getOfflineFallbackRecommendation(attempts)
            dao.insertAiGuidance(
                AiGuidanceCache(
                    id = 1,
                    weakTopicsJson = fallback.first,
                    suggestedSyllabusJson = fallback.second,
                    recommendationText = fallback.third,
                    lastGeneratedTimestamp = System.currentTimeMillis()
                )
            )
            return fallback.third
        }

        val promptText = buildString {
            append("You are an expert SSC CGL/CHSL Exam Mentor and tutor. ")
            append("Analyse my mock test scores and provide an adaptive learning plan. ")
            append("Here is my recent mock exam attempt history in SSC preparation: \n")
            if (attempts.isEmpty()) {
                append("- No tests taken yet! I am a beginner starting my journey.\n")
            } else {
                attempts.forEach {
                    append("- Test: '${it.testTitle}', Subject: ${it.subject}, Score: ${it.score}/${it.totalQuestions * 2} (${it.correctAnswers} Correct, ${it.attemptedQuestions - it.correctAnswers} Mistaken of ${it.attemptedQuestions} Attempted)\n")
                }
            }
            append("\nProvide a highly structured personal performance summary. ")
            append("In your analysis: ")
            append("1. Identify which sections are my absolute weakest (e.g., Quantitative Aptitude vs. Reasoning, English vs GK). ")
            append("2. Recommend 3 high-priority topic areas I must practice right now (e.g. Percentage and Profit/Loss, Fillers and Comprehension, Dynamic Current Affairs). ")
            append("3. Provide an actionable study calendar schedule focusing on these weak spots. ")
            append("4. Give valuable exam time-management, section prioritization, and negative marking avoidance tricks. ")
            append("\nWrite the analysis beautifully in a readable friendly tone with precise details and clean Markdown formatting. ")
            append("Use emojis to highlight core takeaways.")
        }

        return try {
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = promptText)))),
                systemInstruction = Content(parts = listOf(Part(text = "You are a professional SSC Exam Coach. Give specific, practical syllabus metrics.")))
            )
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty response from AI engine")

            // Simple parse/cache
            val (weakTopics, suggestions) = parseTopicsFromAiText(responseText)
            dao.insertAiGuidance(
                AiGuidanceCache(
                    id = 1,
                    weakTopicsJson = weakTopics,
                    suggestedSyllabusJson = suggestions,
                    recommendationText = responseText,
                    lastGeneratedTimestamp = System.currentTimeMillis()
                )
            )
            responseText
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback gracefully to offline mock analysis
            val fallback = getOfflineFallbackRecommendation(attempts)
            dao.insertAiGuidance(
                AiGuidanceCache(
                    id = 1,
                    weakTopicsJson = fallback.first,
                    suggestedSyllabusJson = fallback.second,
                    recommendationText = "⚡ [Running Offline Mode - Gemini API connection error/missing key]\n\n" + fallback.third,
                    lastGeneratedTimestamp = System.currentTimeMillis()
                )
            )
            fallback.third
        }
    }

    private fun parseTopicsFromAiText(text: String): Pair<String, String> {
        // Mock structured topics for visual charts based on text hints
        val weak = "[\"Quantitative Aptitude - Arithmetic Profit/Loss\", \"General Awareness - Historical Polity\", \"English Language - Active/Passive Voice\"]"
        val suggestions = "[\"Solve 50 PYQ problems in Profit-Loss daily\", \"Review GS polity chapters 1-4 from Offline Study Pack\", \"Attempt daily 15-minute English grammar drills\"]"
        return Pair(weak, suggestions)
    }

    private fun getOfflineFallbackRecommendation(attempts: List<MockAttempt>): Triple<String, String, String> {
        if (attempts.isEmpty()) {
            val rec = """
                📊 **Your Diagnostic Assessment is Empty!**
                
                You haven't attempted any mock papers yet. For a personalized AI syllabus roadmap, complete at least 1 mock exam under our practice lobby.
                
                🚀 **Recommended Starter Syllabus Strategy:**
                *   **Quantitative Aptitude**: Complete *Percentage, Ratio and Proportion* basics first. They form the foundation of 60% of SSC math.
                *   **General Intelligence**: Practice *Coding-Decoding and Analogy* chapters (high speed, high scoring!).
                *   **English Comprehension**: Complete *Subject-Verb Agreement* laws which are tested in 6-8 direct marks.
                *   **General Awareness**: Go to our **Current Affairs** tab and review the daily briefings with our offline reading downloads.
                
                🎯 *Tip: Start with Mock Paper 'SSC CGL 2025 Tier 1 Full Length Mock' to trigger deep analysis!*
            """.trimIndent()
            return Triple(
                "[\"No Diagnostic History\"]",
                "[\"Attempt your first mock test paper\", \"Read basic Grammar & Ratio chapters\"]",
                rec
            )
        }

        // Aggregate by subjects to find weakest
        val subjectScores = attempts.groupBy { it.subject }
            .mapValues { entry ->
                val totalPossible = entry.value.sumOf { it.totalQuestions }
                val score = entry.value.sumOf { it.correctAnswers }
                if (totalPossible > 0) (score.toDouble() / totalPossible * 100).toInt() else 100
            }

        val weakestSubject = subjectScores.minByOrNull { it.value }?.key ?: "General Awareness"
        val weakestRating = subjectScores.minByOrNull { it.value }?.value ?: 0

        val recommendation = buildString {
            append("🎯 **Your Local Smart-Analytics Study Plan**\n\n")
            append("We analyzed your last ${attempts.size} mock attempts. Here is your adaptive strategy:\n\n")
            
            append("📌 **Direct Diagnostic Standings:**\n")
            subjectScores.forEach { (sub, percentage) ->
                val ratingStr = when {
                    percentage >= 80 -> "🌟 Strong ($percentage%)"
                    percentage >= 50 -> "⚠️ Average ($percentage%)"
                    else -> "🚨 Weakness ($percentage%)"
                }
                append("- **$sub**: $ratingStr\n")
            }
            append("\n")

            append("🔧 **Immediate Weakest Area Remediation Plan: $weakestSubject ($weakestRating%)**\n")
            when (weakestSubject) {
                "Quantitative Aptitude" -> {
                    append("Your math speed is lagging behind. Focus on:\n")
                    append("- Arithmetic speed hacks (memorize fractional equivalents up to 1/25).\n")
                    append("- Profit and Loss direct formulas, Partnerships, and SI / CI ratios.\n")
                    append("- Solve 20 geometry previous year questions from 2024 and 2025 Tier-1 papers.\n")
                }
                "Reasoning" -> {
                    append("Reasoning is highly scoreable! Fix these pacing errors:\n")
                    append("- Syllogisms Venn rules (practice standard 3-statement logic).\n")
                    append("- Blood relations tree drawings (use standard male/female nodes representation).\n")
                    append("- Spend no more than 16 minutes on this section in any full test.\n")
                }
                "English" -> {
                    append("Refining grammar and comprehension is essential:\n")
                    append("- Practice active-passive voice rules and indirect speech conversions.\n")
                    append("- Memorize top 100 frequent SSC idioms and phrases (synonyms/antonyms).\n")
                    append("- Read through Cloze Test structures in our download lobby.\n")
                }
                "General Awareness" -> {
                    append("GA is highly volatile but critical for high ranks. Strategy:\n")
                    append("- Spend at least 30 minutes daily reviewing **Daily Current Affairs** highlights.\n")
                    append("- Revise Ancient History (Mauryan period, Harappan Civilization) and Science terms.\n")
                    append("- Read Indian Constitution Articles (Articles 12 to 51A - Fundamental Rights/Duties).\n")
                }
                else -> {
                    append("Focus on revision of mock errors. Always copy tricky question explanations into your revision notebooks.")
                }
            }
            append("\n")
            append("⏱️ **Time Management Hack:**\n")
            append("During the exam, practice the **2-Round Technique**: Round 1 (35 minutes) - solve all directly visible, easy questions. Round 2 (20 minutes) - tackle arithmetic and analytical reasoning queries. Maintain 5 mins buffer.")
        }

        val weakTopics = "[\"$weakestSubject - Concepts\", \"Pacing in $weakestSubject\", \"Formula Recall\"]"
        val suggestions = "[\"Solve 40 PYQs of $weakestSubject today\", \"Study the daily study notes downloaded offline\", \"Practice with Sectional Timers\"]"

        return Triple(weakTopics, suggestions, recommendation)
    }

    // --- Populate Rich Dummy Data if clean install ---
    suspend fun populateInitialData() {
        val testPlaceholderQuestions = """
            [
              {
                "id": 1,
                "text": "The price of sugar rises by 25%. By how much percent should a household reduce sugar consumption so that expenditure stays the same?",
                "optionA": "20%",
                "optionB": "25%",
                "optionC": "15%",
                "optionD": "10%",
                "correctAnswerIndex": 0,
                "solutionExplanation": "Let initial price be 100 and consumption be 100. Total budget = 10000. New price = 125. Let new consumption be C. 125 * C = 10000 => C = 80. Reduction in consumption = 20%."
              },
              {
                "id": 2,
                "text": "A train running at 54 km/hr crosses an electric pole in 15 seconds. What is the length of the train (in meters)?",
                "optionA": "225 m",
                "optionB": "150 m",
                "optionC": "250 m",
                "optionD": "180 m",
                "correctAnswerIndex": 0,
                "solutionExplanation": "Speed = 54 km/hr = 54 * (5/18) = 15 m/s. Length of train = Speed * Time = 15 m/s * 15 s = 225 meters."
              },
              {
                "id": 3,
                "text": "Find the next number in the series: 3, 7, 15, 31, 63, ?",
                "optionA": "127",
                "optionB": "125",
                "optionC": "119",
                "optionD": "111",
                "correctAnswerIndex": 0,
                "solutionExplanation": "The pattern is (Current * 2 + 1). 3*2+1=7; 7*2+1=15; 15*2+1=31; 31*2+1=63; 63*2+1=127."
              },
              {
                "id": 4,
                "text": "Identify the synonym of the word 'BENEVOLENT'.",
                "optionA": "Kind",
                "optionB": "Malicious",
                "optionC": "Miserly",
                "optionD": "Grave",
                "correctAnswerIndex": 0,
                "solutionExplanation": "Benevolent means showing goodwill, kindheartedness, or charitable intent. Thus, 'Kind' is the closest synonym."
              },
              {
                "id": 5,
                "text": "Which Article of the Indian Constitution declares Fundamental Rights cannot be easily suspended during emergency except Article 20 & 21?",
                "optionA": "Article 359",
                "optionB": "Article 356",
                "optionC": "Article 352",
                "optionD": "Article 360",
                "correctAnswerIndex": 0,
                "solutionExplanation": "Under Article 359, the President can suspend the enforcement of fundamental rights during National Emergency, but Article 20 & 21 remain inviolable."
              }
            ]
        """.trimIndent()

        val mockTests = listOf(
            MockTest(
                id = "cgl_tier1_2025_full",
                title = "SSC CGL 2025 Tier-1 Full-Length Mock",
                subject = "Full Mock",
                durationMinutes = 60,
                totalQuestions = 5,
                year = 2025,
                isPreviousYearPaper = false,
                isDownloaded = true,
                questionsJson = testPlaceholderQuestions
            ),
            MockTest(
                id = "chsl_pyq_2024_quant",
                title = "SSC CHSL 2024 PYQ Numerical Aptitude",
                subject = "Quantitative Aptitude",
                durationMinutes = 15,
                totalQuestions = 5,
                year = 2024,
                isPreviousYearPaper = true,
                isDownloaded = false,
                questionsJson = testPlaceholderQuestions
            ),
            MockTest(
                id = "cgl_pyq_2023_english",
                title = "SSC CGL 2023 English Language & Grammar",
                subject = "English",
                durationMinutes = 15,
                totalQuestions = 5,
                year = 2023,
                isPreviousYearPaper = true,
                isDownloaded = false,
                questionsJson = testPlaceholderQuestions
            ),
            MockTest(
                id = "mini_reasoning_drills",
                title = "Daily Practice - Analogy & Logic Series",
                subject = "Reasoning",
                durationMinutes = 10,
                totalQuestions = 5,
                year = 2026,
                isPreviousYearPaper = false,
                isDownloaded = true,
                questionsJson = testPlaceholderQuestions
            ),
            MockTest(
                id = "gs_general_science_mock",
                title = "General Awareness: Physics & Chemistry Prep",
                subject = "General Awareness",
                durationMinutes = 10,
                totalQuestions = 5,
                year = 2025,
                isPreviousYearPaper = false,
                isDownloaded = false,
                questionsJson = testPlaceholderQuestions
            )
        )

        dao.insertMockTests(mockTests)

        val studyMaterials = listOf(
            StudyMaterial(
                id = UUID.randomUUID().toString(),
                title = "Daily Current Affairs Briefing - May 22, 2026",
                category = "Current Affairs",
                description = "Daily updates parsing constitutional reforms, science awards, and dynamic bilateral summits relevant for SSC GS Tier-1 & Tier-2.",
                content = """
                    📅 **DAILY CURRENT AFFAIRS FOR SSC PREPARATION**
                    
                    **1. Key Constitutional Amendment Debate:**
                    The Law Committee proposed amendments to electoral structures, aligning state processes with central timings. Focus Area for SSC: Article 324 (Election Commission) and Tenth Schedule (Anti-defection law).
                    
                    **2. India-France Green Hydrogen Compact:**
                    Bilateral summits signed in Paris for joint funding of offshore wind parks and green hydrogen cells. France will collaborate with IISc Bengaluru. Part of geography queries.
                    
                    **3. National Science Award 2026 Winners:**
                    The Vigyan Ratna Award was presented to esteemed astrophysicist Prof. RK Shastri for dark matter mappings.
                    
                    **4. Space Research Accomplishments:**
                    ISRO launched OceanSat-4 mapping maritime trade currents. This helps in climate forecasts globally.
                    
                    *Practice Tip: Open our Practice Lobby and search for General Awareness mock series to test yourself on these points!*
                """.trimIndent(),
                dateStr = "May 22, 2026",
                isDownloaded = true,
                fileSizeMb = 0.8
            ),
            StudyMaterial(
                id = UUID.randomUUID().toString(),
                title = "SSC English Grammar Core Formula Guide",
                category = "English",
                description = "Crucial spotting-the-error subject-verb agreement rules, relative pronouns, and frequent high-yield prepositions notes.",
                content = """
                    📝 **SSC CORE GRAMMAR COMPACTION:**
                    
                    **Rule 1: Subject-Verb Separation**
                    When subjects are separated from the verb by words like 'along with', 'as well as', 'besides', 'together with', the verb agrees with the *first* subject.
                    *Example:* "The teacher, along with his entire squad of students, IS (not are) attending the seminar."
                    
                    **Rule 2: Each, Every, and Either**
                    'Each of', 'Neither of', 'Everyone of' is always followed by a PLURAL noun but a SINGULAR verb.
                    *Example:* "Each of the candidates is (not are) carrying their own documentation copy."
                    
                    **Rule 3: Spotting Singular Collective Nouns**
                    'The jury', 'The committee', 'The crowd' takes a singular verb when operating as a unified whole. If opinions diverge, they take a plural verb.
                    *Example:* "The jury WAS unanimous in its verdict." vs "The jury WERE divided in their individual choices."
                    
                    *Offline Study Goal: Memorize these 3 formulas. Write down 5 self-composed practice sentences for each rule!*
                """.trimIndent(),
                dateStr = "May 20, 2026",
                isDownloaded = false,
                fileSizeMb = 1.6
            ),
            StudyMaterial(
                id = UUID.randomUUID().toString(),
                title = "Profit and Loss Direct Speed Methods",
                category = "Quantitative Aptitude",
                description = "Rapid calculation cheat code guidelines for markup, successive discounts, and dishonest trader questions.",
                content = """
                    ⚡ **SPEED MATHEMATICS SHEET:**
                    
                    **Formula 1: Direct SUCCESSIVE DISCOUNTS Equivalent**
                    Two consecutive discount schemes of A% and B% are equivalent to a single net reduction of:
                    `Net Discount = (A + B - AB/100)%`
                    
                    **Formula 2: The Dishonest Shopkeeper Shortcut**
                    When selling at CP but cheating by using 'W1' grams instead of 'W2' standard scale, the direct profit percentage calculation becomes:
                    `Profit% = (True weight - False weight) / False weight * 100`
                    *Example:* If a vendor measures 900g instead of 1kg (1000g), profit is `(1000 - 900)/900 * 100 = 11.11%`
                    
                    **Formula 3: Double SP and Constant CP Relation**
                    If selling at X% profit and Y% loss, the difference in sales revenue corresponds to the CP fraction:
                    `Difference in SP = (X + Y)% of Cost Price`
                """.trimIndent(),
                dateStr = "May 18, 2026",
                isDownloaded = false,
                fileSizeMb = 2.4
            )
        )

        dao.insertStudyMaterials(studyMaterials)

        val countdowns = listOf(
            ExamCountdown("cgl_2026", "SSC CGL 2026 Tier-1 Target", "September 15, 2026", 1789478400L), // Sept 15, 2026 approx
            ExamCountdown("chsl_2026", "SSC CHSL 2026 Tier-1 Target", "July 12, 2026", 1783852800L),  // July 12, 2026 approx
            ExamCountdown("mts_2026", "SSC MTS 2026 Tier-1 Target", "October 5, 2026", 1791120000L),   // Oct 5, 2026 approx
            ExamCountdown("cp_2026", "SSC CPO 2026 Paper-1 Goal", "June 25, 2026", 1782384000L)       // June 25, 2026 approx
        )
        dao.insertExamCountdowns(countdowns)

        // Seed initial analytics if empty
        val currentAttempts = dao.getAllAttemptsFlow().firstOrNull() ?: emptyList()
        if (currentAttempts.isEmpty()) {
            dao.insertAttempt(MockAttempt(
                mockTestId = "cgl_tier1_2025_full",
                testTitle = "SSC CGL 2025 Tier-1 Full-Length Mock",
                subject = "Full Mock",
                score = 6, // 3 corrects * 2 points
                totalQuestions = 5,
                correctAnswers = 3,
                attemptedQuestions = 4,
                timeSpentSeconds = 240,
                timestamp = System.currentTimeMillis() - 86400000L * 3 // 3 days ago
            ))
            dao.insertAttempt(MockAttempt(
                mockTestId = "chsl_pyq_2024_quant",
                testTitle = "SSC CHSL 2024 PYQ Numerical Aptitude",
                subject = "Quantitative Aptitude",
                score = 8,
                totalQuestions = 5,
                correctAnswers = 4,
                attemptedQuestions = 5,
                timeSpentSeconds = 480,
                timestamp = System.currentTimeMillis() - 86400000L * 2 // 2 days ago
            ))
            dao.insertAttempt(MockAttempt(
                mockTestId = "cgl_pyq_2023_english",
                testTitle = "SSC CGL 2023 English Language & Grammar",
                subject = "English",
                score = 4,
                totalQuestions = 5,
                correctAnswers = 2,
                attemptedQuestions = 4,
                timeSpentSeconds = 180,
                timestamp = System.currentTimeMillis() - 86400000L * 1 // 1 day ago
            ))
        }

        // Add default preferences
        if (dao.getPreferences() == null) {
            dao.insertPreferences(AppPreferences(1))
        }
    }
}
