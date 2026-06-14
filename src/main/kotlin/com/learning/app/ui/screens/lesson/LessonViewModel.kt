package com.learning.app.ui.screens.lesson

import androidx.lifecycle.ViewModel
import com.learning.app.data.repository.LessonRepository
import com.learning.app.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class LessonViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {

    private var lessonId: String? = null

    val lesson by lazy {
        flowOf(lessonId).flatMapLatest { id ->
            id?.let { lessonRepository.getLessonById(it) } ?: emptyFlow()
        }
    }

    val quiz by lazy {
        flowOf(lessonId).flatMapLatest { id ->
            id?.let { quizRepository.getQuizByLesson(it) } ?: emptyFlow()
        }
    }

    fun setLessonId(id: String) {
        lessonId = id
    }
}
