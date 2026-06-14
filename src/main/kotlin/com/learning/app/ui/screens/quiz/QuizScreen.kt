package com.learning.app.ui.screens.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.learning.app.data.local.entity.QuizEntity
import com.learning.app.data.local.entity.QuizQuestionEntity

@Composable
fun QuizScreen(
    navController: NavHostController,
    quizId: String,
    viewModel: QuizViewModel = hiltViewModel()
) {
    viewModel.setQuizId(quizId)

    val quiz by viewModel.quiz.collectAsState(initial = null)
    val questions by viewModel.questions.collectAsState(initial = emptyList())
    
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswers by remember { mutableStateOf(mutableMapOf<String, String>()) }
    var showResults by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Quiz") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        quiz?.let { quizData ->
            if (showResults) {
                // Show Results
                QuizResultsScreen(
                    quiz = quizData,
                    score = score,
                    onBackClick = { navController.popBackStack() }
                )
            } else if (questions.isNotEmpty()) {
                // Show Quiz Questions
                QuizQuestionsScreen(
                    questions = questions,
                    currentQuestionIndex = currentQuestionIndex,
                    selectedAnswers = selectedAnswers,
                    onAnswerSelected = { questionId, answer ->
                        selectedAnswers[questionId] = answer
                    },
                    onNext = {
                        if (currentQuestionIndex < questions.size - 1) {
                            currentQuestionIndex++
                        } else {
                            // Calculate score
                            score = calculateScore(questions, selectedAnswers)
                            showResults = true
                        }
                    },
                    onPrevious = {
                        if (currentQuestionIndex > 0) {
                            currentQuestionIndex--
                        }
                    },
                    totalQuestions = questions.size
                )
            }
        }
    }
}

@Composable
fun QuizQuestionsScreen(
    questions: List<QuizQuestionEntity>,
    currentQuestionIndex: Int,
    selectedAnswers: Map<String, String>,
    onAnswerSelected: (String, String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    totalQuestions: Int
) {
    val currentQuestion = questions[currentQuestionIndex]
    val selectedAnswer = selectedAnswers[currentQuestion.id] ?: ""

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress
        item {
            Text(
                text = "Question ${currentQuestionIndex + 1} of $totalQuestions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = (currentQuestionIndex + 1).toFloat() / totalQuestions,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        // Question
        item {
            Text(
                text = currentQuestion.question,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Options
        itemsIndexed(currentQuestion.options) { index, option ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (option == selectedAnswer),
                        onClick = { onAnswerSelected(currentQuestion.id, option) }
                    ),
                shape = RoundedCornerShape(8.dp),
                border = if (option == selectedAnswer) {
                    androidx.compose.foundation.BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.primary
                    )
                } else {
                    null
                },
                colors = CardDefaults.cardColors(
                    containerColor = if (option == selectedAnswer) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = option == selectedAnswer,
                        onClick = { onAnswerSelected(currentQuestion.id, option) },
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Navigation Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPrevious,
                    modifier = Modifier.weight(1f),
                    enabled = currentQuestionIndex > 0,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Previous")
                }

                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                    enabled = selectedAnswer.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (currentQuestionIndex == 0) "Next" else "Next")
                }
            }
        }
    }
}

@Composable
fun QuizResultsScreen(
    quiz: QuizEntity,
    score: Int,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quiz Completed!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Surface(
            shape = RoundedCornerShape(100),
            color = if (score >= quiz.passingScore) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
            modifier = Modifier
                .size(120.dp)
                .padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "$score%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = if (score >= quiz.passingScore) "Passed!" else "Try Again",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp),
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Passing score: ${quiz.passingScore}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        Button(
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Back to Lesson")
        }
    }
}

private fun calculateScore(questions: List<QuizQuestionEntity>, selectedAnswers: Map<String, String>): Int {
    val correctAnswers = questions.count { question ->
        selectedAnswers[question.id] == question.correctAnswer
    }
    return (correctAnswers * 100) / questions.size
}
