package com.learning.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learning.app.data.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    val courses = courseRepository.getAllCourses()

    init {
        viewModelScope.launch {
            try {
                courseRepository.initializeSampleData()
            } catch (e: Exception) {
                // Handle error - data might already exist
            }
        }
    }
}
