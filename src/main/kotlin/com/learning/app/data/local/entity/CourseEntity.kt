package com.learning.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val category: String,
    val level: String, // Beginner, Intermediate, Advanced
    val totalLessons: Int,
    val duration: String, // e.g., "4 weeks"
    val instructor: String
)
