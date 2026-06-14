package com.learning.app.data.repository

import com.learning.app.data.local.dao.UserProgressDao
import com.learning.app.data.local.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProgressRepository @Inject constructor(
    private val userProgressDao: UserProgressDao
) {
    fun getUserProgress(userId: String): Flow<List<UserProgressEntity>> =
        userProgressDao.getUserProgress(userId)

    fun getCourseProgress(userId: String, courseId: String): Flow<UserProgressEntity?> =
        userProgressDao.getCourseProgress(userId, courseId)

    fun getOverallProgress(userId: String): Flow<Double?> =
        userProgressDao.getOverallProgress(userId)

    suspend fun updateProgress(progress: UserProgressEntity) =
        userProgressDao.updateProgress(progress)

    suspend fun insertProgress(progress: UserProgressEntity) =
        userProgressDao.insertProgress(progress)
}
