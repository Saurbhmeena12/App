package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SscViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SscRepository
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // --- Database Streams ---
    val preferences: StateFlow<AppPreferences> = MutableStateFlow(AppPreferences())
    val mockTests: StateFlow<List<MockTest>>
    val attempts: StateFlow<List<MockAttempt>>
    val studyMaterials: StateFlow<List<StudyMaterial>>
    val aiGuidance: StateFlow<AiGuidanceCache?>
    val examCountdowns: StateFlow<List<ExamCountdown>>

    // --- Live UI States ---
    var isAnalyzing by mutableStateOf(false)
        private set

    var aiResponseText by mutableStateOf("")
        private set

    var welcomingSuggestedFeaturesDialogShown by mutableStateOf(true) // "Suggest more features after start" popup

    // --- Active Practice Exam States ---
    var activeTest by mutableStateOf<MockTest?>(null)
        private set
    var currentQuestionList by mutableStateOf<List<MockQuestion>>(emptyList())
        private set
    var currentQuestionIndex by mutableStateOf(0)
        private set
    val selectedAnswers = mutableStateMapOf<Int, Int>() // QuestionId -> Selected Option Index (0-3)
    var secondsLeft by mutableStateOf(0)
        private set
    var isTimerToggledOn by mutableStateOf(true)
        private set
    var isExamTimerRunning by mutableStateOf(false)
        private set
    var isTestCompleted by mutableStateOf(false)
        private set
    var latestScoreReport by mutableStateOf<MockAttempt?>(null)
        private set

    private var timerJob: Job? = null

    // --- Preferences Local States ---
    val localPrefsFlow: StateFlow<AppPreferences>

    init {
        val database = SscDatabase.getDatabase(application)
        val dao = database.sscPrepDao()
        repository = SscRepository(dao)

        // Seed on launch asynchronously
        viewModelScope.launch(Dispatchers.IO) {
            repository.populateInitialData()
        }

        mockTests = repository.allMockTestsFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        attempts = repository.allAttemptsFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        studyMaterials = repository.allStudyMaterialsFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        aiGuidance = repository.aiGuidanceFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        examCountdowns = repository.allExamCountdownsFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        localPrefsFlow = repository.preferencesFlow
            .map { it ?: AppPreferences() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferences())
    }

    // --- Subject List API Helper ---
    val subjectsList = listOf(
        "Quantitative Aptitude",
        "Reasoning",
        "English",
        "General Awareness"
    )

    // --- Preferences Modifications ---
    fun updateTheme(presetName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getPreferences()
            repository.savePreferences(current.copy(themePreset = presetName))
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getPreferences()
            repository.savePreferences(current.copy(isDarkMode = !current.isDarkMode))
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getPreferences()
            repository.savePreferences(current.copy(notificationsEnabled = enabled))
        }
    }

    fun updateStudyReminderTime(time24h: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getPreferences()
            repository.savePreferences(current.copy(studyReminderTime = time24h))
        }
    }

    fun toggleDashboardLayout() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getPreferences()
            repository.savePreferences(current.copy(dashboardGridStyle = !current.dashboardGridStyle))
        }
    }

    // --- Study Materials Downloads ---
    fun downloadMaterialOffline(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            delay(1500) // Simulate offline file buffer download
            repository.updateStudyMaterialDownloaded(id, true)
        }
    }

    fun removeDownloadedMaterial(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateStudyMaterialDownloaded(id, false)
        }
    }

    // --- Mock Tests Downloads ---
    fun downloadMockTestOffline(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            delay(1200) // Simulate downloading questions and solutions offline
            repository.updateMockTestDownloaded(id, true)
        }
    }

    fun removeDownloadedMockTest(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMockTestDownloaded(id, false)
        }
    }

    // --- Active Practice Test Session Operations ---
    fun startTestSession(test: MockTest) {
        activeTest = test
        currentQuestionIndex = 0
        selectedAnswers.clear()
        isTestCompleted = false
        latestScoreReport = null

        // Parse questions
        try {
            val type = Types.newParameterizedType(List::class.java, MockQuestion::class.java)
            val adapter = moshi.adapter<List<MockQuestion>>(type)
            currentQuestionList = adapter.fromJson(test.questionsJson) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            currentQuestionList = emptyList()
        }

        secondsLeft = test.durationMinutes * 60
        isTimerToggledOn = true
        startTimer()
    }

    fun toggleTimerState() {
        isTimerToggledOn = !isTimerToggledOn
        if (isTimerToggledOn) {
            startTimer()
        } else {
            stopTimer()
        }
    }

    private fun startTimer() {
        stopTimer()
        isExamTimerRunning = true
        timerJob = viewModelScope.launch {
            while (secondsLeft > 0 && isExamTimerRunning) {
                delay(1000)
                secondsLeft -= 1
            }
            if (secondsLeft <= 0) {
                submitCurrentTestSession()
            }
        }
    }

    private fun stopTimer() {
        isExamTimerRunning = false
        timerJob?.cancel()
        timerJob = null
    }

    fun selectOption(questionId: Int, optionIndex: Int) {
        selectedAnswers[questionId] = optionIndex
    }

    fun navigateQuestion(next: Boolean) {
        if (next) {
            if (currentQuestionIndex < currentQuestionList.size - 1) {
                currentQuestionIndex += 1
            }
        } else {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex -= 1
            }
        }
    }

    fun submitCurrentTestSession() {
        stopTimer()
        val test = activeTest ?: return

        var correctCount = 0
        var attemptedCount = 0

        currentQuestionList.forEach { q ->
            val sel = selectedAnswers[q.id]
            if (sel != null) {
                attemptedCount += 1
                if (sel == q.correctAnswerIndex) {
                    correctCount += 1
                }
            }
        }

        // SSC Scoring - 2 marks per question, penalty 0.5 per mistake
        val rightMarks = correctCount * 2
        val mistakeCount = attemptedCount - correctCount
        val negativeMarks = mistakeCount * 0.5
        val netScore = (rightMarks - negativeMarks).coerceAtLeast(0.0).toInt()

        val timeSpent = (test.durationMinutes * 60) - secondsLeft

        val attempt = MockAttempt(
            mockTestId = test.id,
            testTitle = test.title,
            subject = test.subject,
            score = netScore,
            totalQuestions = test.totalQuestions,
            correctAnswers = correctCount,
            attemptedQuestions = attemptedCount,
            timeSpentSeconds = timeSpent,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAttempt(attempt)
            latestScoreReport = attempt
            isTestCompleted = true
        }
    }

    fun quitTestSession() {
        stopTimer()
        activeTest = null
        currentQuestionList = emptyList()
        currentQuestionIndex = 0
        selectedAnswers.clear()
        isTestCompleted = false
        latestScoreReport = null
    }

    // --- AI Weakness Analysis Core (Gemini Call) ---
    fun runAiSyllabusAnalysis() {
        val list = attempts.value
        isAnalyzing = true
        viewModelScope.launch(Dispatchers.Default) {
            val report = repository.generateAiGuidance(list)
            viewModelScope.launch(Dispatchers.Main) {
                aiResponseText = report
                isAnalyzing = false
            }
        }
    }

    fun resetAllPracticeStats() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAttempts()
        }
    }
}
