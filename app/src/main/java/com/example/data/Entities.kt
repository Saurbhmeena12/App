package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@Entity(tableName = "mock_tests")
data class MockTest(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String, // "Quantitative Aptitude", "Reasoning", "English", "General Awareness", "Full Mock"
    val durationMinutes: Int,
    val totalQuestions: Int,
    val year: Int,
    val isPreviousYearPaper: Boolean,
    val isDownloaded: Boolean = false,
    val questionsJson: String // Serialized Mock questions
)

@Entity(tableName = "mock_attempts")
data class MockAttempt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mockTestId: String,
    val testTitle: String,
    val subject: String,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val attemptedQuestions: Int,
    val timeSpentSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_materials")
data class StudyMaterial(
    @PrimaryKey val id: String,
    val title: String,
    val category: String, // "Current Affairs", "Quantitative Aptitude", "General Intelligence", "English", "General Awareness"
    val description: String,
    val content: String,
    val dateStr: String,
    val isDownloaded: Boolean = false,
    val fileSizeMb: Double = 1.2
)

@Entity(tableName = "ui_preferences")
data class AppPreferences(
    @PrimaryKey val id: Int = 1,
    val themePreset: String = "PROFESSIONAL_POLISH", // "PROFESSIONAL_POLISH", "CLASSIC_BLUE", "COSMIC_MIDNIGHT", "WARM_SEPIA", "FOREST_ZEN"
    val isDarkMode: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val studyReminderTime: String = "19:00", // 24hr format
    val autoRefreshEnabled: Boolean = true,
    val dashboardGridStyle: Boolean = true
)

@Entity(tableName = "ai_guidance")
data class AiGuidanceCache(
    @PrimaryKey val id: Int = 1,
    val weakTopicsJson: String = "",
    val suggestedSyllabusJson: String = "",
    val recommendationText: String = "",
    val lastGeneratedTimestamp: Long = 0L
)

@Entity(tableName = "exam_countdown")
data class ExamCountdown(
    @PrimaryKey val id: String,
    val examName: String, // e.g., "SSC CGL 2026 Tier 1"
    val examDate: String, // e.g., "September 15, 2026"
    val targetEpochSecond: Long
)

// Simple question model serialized into JSON
data class MockQuestion(
    val id: Int,
    val text: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswerIndex: Int, // 0 to 3
    val solutionExplanation: String
)

@Entity(tableName = "study_notes")
data class StudyNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String, // "Quantitative Aptitude", "Reasoning", "English", "General Awareness", "Self Notes"
    val keywords: String = "",
    val isSuggestedByAi: Boolean = false,
    val sourceUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectionName: String, // "Quantitative Aptitude", "Reasoning", "English", "General Awareness"
    val durationSeconds: Int,
    val mode: String = "EXAM_STUDY", // "EXAM_STUDY", "REVISION", "ACTIVE_PRACTICE"
    val timestamp: Long = System.currentTimeMillis()
)
