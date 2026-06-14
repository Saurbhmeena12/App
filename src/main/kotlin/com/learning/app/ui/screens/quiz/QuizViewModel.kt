package com.learning.app.ui.screens.quiz

import androidx.lifecycle.ViewModel
import com.learning.app.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private var quizId: String? = null

    val quiz by lazy {
        flowOf(quizId).flatMapLatest { id ->
            id?.let { quizRepository.getQuizByLesson(it) } ?: emptyFlow()
        }
    }

    val questions by lazy {
        flowOf(quizId).flatMapLatest { id ->
            id?.let { quizRepository.getQuizQuestions(it) } ?: emptyFlow()
        }
    }

    fun setQuizId(id: String) {
        quizId = id
    }
}
