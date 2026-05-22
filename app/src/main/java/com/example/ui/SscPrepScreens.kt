package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.MyApplicationTheme
import com.example.data.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SscPrepMainApp(viewModel: SscViewModel) {
    val prefs by viewModel.localPrefsFlow.collectAsState()
    val mockTests by viewModel.mockTests.collectAsState()
    val attempts by viewModel.attempts.collectAsState()
    val studyMaterials by viewModel.studyMaterials.collectAsState()
    val aiGuidance by viewModel.aiGuidance.collectAsState()
    val countdowns by viewModel.examCountdowns.collectAsState()

    var activeTab by remember { mutableStateOf("home") }

    MyApplicationTheme(
        themePreset = prefs.themePreset,
        isDarkMode = prefs.isDarkMode
    ) {
        val colors = MaterialTheme.colorScheme

        Scaffold(
            bottomBar = {
                if (viewModel.activeTest == null) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = activeTab == "home",
                            onClick = { activeTab = "home" },
                            icon = { Icon(Icons.Filled.Home, "Home Tab") },
                            label = { Text("Home", fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_home_tab")
                        )
                        NavigationBarItem(
                            selected = activeTab == "practice",
                            onClick = { activeTab = "practice" },
                            icon = { Icon(Icons.Filled.MenuBook, "Practice Tab") },
                            label = { Text("Practice", fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_practice_tab")
                        )
                        NavigationBarItem(
                            selected = activeTab == "study",
                            onClick = { activeTab = "study" },
                            icon = { Icon(Icons.Filled.Download, "Study Tab") },
                            label = { Text("Library", fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_study_tab")
                        )
                        NavigationBarItem(
                            selected = activeTab == "ai",
                            onClick = { activeTab = "ai" },
                            icon = { Icon(Icons.Filled.Psychology, "AI Coach Tab") },
                            label = { Text("AI Coach", fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_ai_tab")
                        )
                        NavigationBarItem(
                            selected = activeTab == "settings",
                            onClick = { activeTab = "settings" },
                            icon = { Icon(Icons.Filled.Settings, "Settings Tab") },
                            label = { Text("Settings", fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_settings_tab")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(colors.background)
            ) {
                // If a test session is active, override other views completely
                val activeQuiz = viewModel.activeTest
                if (activeQuiz != null) {
                    ActiveQuizScreen(viewModel = viewModel, test = activeQuiz)
                } else {
                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(250))
                        },
                        label = "tab_navigation"
                    ) { tabState ->
                        when (tabState) {
                            "home" -> HomeScreen(
                                viewModel = viewModel,
                                mockTests = mockTests,
                                attempts = attempts,
                                countdowns = countdowns,
                                prefs = prefs,
                                navigateToPractice = { activeTab = "practice" }
                            )
                            "practice" -> PracticeLobbyScreen(
                                viewModel = viewModel,
                                mockTests = mockTests
                            )
                            "study" -> StudyMaterialsScreen(
                                viewModel = viewModel,
                                materials = studyMaterials
                            )
                            "ai" -> AiCoachScreen(
                                viewModel = viewModel,
                                attempts = attempts,
                                aiGuidance = aiGuidance
                            )
                            "settings" -> SettingsScreen(
                                viewModel = viewModel,
                                prefs = prefs
                            )
                        }
                    }
                }

                // Welcoming Suggested Features Dialog (Popup on start)
                if (viewModel.welcomingSuggestedFeaturesDialogShown) {
                    WelcomingSuggestedFeaturesPopup(
                        onDismiss = { viewModel.welcomingSuggestedFeaturesDialogShown = false }
                    )
                }
            }
        }
    }
}

// Any app countdown target ticker calculates live hours / minutes
@Composable
fun LiveCountTicker(targetSecond: Long) {
    var diffMinutes by remember { mutableStateOf(0L) }
    var diffHours by remember { mutableStateOf(0L) }
    var diffDays by remember { mutableStateOf(0L) }

    LaunchedEffect(targetSecond) {
        while (true) {
            val nowSeconds = System.currentTimeMillis() / 1000
            val diff = targetSecond - nowSeconds
            if (diff > 0) {
                diffDays = diff / 86400
                diffHours = (diff % 86400) / 3600
                diffMinutes = (diff % 3600) / 60
            } else {
                diffDays = 0
                diffHours = 0
                diffMinutes = 0
            }
            delay(30000) // Update countdowns every 30 seconds
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CountdownBlock(diffDays.toString(), "Days")
        Text(":", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
        CountdownBlock(diffHours.toString().padStart(2, '0'), "Hrs")
        Text(":", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
        CountdownBlock(diffMinutes.toString().padStart(2, '0'), "Mins")
    }
}

@Composable
fun CountdownBlock(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            fontSize = 8.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
    }
}

// Utility modifier to implement bottom border highlights
fun Modifier.drawBottomBorder(thickness: androidx.compose.ui.unit.Dp, color: Color): Modifier = this.drawBehind {
    val strokeWidth = thickness.toPx()
    val y = size.height - strokeWidth / 2
    drawLine(
        color = color,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = strokeWidth
    )
}

// HomeScreen: Custom cards and quick performance indicators in Professional Polish theme
@Composable
fun HomeScreen(
    viewModel: SscViewModel,
    mockTests: List<MockTest>,
    attempts: List<MockAttempt>,
    countdowns: List<ExamCountdown>,
    prefs: AppPreferences,
    navigateToPractice: () -> Unit
) {
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header - Professional Profile bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AS",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Column {
                        Text(
                            text = "Welcome back,",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Abhishek Singh",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Diagnostics Active / Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (viewModel.isTimerToggledOn) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (viewModel.isTimerToggledOn) "FOCUSED" else "MANUAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Target Board Exam Countdown Widget
        item {
            if (countdowns.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("primary_countdown_widget"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = countdowns[0].examName.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LiveCountTicker(targetSecond = countdowns[0].targetEpochSecond)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "STATUS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "ACTIVE",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }

        // AI Adaptive Readiness Card
        item {
            val listAttempts = attempts
            val peak = if (listAttempts.isEmpty()) 0 else listAttempts.maxOf { it.score }
            val targetBoost = if (peak < 100) "+15 pts" else "+8 pts"
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Pulsing AI Indicator light
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "alpha"
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI READINESS INSIGHT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )
                        }
                        
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (listAttempts.isEmpty()) "65% READY" else "${(70 + (listAttempts.size * 2).coerceAtMost(25))}% READY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (listAttempts.isEmpty()) {
                            "Unlock high-accuracy indicators! Take your first Mock test under the Practice Lobby to receive targeted AI topic suggestions."
                        } else {
                            "Based on your last Mock attempts, focus on Ancient History and Trigonometry to boost potential score by $targetBoost."
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Section Timer Toggle UI Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                onClick = { viewModel.toggleTimerState() }
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SECTION TIMER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (viewModel.isTimerToggledOn) "AUTO-ENABLED" else "MANUAL SPEED",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Increments focus time limits on active mock tests automatically.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    // Custom switch mimicking HTML's slider pill
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(26.dp)
                            .background(
                                color = if (viewModel.isTimerToggledOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(13.dp)
                            )
                            .padding(2.dp),
                        contentAlignment = if (viewModel.isTimerToggledOn) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(
                                    color = if (viewModel.isTimerToggledOn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }

        // Dynamic Welcoming Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_welcome_card"),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(gradientColors))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "SSC Prep Mastery 🎯",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Build offline exam readiness with adaptive AI recommendations & sectional speed-planning.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FlashOn, "Ticker", tint = Color.Yellow, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Current Affairs Hot-update: Union Cabinet amends solar grid funds to bolster state renewable initiatives.",
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Quick Stats layout styled with Bottom Accent Boarders
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val mockCount = attempts.size
                val peakScore = attempts.maxOfOrNull { it.score } ?: 0

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .drawBottomBorder(4.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Filled.AssignmentTurnedIn, "Attempts", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Mock Tests", fontSize = 11.sp, color = Color.Gray)
                        Text(if (mockCount == 0) "12 New Available" else "$mockCount Completed", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .drawBottomBorder(4.dp, MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Filled.MilitaryTech, "Peak Score", tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Offline Prep", fontSize = 11.sp, color = Color.Gray)
                        Text("4.2GB Cached", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Analytical Performance Trend curves (drawn on Canvas)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SCORE TREND",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Last 5 Mocks",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (attempts.isEmpty()) {
                        Text(
                            text = "No practice history yet. Take tests to plot performance metrics.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        )
                    } else {
                        // Drawing static grid lines and custom curve values
                        val lineAccentColor = MaterialTheme.colorScheme.primary
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            val count = attempts.size.coerceAtLeast(2)
                            val stepX = size.width / (count - 1)
                            val maxScore = attempts.maxOf { it.score }.coerceAtLeast(10)
                            
                            // Draw guidelines
                            for (i in 1..3) {
                                val yLine = size.height * (i / 4.0f)
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.15f),
                                    start = Offset(0f, yLine),
                                    end = Offset(size.width, yLine),
                                    strokeWidth = 2f
                                )
                            }

                            // Plot scores path line
                            attempts.reversed().take(5).forEachIndexed { index, attempt ->
                                val x = index * stepX
                                val fractionY = attempt.score.toFloat() / maxScore.toFloat()
                                val y = size.height - (fractionY * size.height * 0.8f) - (size.height * 0.1f)

                                drawCircle(
                                    color = lineAccentColor,
                                    radius = 8f,
                                    center = Offset(x, y)
                                )

                                if (index > 0) {
                                    val lastAttempt = attempts.reversed().take(5)[index - 1]
                                    val lx = (index - 1) * stepX
                                    val lFractionY = lastAttempt.score.toFloat() / maxScore.toFloat()
                                    val ly = size.height - (lFractionY * size.height * 0.8f) - (size.height * 0.1f)
                                    drawLine(
                                        color = lineAccentColor,
                                        start = Offset(lx, ly),
                                        end = Offset(x, y),
                                        strokeWidth = 5f
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "*Scored marks are calculated on: Positive 2.0 per answer, Penalty 0.5 per wrong entry.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Daily Updates Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "NEWS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Daily Current Affairs: 22 May Update is now live...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // List of upcoming countdown items
        item {
            Column {
                Text(
                    text = "📅 Upcoming SSC Board Timetable",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    countdowns.drop(1).forEach { countdown ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = countdown.examName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Target registration timeline: " + countdown.examDate,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                LiveCountTicker(targetSecond = countdown.targetEpochSecond)
                            }
                        }
                    }
                }
            }
        }

        // Suggestions block
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                onClick = navigateToPractice
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Info,
                        "Tips",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "💡 Practice Makes Perfect",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Solve previous year sectional questions dynamically under our practice lobby. Download notes for offline reads during flight modes.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// PracticeLobbyScreen: Filtering test papers, downloads and test active launching
@Composable
fun PracticeLobbyScreen(
    viewModel: SscViewModel,
    mockTests: List<MockTest>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Quantitative Aptitude", "Reasoning", "English", "General Awareness", "PYQ Papers")

    val filteredTests = mockTests.filter { test ->
        val matchesSearch = test.title.contains(searchQuery, ignoreCase = true) ||
                test.subject.contains(searchQuery, ignoreCase = true)
        val matchesCategory = when (selectedCategory) {
            "All" -> true
            "PYQ Papers" -> test.isPreviousYearPaper
            else -> test.subject == selectedCategory
        }
        matchesSearch && matchesCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "⚡ Practice & Practice Papers",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Solve CGL, CHSL previous query compilations with deep explanatory analysis.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search mocks or year papers...") },
            leadingIcon = { Icon(Icons.Filled.Search, "Search Icon") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("practice_search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category scrollable tab row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredTests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Drafts,
                        "Empty List",
                        tint = Color.LightGray,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No tests found matching your terms.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTests) { test ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_item_" + test.id),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Badge(
                                            containerColor = if (test.isPreviousYearPaper) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
                                        ) {
                                            Text(
                                                text = if (test.isPreviousYearPaper) "PYQ ${test.year}" else "MOCK TEST",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                        if (test.isDownloaded) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .background(Color(0xFF059669).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Icon(Icons.Filled.CloudDone, "Offline", tint = Color(0xFF059669), modifier = Modifier.size(10.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("OFFLINE READY", fontSize = 9.sp, color = Color(0xFF059669), fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = test.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Subject: ${test.subject} • ${test.totalQuestions} Questions • ${test.durationMinutes} minutes",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                
                                // Download Icon
                                IconButton(
                                    onClick = {
                                        if (test.isDownloaded) {
                                            viewModel.removeDownloadedMockTest(test.id)
                                        } else {
                                            viewModel.downloadMockTestOffline(test.id)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (test.isDownloaded) Icons.Filled.DeleteOutline else Icons.Filled.CloudDownload,
                                        contentDescription = "Download state switch",
                                        tint = if (test.isDownloaded) Color.Red.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.startTestSession(test) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, "Begin")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Begin Quiz")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ActiveQuizScreen: Multi sectional questions, timer with pause switch, submit operations
@Composable
fun ActiveQuizScreen(viewModel: SscViewModel, test: MockTest) {
    val currentQuestion = viewModel.currentQuestionList.getOrNull(viewModel.currentQuestionIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Exit block
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.quitTestSession() }) {
                Icon(Icons.Filled.Close, "Exit Quiz")
            }
            Text(
                text = "Live Exam Lobby",
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            // Empty placeholder for balance
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Timer Panel Custom Header with toggler
        if (!viewModel.isTestCompleted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (viewModel.isExamTimerRunning) Icons.Filled.Timer else Icons.Filled.TimerOff,
                            contentDescription = "Timer status icon",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val clockMinutes = viewModel.secondsLeft / 60
                        val clockSeconds = viewModel.secondsLeft % 60
                        Text(
                            text = "Timer: ${clockMinutes.toString().padStart(2, '0')}:${clockSeconds.toString().padStart(2, '0')}",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Toggler button
                    OutlinedButton(
                        onClick = { viewModel.toggleTimerState() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(if (viewModel.isExamTimerRunning) "Pause Ticker" else "Resume Ticker", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isTestCompleted) {
            // Completed scorecard report and solutions explanations
            QuizReportPanel(viewModel = viewModel, test = test)
        } else if (currentQuestion == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Error compiling questions.", color = Color.Red)
            }
        } else {
            // Live Quiz view
            Text(
                text = "Question ${viewModel.currentQuestionIndex + 1} of ${viewModel.currentQuestionList.size}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Question Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Text(
                    text = currentQuestion.text,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Options list
            val options = listOf(
                Pair(0, currentQuestion.optionA),
                Pair(1, currentQuestion.optionB),
                Pair(2, currentQuestion.optionC),
                Pair(3, currentQuestion.optionD)
            )

            options.forEach { (idx, optionText) ->
                val isSelected = viewModel.selectedAnswers[currentQuestion.id] == idx
                val optionBorderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                val optionBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { viewModel.selectOption(currentQuestion.id, idx) }
                        .testTag("option_card_${currentQuestion.id}_$idx"),
                    border = BorderStroke(2.dp, optionBorderColor),
                    colors = CardDefaults.cardColors(containerColor = optionBg)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.selectOption(currentQuestion.id, idx) }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = optionText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation and Submission triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { viewModel.navigateQuestion(next = false) },
                    enabled = viewModel.currentQuestionIndex > 0,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Previous")
                }

                if (viewModel.currentQuestionIndex == viewModel.currentQuestionList.size - 1) {
                    Button(
                        onClick = { viewModel.submitCurrentTestSession() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("submit_exam_quiz_btn")
                    ) {
                        Icon(Icons.Filled.Check, "Finish")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Submit Exam")
                    }
                } else {
                    Button(
                        onClick = { viewModel.navigateQuestion(next = true) },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text("Next Question")
                    }
                }
            }
        }
    }
}

// Completed report card summary with calculations
@Composable
fun QuizReportPanel(viewModel: SscViewModel, test: MockTest) {
    val report = viewModel.latestScoreReport ?: return

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Stars,
            "Crown Score",
            tint = Color(0xFFD97706),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Mock Quiz Completed! 🎉",
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Results stats table
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ResultReportRow("Attempted Questions", "${report.attemptedQuestions} of ${report.totalQuestions}")
                ResultReportRow("Correct Submissions", "${report.correctAnswers} Correct", color = Color(0xFF059669))
                ResultReportRow("Incorrect Submissions", "${report.attemptedQuestions - report.correctAnswers} Wrong", color = Color.Red)
                Divider(modifier = Modifier.padding(vertical = 10.dp))
                ResultReportRow("Calculated Net Score", "${report.score} Points", isBold = true, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "💡 Answers & Explanation Keys",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Detailed Answers List Explaining Solutions
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            viewModel.currentQuestionList.forEach { q ->
                val chosenIndex = viewModel.selectedAnswers[q.id]
                val isCorrect = chosenIndex == q.correctAnswerIndex

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrect) Color(0xFFE6F4EA).copy(alpha = 0.4f) else Color(0xFFFCE8E6).copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, if (isCorrect) Color(0xFF059669).copy(alpha = 0.3f) else Color.Red.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Question: ${q.text}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your submission: " + when (chosenIndex) {
                                0 -> q.optionA
                                1 -> q.optionB
                                2 -> q.optionC
                                3 -> q.optionD
                                else -> "Unattempted"
                            },
                            color = if (isCorrect) Color(0xFF059669) else Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!isCorrect) {
                            Text(
                                text = "Correct answer: " + when (q.correctAnswerIndex) {
                                    0 -> q.optionA
                                    1 -> q.optionB
                                    2 -> q.optionC
                                    3 -> q.optionD
                                    else -> ""
                                },
                                color = Color(0xFF059669),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Solution Explanation:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Gray)
                                Text(q.solutionExplanation, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.quitTestSession() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Practice Lobby")
        }
    }
}

@Composable
fun ResultReportRow(label: String, valStr: String, isBold: Boolean = false, color: Color = Color.Unspecified) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(
            text = valStr,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.Black else FontWeight.Bold,
            color = color
        )
    }
}

// StudyMaterialsScreen: Daily reports and grammar notebooks with offline downloads
@Composable
fun StudyMaterialsScreen(
    viewModel: SscViewModel,
    materials: List<StudyMaterial>
) {
    var activeCategory by remember { mutableStateOf("All") }
    var expandedMaterialId by remember { mutableStateOf<String?>(null) }

    val categories = listOf("All", "Current Affairs", "English", "Quantitative Aptitude")

    val filtered = materials.filter {
        activeCategory == "All" || it.category == activeCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "📚 Study Materials & Daily Bulletins",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Read dynamic syllabus briefs and save files for complete offline access.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Categories Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSel = activeCategory == cat
                FilterChip(
                    selected = isSel,
                    onClick = { activeCategory = cat },
                    label = { Text(cat) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No resources available in this category.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { mat ->
                    val isExpanded = expandedMaterialId == mat.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("material_item_" + mat.id),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                            Text(
                                                text = mat.category.uppercase(),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                        if (mat.isDownloaded) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .background(Color(0xFF059669).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Icon(Icons.Filled.Check, "Saved", tint = Color(0xFF059669), modifier = Modifier.size(10.dp))
                                                Text("SAVED OFFLINE", fontSize = 9.sp, color = Color(0xFF059669), fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = mat.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = mat.dateStr,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }

                                // Download Toggle Switch
                                IconButton(
                                    onClick = {
                                        if (mat.isDownloaded) {
                                            viewModel.removeDownloadedMaterial(mat.id)
                                        } else {
                                            viewModel.downloadMaterialOffline(mat.id)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (mat.isDownloaded) Icons.Filled.DeleteOutline else Icons.Filled.CloudDownload,
                                        contentDescription = "Offline capability download switch",
                                        tint = if (mat.isDownloaded) Color.Red.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = mat.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { expandedMaterialId = if (isExpanded) null else mat.id },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (isExpanded) "Collapse Document" else "Open and Study Document")
                            }

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = mat.content,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// AiCoachScreen: Analyzer triggering Gemini API, parsing weakness trends and layout plans
@Composable
fun AiCoachScreen(
    viewModel: SscViewModel,
    attempts: List<MockAttempt>,
    aiGuidance: AiGuidanceCache?
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "🧠 Gemini AI Cognitive Mentor",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Analyse your score history to produce high-priority SSC topics plans.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Trigger analysis card
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Generate Course Corrections Plan 🚀",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Calls the server-side Gemini 3.5 Flash engine to scan your attempts ratios and produce customized arithmetic & logic study plans.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (viewModel.isAnalyzing) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Constructing adaptive schedule...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    Button(
                        onClick = { viewModel.runAiSyllabusAnalysis() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_generate_mentor_plan_btn")
                    ) {
                        Icon(Icons.Filled.AutoAwesome, "Sparkle")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analyze Weakness History")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recommendations output
        if (aiGuidance == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Psychology, "Ai wait", tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No diagnostic generated yet. Press the scan button above to generate standard schedules or Gemini plans.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Text(
                text = "📌 Adaptive Recommendations Table",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = aiGuidance.recommendationText,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (aiGuidance.lastGeneratedTimestamp > 0L) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Last calculated: " + SimpleDateFormat("hh:mm a, MMM dd, yyyy", Locale.getDefault()).format(Date(aiGuidance.lastGeneratedTimestamp)),
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}

// SettingsScreen: Study widgets structure customization mode, notification parameters
@Composable
fun SettingsScreen(viewModel: SscViewModel, prefs: AppPreferences) {
    var notificationTimeInput by remember { mutableStateOf(prefs.studyReminderTime) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "⚙️ Customization & Settings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Calibrate study alerts, dashboard architectures, and premium aesthetic themes.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Theme Selection Preset Chips
        Text(
            text = "🎨 Study Theme Presets",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        val themePresets = listOf(
            Pair("PROFESSIONAL_POLISH", "Professional Polish (Theme Default) ✨"),
            Pair("CLASSIC_BLUE", "SSC Classic Blue 🔵"),
            Pair("COSMIC_MIDNIGHT", "Cosmic Midnight 🪐"),
            Pair("WARM_SEPIA", "Warm Library Sepia 🪵"),
            Pair("FOREST_ZEN", "Forest Zen Sage 🌲")
        )

        themePresets.forEach { (presetVal, descriptionText) ->
            val isCurrent = prefs.themePreset == presetVal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.updateTheme(presetVal) }
                    .testTag("theme_preset_" + presetVal),
                border = BorderStroke(2.dp, if (isCurrent) MaterialTheme.colorScheme.primary else Color.Transparent),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(descriptionText, fontSize = 14.sp)
                    if (isCurrent) {
                        Icon(Icons.Filled.LibraryAddCheck, "Selected Theme", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Custom Switches
        Text(
            text = "⚙️ Study Environment Preferences",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Dark Mode Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Night Study Dark Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Eye-friendly dark interface for deep evening readings.", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = prefs.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("dark_mode_toggler_setting")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                // Custom notifications Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Daily Reminders Alerts", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Trigger localized push notifications or alarms rules.", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = prefs.notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                // Reminders Alarm Time Selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Daily Study Alarm Time", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Input your preferred daily study target alarm time (24-hour format):", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = notificationTimeInput,
                            onValueChange = { notificationTimeInput = it },
                            modifier = Modifier
                                .width(120.dp)
                                .testTag("study_time_alert_input_box"),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = { viewModel.updateStudyReminderTime(notificationTimeInput) }
                        ) {
                            Text("Set Time")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reset statistics button
        OutlinedButton(
            onClick = { viewModel.resetAllPracticeStats() },
            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red.copy(alpha = 0.8f))
        ) {
            Icon(Icons.Filled.DeleteSweep, "Clear database data")
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reset Mock Test Performance Statistics")
        }
    }
}

// Welcoming popup displaying recommended/additional features on launch
@Composable
fun WelcomingSuggestedFeaturesPopup(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Lightbulb,
                    "Suggested Tips",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Welcome to SSC Prep Mastery! 🌟",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Here are a few suggested features we configured for a seamless CGL study setup:",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Suggestion tips points
                SyllabusPointRow("🔄 Native Database Synchronization", "Allows client-to-desktop background syncing seamlessly as you take tests.")
                SyllabusPointRow("⏱️ Toggled Section Timers", "Test yourself on individual sections and freely pause/resume your metrics clock.")
                SyllabusPointRow("🧠 Advanced Gemini Flash Analytics", "Calls our server-side model helper to compute your precise weaknesses instantly.")
                SyllabusPointRow("🌲 Mindful Stress-Anti Themes", "Custom theme presets (Sepia, Zen green, Blue) calibrated to lower exam anxiety.")

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enter Study Lobby")
                }
            }
        }
    }
}

@Composable
fun SyllabusPointRow(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            "Syllabus Point",
            tint = Color(0xFF0284C7),
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text(desc, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
