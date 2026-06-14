package com.learning.app.ui.screens.course

import androidx.lifecycle.ViewModel
import com.learning.app.data.repository.CourseRepository
import com.learning.app.data.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val lessonRepository: LessonRepository
) : ViewModel() {

    private var courseId: String? = null
    
    val course by lazy {
        flowOf(courseId).flatMapLatest { id ->
            id?.let { courseRepository.getCourseById(it) } ?: emptyFlow()
        }
    }

    val lessons by lazy {
        flowOf(courseId).flatMapLatest { id ->
            id?.let { lessonRepository.getLessonsByCourse(it) } ?: emptyFlow()
        }
    }

    fun setCourseId(id: String) {
        courseId = id
    }
}
