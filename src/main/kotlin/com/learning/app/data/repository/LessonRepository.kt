package com.learning.app.data.repository

import com.learning.app.data.local.dao.LessonDao
import com.learning.app.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LessonRepository @Inject constructor(
    private val lessonDao: LessonDao
) {
    fun getLessonsByCourse(courseId: String): Flow<List<LessonEntity>> =
        lessonDao.getLessonsByCourse(courseId)

    fun getLessonById(lessonId: String): Flow<LessonEntity?> = lessonDao.getLessonById(lessonId)

    suspend fun insertLesson(lesson: LessonEntity) = lessonDao.insertLesson(lesson)

    suspend fun insertLessons(lessons: List<LessonEntity>) = lessonDao.insertLessons(lessons)

    suspend fun updateLesson(lesson: LessonEntity) = lessonDao.updateLesson(lesson)

    suspend fun markLessonAsCompleted(lessonId: String) = lessonDao.markLessonAsCompleted(lessonId)
}
