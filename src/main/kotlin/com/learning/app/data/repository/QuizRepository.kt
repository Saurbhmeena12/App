package com.learning.app.data.repository

import com.learning.app.data.local.dao.QuizDao
import com.learning.app.data.local.entity.QuizEntity
import com.learning.app.data.local.entity.QuizQuestionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class QuizRepository @Inject constructor(
    private val quizDao: QuizDao
) {
    fun getQuizByLesson(lessonId: String): Flow<QuizEntity?> = quizDao.getQuizByLesson(lessonId)

    fun getQuizQuestions(quizId: String): Flow<List<QuizQuestionEntity>> =
        quizDao.getQuizQuestions(quizId)

    suspend fun insertQuiz(quiz: QuizEntity) = quizDao.insertQuiz(quiz)

    suspend fun insertQuizQuestions(questions: List<QuizQuestionEntity>) =
        quizDao.insertQuizQuestions(questions)
}
