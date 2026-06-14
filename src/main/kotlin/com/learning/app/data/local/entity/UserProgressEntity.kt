package com.learning.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey
    val id: String = System.currentTimeMillis().toString(),
    val userId: String,
    val courseId: String,
    val lessonsCompleted: Int,
    val totalLessons: Int,
    val quizzesCompleted: Int,
    val quizzesScore: Int, // average score
    val progressPercentage: Int,
    val lastAccessedTime: Long,
    val certificateEarned: Boolean = false
)
