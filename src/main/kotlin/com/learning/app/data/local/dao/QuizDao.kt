package com.learning.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.learning.app.data.local.entity.QuizEntity
import com.learning.app.data.local.entity.QuizQuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes WHERE lessonId = :lessonId")
    fun getQuizByLesson(lessonId: String): Flow<QuizEntity?>

    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId ORDER BY orderIndex")
    fun getQuizQuestions(quizId: String): Flow<List<QuizQuestionEntity>>

    @Insert
    suspend fun insertQuiz(quiz: QuizEntity)

    @Insert
    suspend fun insertQuizQuestion(question: QuizQuestionEntity)

    @Insert
    suspend fun insertQuizQuestions(questions: List<QuizQuestionEntity>)

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    @Delete
    suspend fun deleteQuiz(quiz: QuizEntity)
}
