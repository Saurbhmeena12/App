package com.learning.app.data.repository

import com.learning.app.data.local.dao.CourseDao
import com.learning.app.data.local.entity.CourseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CourseRepository @Inject constructor(
    private val courseDao: CourseDao
) {
    fun getAllCourses(): Flow<List<CourseEntity>> = courseDao.getAllCourses()

    fun getCourseById(courseId: String): Flow<CourseEntity?> = courseDao.getCourseById(courseId)

    fun getCoursesByCategory(category: String): Flow<List<CourseEntity>> =
        courseDao.getCoursesByCategory(category)

    fun getCoursesByLevel(level: String): Flow<List<CourseEntity>> =
        courseDao.getCoursesByLevel(level)

    suspend fun insertCourse(course: CourseEntity) = courseDao.insertCourse(course)

    suspend fun insertCourses(courses: List<CourseEntity>) = courseDao.insertCourses(courses)

    suspend fun updateCourse(course: CourseEntity) = courseDao.updateCourse(course)

    suspend fun initializeSampleData() {
        val sampleCourses = listOf(
            CourseEntity(
                id = "1",
                title = "Kotlin Basics",
                description = "Learn the fundamentals of Kotlin programming",
                imageUrl = "https://via.placeholder.com/300x200?text=Kotlin+Basics",
                category = "Programming",
                level = "Beginner",
                totalLessons = 10,
                duration = "2 weeks",
                instructor = "John Doe"
            ),
            CourseEntity(
                id = "2",
                title = "Android UI Design",
                description = "Master Android UI design principles and Jetpack Compose",
                imageUrl = "https://via.placeholder.com/300x200?text=Android+UI",
                category = "UI/UX",
                level = "Intermediate",
                totalLessons = 15,
                duration = "3 weeks",
                instructor = "Jane Smith"
            ),
            CourseEntity(
                id = "3",
                title = "Advanced Coroutines",
                description = "Deep dive into Kotlin coroutines and async programming",
                imageUrl = "https://via.placeholder.com/300x200?text=Coroutines",
                category = "Programming",
                level = "Advanced",
                totalLessons = 12,
                duration = "4 weeks",
                instructor = "Mike Johnson"
            )
        )
        insertCourses(sampleCourses)
    }
}
