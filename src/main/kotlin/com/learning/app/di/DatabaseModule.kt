package com.learning.app.di

import android.content.Context
import androidx.room.Room
import com.learning.app.data.local.dao.CourseDao
import com.learning.app.data.local.dao.LessonDao
import com.learning.app.data.local.dao.QuizDao
import com.learning.app.data.local.dao.UserProgressDao
import com.learning.app.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "learning_app_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideCourseDao(database: AppDatabase): CourseDao = database.courseDao()

    @Provides
    @Singleton
    fun provideLessonDao(database: AppDatabase): LessonDao = database.lessonDao()

    @Provides
    @Singleton
    fun provideQuizDao(database: AppDatabase): QuizDao = database.quizDao()

    @Provides
    @Singleton
    fun provideUserProgressDao(database: AppDatabase): UserProgressDao = database.userProgressDao()
}
