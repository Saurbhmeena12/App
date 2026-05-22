package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
                if (viewModel.activeTest == null && !viewModel.isStudyTimerActive) {
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
                // If a test session or immersive study mode is active, override other views completely
                val activeQuiz = viewModel.activeTest
                val isStudyModeActive = viewModel.isStudyTimerActive
                if (activeQuiz != null) {
                    ActiveQuizScreen(viewModel = viewModel, test = activeQuiz)
                } else if (isStudyModeActive) {
                    ActiveStudyTimerScreen(viewModel = viewModel)
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
    var homeSegment by remember { mutableStateOf("desk") }

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

        // Segment switch tab
        item {
            TabRow(
                selectedTabIndex = if (homeSegment == "desk") 0 else 1,
                containerColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Tab(
                    selected = homeSegment == "desk",
                    onClick = { homeSegment = "desk" }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.Dashboard, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Syllabus Desk", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                Tab(
                    selected = homeSegment == "analytics",
                    onClick = { homeSegment = "analytics" }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.BarChart, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analytics & Reports", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        if (homeSegment == "desk") {
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
        } else {
            item {
                AnalyticsDashboardSegment(viewModel = viewModel)
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
    var librarySegment by remember { mutableStateOf("notes") }
    val notes by viewModel.studyNotes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "📚 Study Hub & Library",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Review lessons, take custom study notes, or run sectional target study sessions.",
            fontSize = 11.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(10.dp))

        TabRow(
            selectedTabIndex = when(librarySegment) {
                "notes" -> 0
                "guides" -> 1
                "timer" -> 2
                else -> 0
            },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth().height(42.dp)
        ) {
            Tab(
                selected = librarySegment == "notes",
                onClick = { librarySegment = "notes" }
            ) {
                Text("Self Notes", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Tab(
                selected = librarySegment == "guides",
                onClick = { librarySegment = "guides" }
            ) {
                Text("Study Guides", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Tab(
                selected = librarySegment == "timer",
                onClick = { librarySegment = "timer" }
            ) {
                Text("Sectional Timer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (librarySegment) {
            "notes" -> SelfStudyNotesSegment(viewModel = viewModel, notes = notes)
            "guides" -> CuratedGuidesSegment(viewModel = viewModel, materials = materials)
            "timer" -> StudyTimerConfigurationSegment(viewModel = viewModel)
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
    var coachSegment by remember { mutableStateOf("chat") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "🧠 Gemini AI Cognitive Mentor",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Analyse score logs or search key terms to create persistent notes.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(10.dp))

        TabRow(
            selectedTabIndex = if (coachSegment == "chat") 0 else 1,
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth().height(42.dp)
        ) {
            Tab(selected = coachSegment == "chat", onClick = { coachSegment = "chat" }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Chat, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Study Chat", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Tab(selected = coachSegment == "diagnostics", onClick = { coachSegment = "diagnostics" }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoAwesome, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Syllabus Plan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (coachSegment == "chat") {
            Box(modifier = Modifier.weight(1f)) {
                AiResearchChatbotScreen(viewModel = viewModel)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                                HorizontalDivider()
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

// ==========================================
// NEW FEATURE USER MODULE COMPOSABLES
// ==========================================

@Composable
fun ActiveStudyTimerScreen(viewModel: SscViewModel) {
    val totalSec = viewModel.studyTimerTotalDuration
    val leftSec = viewModel.studyTimerSecondsLeft
    val phaseRatio = if (totalSec > 0) leftSec.toFloat() / totalSec.toFloat() else 0f
    val minutes = leftSec / 60
    val seconds = leftSec % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "⚡ IMMERSIVE STUDY ZONE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = viewModel.studyTimerSectionName.uppercase(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = colors.onBackground
            )
            Text(
                text = "Keep absolute focus on your learning targets. Distractions are suppressed.",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                CircularProgressIndicator(
                    progress = { phaseRatio },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 14.dp,
                    color = colors.primary,
                    trackColor = colors.primary.copy(alpha = 0.15f)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onBackground,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "time remaining",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(42.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Lightbulb, "Concept", tint = Color.Yellow)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = when (viewModel.studyTimerSectionName) {
                            "Quantitative Aptitude" -> "Remember: Arithmetic answers are easier to back-solve using the options! Speed up by estimating limits."
                            "Reasoning" -> "Syllogism rule: Focus on Venn diagrams overlaps. Never assume real-world details unless logical rules dictate."
                            "English" -> "Grammar thumbrule: Direct-and-indirect conversions demand specific back-tense changes. Watch your plural auxiliary verbs."
                            "General Awareness" -> "Current Affairs rule: Sum up central welfare names with corresponding state launch months."
                            else -> "Keep joting key formula definitions onto your scrap sheet. Re-reading errors forms 90% of exam improvement!"
                        },
                        fontSize = 12.sp,
                        color = colors.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(42.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.toggleStudyTimerPause() },
                    modifier = Modifier.testTag("study_timer_pause_btn")
                ) {
                    Icon(
                        if (viewModel.isStudyTimerRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (viewModel.isStudyTimerRunning) "Pause" else "Resume")
                }

                Button(
                    onClick = { viewModel.completeSectionalStudySession() },
                    modifier = Modifier.testTag("study_timer_complete_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Icon(Icons.Filled.Check, null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Complete Goal")
                }

                IconButton(
                    onClick = { viewModel.cancelSectionalStudySession() },
                    modifier = Modifier
                        .background(colors.error.copy(alpha = 0.15f), CircleShape)
                        .testTag("study_timer_cancel_btn")
                ) {
                    Icon(Icons.Filled.Close, "Exit Study", tint = colors.error)
                }
            }
        }
    }
}

@Composable
fun StudyPacingBarChart(sessions: List<StudySession>) {
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

    val rawDays = (0..6).map { i ->
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, -i)
        c
    }.reversed()

    val dailyMinutes = rawDays.map { c ->
        val d = Calendar.getInstance()
        d.timeInMillis = c.timeInMillis
        d.set(Calendar.HOUR_OF_DAY, 0)
        d.set(Calendar.MINUTE, 0)
        d.set(Calendar.SECOND, 0)
        d.set(Calendar.MILLISECOND, 0)
        val dayStart = d.timeInMillis
        val dayEnd = dayStart + 86400000L

        val daySessions = sessions.filter { it.timestamp in dayStart until dayEnd }
        val sumSec = daySessions.sumOf { it.durationSeconds }
        Pair(dayFormat.format(c.time), sumSec / 60)
    }

    val maxMin = dailyMinutes.maxOf { it.second }.coerceAtLeast(1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 Focused Study Minutes (Last 7 Days)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyMinutes.forEach { (dayName, mins) ->
                    val barWeight = if (maxMin > 0) mins.toFloat() / maxMin.toFloat() else 0f
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${mins}m",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (mins > 0) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height((barWeight * 80).coerceAtLeast(3f).dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = dayName, fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsDashboardSegment(viewModel: SscViewModel) {
    val attempts by viewModel.attempts.collectAsState()
    val sessions by viewModel.studySessions.collectAsState()

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        StudyPacingBarChart(sessions = sessions)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 Weekly Performance Audit & Analysis",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                        Text("COMPARED BASELINE", fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                val thisWeekStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                }.timeInMillis

                val attemptsThisWeek = attempts.filter { it.timestamp >= thisWeekStart }
                val sessionsThisWeek = sessions.filter { it.timestamp >= thisWeekStart }
                val studyThisWeekMins = sessionsThisWeek.sumOf { it.durationSeconds } / 60

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Work Done This Week", fontSize = 10.sp, color = Color.Gray)
                        Text("$studyThisWeekMins Min Study", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${sessionsThisWeek.size} focused sessions log",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active Readiness", fontSize = 10.sp, color = Color.Gray)
                        Text(if (attemptsThisWeek.isEmpty()) "Need Mock Tests" else "${attemptsThisWeek.size} Tests Taken", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        val avgScore = if (attemptsThisWeek.isNotEmpty()) attemptsThisWeek.map { it.score }.average().toInt() else 0
                        Text("Avg Score: $avgScore points", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "🤖 AI Coach Weekly Remediation:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = when {
                        studyThisWeekMins == 0 -> "⚠️ You scheduled no focus timing blocks this week yet! Start with a 15-minute section Study block in the Library tab."
                        attemptsThisWeek.isEmpty() -> "📊 Good progress on sectional readings ($studyThisWeekMins mins). However, you must take a Mock Test so that the AI score trend metrics can update!"
                        else -> "⚡ Incredible work Abhishek Singh! Your daily study routines ($studyThisWeekMins mins) are syncing nicely with Mock Score records. General Awareness and Quantitative Aptitude pacing looks solid."
                    },
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋 Local Study & Practice Records Manager",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { viewModel.resetAllPracticeStats() }) {
                        Icon(Icons.Filled.DeleteSweep, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset All", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (attempts.isEmpty() && sessions.isEmpty()) {
                    Text(
                        "No logs stored. Take tests or run study timer sessions to produce data streams.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Text("Practice Attempts Log:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    attempts.take(4).forEach { att ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(att.testTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${att.subject} • Score: ${att.score} • Accuracy: ${if(att.attemptedQuestions>0)(att.correctAnswers*100/att.attemptedQuestions) else 0}%", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Focused Timers Log:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    sessions.take(4).forEach { ses ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ses.sectionName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Duration: ${ses.durationSeconds / 60}m ${ses.durationSeconds % 60}s • Type: ${ses.mode}", fontSize = 10.sp, color = Color.Gray)
                            }
                            IconButton(
                                onClick = { viewModel.deleteStudySession(ses.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Filled.Delete, "Delete", tint = Color.Gray, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelfStudyNotesSegment(viewModel: SscViewModel, notes: List<StudyNote>) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedNoteForDialog by remember { mutableStateOf<StudyNote?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📝 Personal Notes Shelf (${notes.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp).testTag("create_note_btn")
            ) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Note", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (notes.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.NoteAlt, null, tint = Color.LightGray, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your Study Notes shelf is empty.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add notes manually above, or seek help from the chatbot on the 'AI Coach' tab, which performs searches to construct ready-to-save revision notes!",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)
            ) {
                items(notes) { note ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedNoteForDialog = note },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                    Text(note.category.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (note.isSuggestedByAi) {
                                        Icon(Icons.Filled.AutoAwesome, "AI", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("AI GOOGLE SEARCH", fontSize = 8.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteStudyNote(note.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.Delete, "Delete", tint = Color.Gray, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(note.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(note.content, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddManualNoteDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, content, cat ->
                viewModel.addStudyNote(title = title, content = content, category = cat)
                showAddDialog = false
            }
        )
    }

    selectedNoteForDialog?.let { note ->
        Dialog(onDismissRequest = { selectedNoteForDialog = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                            Text(note.category.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                        IconButton(onClick = { selectedNoteForDialog = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Close, null)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(note.title, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(note.content, fontSize = 13.sp, lineHeight = 18.sp)
                    if (note.sourceUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Search reference credits: ${note.sourceUrl}", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun AddManualNoteDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Self Notes") }
    val categories = listOf("Self Notes", "Quantitative Aptitude", "Reasoning", "English", "General Awareness")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📝 Add Manual Study Note", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title (e.g., Trigonometry formulas)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text("Subject Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                TextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Write notes information...") },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    maxLines = 10
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = { if (title.isNotBlank()) onSave(title, content, category) },
                        enabled = title.isNotBlank() && content.isNotBlank()
                    ) { Text("Save Note") }
                }
            }
        }
    }
}

@Composable
fun StudyTimerConfigurationSegment(viewModel: SscViewModel) {
    var selectedSection by remember { mutableStateOf("Quantitative Aptitude") }
    var selectedMinutes by remember { mutableStateOf(15) }

    val sections = listOf("Quantitative Aptitude", "Reasoning", "English", "General Awareness", "Self Notes")
    val times = listOf(5, 10, 15, 20, 30, 45, 60)

    val sessions by viewModel.studySessions.collectAsState()
    val totalFocusMinutes = sessions.sumOf { it.durationSeconds } / 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚡ Sectional Study Timer Lobby",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Build deep comprehension limits by studying under scheduled exam pacing.",
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("1. Choose Target Study Section:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sections.forEach { sec ->
                    FilterChip(
                        selected = selectedSection == sec,
                        onClick = { selectedSection = sec },
                        label = { Text(sec, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("2. Set Section Time Limit:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                times.forEach { mins ->
                    FilterChip(
                        selected = selectedMinutes == mins,
                        onClick = { selectedMinutes = mins },
                        label = { Text("${mins} Mins", fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { viewModel.startSectionalStudyTimer(selectedSection, selectedMinutes) },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("start_study_timer_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.FlashOn, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enter Immersive Study Zone (${selectedMinutes}m)", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Focus Logged:", fontSize = 11.sp, color = Color.Gray)
                Text("$totalFocusMinutes Minutes Offline", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun CuratedGuidesSegment(viewModel: SscViewModel, materials: List<StudyMaterial>) {
    var activeCategory by remember { mutableStateOf("All") }
    var expandedMaterialId by remember { mutableStateOf<String?>(null) }
    val categories = listOf("All", "Current Affairs", "English", "Quantitative Aptitude")

    val filtered = materials.filter {
        activeCategory == "All" || it.category == activeCategory
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = activeCategory == cat,
                    onClick = { activeCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No resources available in this category.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)
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
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("SAVED OFFLINE", fontSize = 9.sp, color = Color(0xFF059669), fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = mat.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = mat.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (isExpanded && mat.isDownloaded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = mat.content,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Date: ${mat.dateStr} • ${mat.fileSizeMb}MB", fontSize = 11.sp, color = Color.Gray)
                                Row {
                                    if (mat.isDownloaded) {
                                        TextButton(onClick = { expandedMaterialId = if (isExpanded) null else mat.id }) {
                                            Icon(if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isExpanded) "Collapse" else "Read Offline")
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(onClick = { viewModel.removeDownloadedMaterial(mat.id) }) {
                                            Icon(Icons.Filled.Delete, "Delete", tint = Color.Gray)
                                        }
                                    } else {
                                        Button(onClick = { viewModel.downloadMaterialOffline(mat.id) }) {
                                            Icon(Icons.Filled.Download, null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Download PDF")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiResearchChatbotScreen(viewModel: SscViewModel) {
    val messages = viewModel.chatMessages
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            .padding(12.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(viewModel = viewModel, message = msg)
            }
            if (viewModel.isChatLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Research Engine pulling Google Search summaries...", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = viewModel.chatInputText,
                onValueChange = { viewModel.chatInputText = it },
                placeholder = { Text("Ask study questions...", fontSize = 12.sp) },
                modifier = Modifier.weight(1f).testTag("chat_input_field"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                maxLines = 2,
                singleLine = false
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (viewModel.chatInputText.isNotBlank()) {
                        viewModel.sendChatMessage(viewModel.chatInputText)
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .testTag("chat_send_button")
            ) {
                Icon(Icons.Filled.Send, "Send message", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun ChatBubble(viewModel: SscViewModel, message: ChatMessage) {
    val colors = MaterialTheme.colorScheme
    val isAi = message.isFromAi

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isAi) Alignment.Start else Alignment.End
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isAi) colors.surface else colors.primary
                ),
                border = if (isAi) BorderStroke(1.dp, colors.primary.copy(alpha = 0.15f)) else null,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isAi) 4.dp else 16.dp,
                    bottomEnd = if (isAi) 16.dp else 4.dp
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (isAi) "🤖 GEMINI RESEARCH COACH" else "🙋 ME (ABHISHEK SINGH)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isAi) colors.primary else colors.onPrimary.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.text,
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        color = if (isAi) colors.onSurface else colors.onPrimary
                    )

                    if (isAi) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val lines = message.text.split("\n")
                                val firstHeader = lines.firstOrNull { it.trim().isNotEmpty() && !it.startsWith("#") }?.take(30) ?: "Saved AI Topic Note"
                                viewModel.addStudyNote(
                                    title = firstHeader,
                                    content = message.text,
                                    category = "General Awareness",
                                    isSuggestedByAi = true,
                                    sourceUrl = "Google Web Search Index"
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryContainer, contentColor = colors.onSecondaryContainer)
                        ) {
                            Icon(Icons.Filled.Save, null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save to My Study Notes", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
