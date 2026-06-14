package com.learning.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.learning.app.data.local.dao.CourseDao
import com.learning.app.data.local.dao.LessonDao
import com.learning.app.data.local.dao.QuizDao
import com.learning.app.data.local.dao.UserProgressDao
import com.learning.app.data.local.entity.CourseEntity
import com.learning.app.data.local.entity.LessonEntity
import com.learning.app.data.local.entity.QuizEntity
import com.learning.app.data.local.entity.QuizQuestionEntity
import com.learning.app.data.local.entity.UserProgressEntity

@Database(
    entities = [
        CourseEntity::class,
        LessonEntity::class,
        QuizEntity::class,
        QuizQuestionEntity::class,
        UserProgressEntity::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun lessonDao(): LessonDao
    abstract fun quizDao(): QuizDao
    abstract fun userProgressDao(): UserProgressDao
}
