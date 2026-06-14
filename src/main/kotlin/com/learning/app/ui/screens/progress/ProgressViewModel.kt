package com.learning.app.ui.screens.progress

import androidx.lifecycle.ViewModel
import com.learning.app.data.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val progressRepository: ProgressRepository
) : ViewModel() {

    // For now, using a hardcoded userId - in production, this should come from authentication
    private val userId = "user_001"

    val userProgress = progressRepository.getUserProgress(userId)

    val overallProgress = progressRepository.getOverallProgress(userId)
}
