package com.learning.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "quizzes",
    foreignKeys = [
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class QuizEntity(
    @PrimaryKey
    val id: String,
    val lessonId: String,
    val title: String,
    val description: String,
    val totalQuestions: Int,
    val passingScore: Int // percentage
)

@Serializable
@Entity(
    tableName = "quiz_questions",
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class QuizQuestionEntity(
    @PrimaryKey
    val id: String,
    val quizId: String,
    val question: String,
    val options: List<String>, // Multiple choice options
    val correctAnswer: String,
    val explanation: String,
    val orderIndex: Int
)
