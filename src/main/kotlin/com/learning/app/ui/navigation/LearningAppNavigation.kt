package com.learning.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.learning.app.ui.screens.home.HomeScreen
import com.learning.app.ui.screens.course.CourseDetailScreen
import com.learning.app.ui.screens.lesson.LessonScreen
import com.learning.app.ui.screens.quiz.QuizScreen
import com.learning.app.ui.screens.progress.ProgressScreen

@Composable
fun LearningAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Home.route
    ) {
        composable(NavRoutes.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(
            route = NavRoutes.CourseDetail.route,
            arguments = NavRoutes.CourseDetail.arguments
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            CourseDetailScreen(navController = navController, courseId = courseId)
        }

        composable(
            route = NavRoutes.Lesson.route,
            arguments = NavRoutes.Lesson.arguments
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: return@composable
            LessonScreen(navController = navController, lessonId = lessonId)
        }

        composable(
            route = NavRoutes.Quiz.route,
            arguments = NavRoutes.Quiz.arguments
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId") ?: return@composable
            QuizScreen(navController = navController, quizId = quizId)
        }

        composable(NavRoutes.Progress.route) {
            ProgressScreen(navController = navController)
        }
    }
}
