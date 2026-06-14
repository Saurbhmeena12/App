package com.learning.app.data.repository

import com.learning.app.data.local.dao.CourseDao
import com.learning.app.data.local.dao.OfflineContentDao
import com.learning.app.data.remote.api.LearningApiService
import com.learning.app.data.remote.api.RetryPolicy
import com.learning.app.data.remote.api.NetworkException
import com.learning.app.data.local.entity.CourseEntity
import com.learning.app.data.local.entity.OfflineContentEntity
import com.learning.app.utils.network.NetworkManager
import com.learning.app.utils.network.NetworkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import java.io.IOException

class CourseRepositoryImproved @Inject constructor(
    private val courseDao: CourseDao,
    private val offlineContentDao: OfflineContentDao,
    private val apiService: LearningApiService,
    private val networkManager: NetworkManager,
    private val retryPolicy: RetryPolicy
) {

    fun getAllCourses(): Flow<List<CourseEntity>> = flow {
        // First emit from local cache
        val localCourses = courseDao.getAllCourses().firstOrNull() ?: emptyList()
        if (localCourses.isNotEmpty()) {
            emit(localCourses)
        }

        // Then try to fetch from remote if online
        if (networkManager.isNetworkAvailable()) {
            try {
                val remoteCourses = retryPolicy.executeWithRetry {
                    val response = apiService.getAllCourses()
                    if (response.isSuccessful && response.body() != null) {
                        response.body()!!.data.map { dto ->
                            CourseEntity(
                                id = dto.id,
                                title = dto.title,
                                description = dto.description,
                                imageUrl = dto.imageUrl,
                                category = dto.category,
                                level = dto.level,
                                totalLessons = dto.totalLessons,
                                duration = dto.duration,
                                instructor = dto.instructor
                            )
                        }
                    } else {
                        emptyList()
                    }
                }

                // Cache the remote courses
                courseDao.insertCourses(remoteCourses)
                emit(remoteCourses)
            } catch (e: Exception) {
                // If remote fails, emit cached data
                if (localCourses.isNotEmpty()) {
                    emit(localCourses)
                } else {
                    throw NetworkException.ConnectionError(e.message ?: "Failed to fetch courses")
                }
            }
        }
    }

    fun getCourseById(courseId: String): Flow<CourseEntity?> = flow {
        // Try local first
        val localCourse = courseDao.getCourseById(courseId).firstOrNull()
        if (localCourse != null) {
            emit(localCourse)
            return@flow
        }

        // Fetch from remote if online
        if (networkManager.isNetworkAvailable()) {
            try {
                val response = retryPolicy.executeWithRetry {
                    apiService.getCourse(courseId)
                }

                if (response.isSuccessful && response.body() != null) {
                    val dto = response.body()!!
                    val entity = CourseEntity(
                        id = dto.id,
                        title = dto.title,
                        description = dto.description,
                        imageUrl = dto.imageUrl,
                        category = dto.category,
                        level = dto.level,
                        totalLessons = dto.totalLessons,
                        duration = dto.duration,
                        instructor = dto.instructor
                    )
                    courseDao.insertCourse(entity)
                    emit(entity)
                }
            } catch (e: Exception) {
                emit(null)
            }
        }
    }

    suspend fun downloadCourseForOffline(courseId: String) {
        val course = getCourseById(courseId).firstOrNull() ?: return
        val courseJson = kotlinx.serialization.json.Json.encodeToString(
            CourseEntity.serializer(),
            course
        )
        offlineContentDao.insertContent(
            OfflineContentEntity(
                id = courseId,
                contentType = "course",
                contentData = courseJson,
                downloadedAt = System.currentTimeMillis(),
                lastAccessedAt = System.currentTimeMillis()
            )
        )
    }
}
