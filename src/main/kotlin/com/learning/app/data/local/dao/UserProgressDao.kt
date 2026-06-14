package com.learning.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.learning.app.data.local.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    fun getUserProgress(userId: String): Flow<List<UserProgressEntity>>

    @Query("SELECT * FROM user_progress WHERE userId = :userId AND courseId = :courseId")
    fun getCourseProgress(userId: String, courseId: String): Flow<UserProgressEntity?>

    @Insert
    suspend fun insertProgress(progress: UserProgressEntity)

    @Update
    suspend fun updateProgress(progress: UserProgressEntity)

    @Query("SELECT AVG(progressPercentage) FROM user_progress WHERE userId = :userId")
    fun getOverallProgress(userId: String): Flow<Double?>
}
