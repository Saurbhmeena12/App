package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SscPrepDao {

    // --- Mock Tests ---
    @Query("SELECT * FROM mock_tests")
    fun getAllMockTestsFlow(): Flow<List<MockTest>>

    @Query("SELECT * FROM mock_tests WHERE id = :id")
    suspend fun getMockTestById(id: String): MockTest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockTests(tests: List<MockTest>)

    @Query("UPDATE mock_tests SET isDownloaded = :isDownloaded WHERE id = :id")
    suspend fun updateMockTestDownloadState(id: String, isDownloaded: Boolean)

    // --- Mock Attempts ---
    @Query("SELECT * FROM mock_attempts ORDER BY timestamp DESC")
    fun getAllAttemptsFlow(): Flow<List<MockAttempt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: MockAttempt)

    @Query("DELETE FROM mock_attempts")
    suspend fun clearAllAttempts()

    // --- Study Materials & Current Affairs ---
    @Query("SELECT * FROM study_materials ORDER BY id DESC")
    fun getAllStudyMaterialsFlow(): Flow<List<StudyMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyMaterials(materials: List<StudyMaterial>)

    @Query("UPDATE study_materials SET isDownloaded = :isDownloaded WHERE id = :id")
    suspend fun updateStudyMaterialDownloadState(id: String, isDownloaded: Boolean)

    // --- Preferences ---
    @Query("SELECT * FROM ui_preferences WHERE id = 1")
    fun getPreferencesFlow(): Flow<AppPreferences?>

    @Query("SELECT * FROM ui_preferences WHERE id = 1")
    suspend fun getPreferences(): AppPreferences?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: AppPreferences)

    // --- AI Guidance Cache ---
    @Query("SELECT * FROM ai_guidance WHERE id = 1")
    fun getAiGuidanceFlow(): Flow<AiGuidanceCache?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiGuidance(guidance: AiGuidanceCache)

    // --- Exam Countdowns ---
    @Query("SELECT * FROM exam_countdown ORDER BY targetEpochSecond ASC")
    fun getAllExamCountdownsFlow(): Flow<List<ExamCountdown>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamCountdowns(countdowns: List<ExamCountdown>)
}
