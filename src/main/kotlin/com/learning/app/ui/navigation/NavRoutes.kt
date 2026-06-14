package com.learning.app.ui.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

seal class NavRoutes(val route: String, val arguments: List<NamedNavArgument> = emptyList()) {
    object Home : NavRoutes("home")
    
    object CourseDetail : NavRoutes(
        route = "course_detail/{courseId}",
        arguments = listOf(
            navArgument("courseId") { type = NavType.StringType }
        )
    ) {
        fun createRoute(courseId: String) = "course_detail/$courseId"
    }
    
    object Lesson : NavRoutes(
        route = "lesson/{lessonId}",
        arguments = listOf(
            navArgument("lessonId") { type = NavType.StringType }
        )
    ) {
        fun createRoute(lessonId: String) = "lesson/$lessonId"
    }
    
    object Quiz : NavRoutes(
        route = "quiz/{quizId}",
        arguments = listOf(
            navArgument("quizId") { type = NavType.StringType }
        )
    ) {
        fun createRoute(quizId: String) = "quiz/$quizId"
    }
    
    object Progress : NavRoutes("progress")
}
