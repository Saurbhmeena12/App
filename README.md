# Learning App - Android Application

A comprehensive Android learning platform built with Kotlin, Jetpack Compose, and MVVM architecture.

## Features

✅ **Course Management**
- Browse available courses by category and level
- Detailed course information with instructor details
- Track course progress

✅ **Lesson Content**
- Structured lesson content with text and video support
- Lesson duration tracking
- Mark lessons as completed

✅ **Quizzes & Assessments**
- Multiple-choice quizzes for each lesson
- Score calculation and passing requirements
- Detailed quiz explanations

✅ **Progress Tracking**
- Real-time progress visualization
- Course completion percentage
- Quiz performance metrics
- Certificate tracking

✅ **User Authentication & Profiles**
- User progress persistence
- Course history
- Achievement tracking

## Architecture

### MVVM (Model-View-ViewModel) Pattern
- **Model**: Data entities and repositories
- **View**: Jetpack Compose UI components
- **ViewModel**: Business logic and state management

### Key Technologies
- **UI Framework**: Jetpack Compose
- **Database**: Room Database
- **Navigation**: Jetpack Navigation
- **Dependency Injection**: Dagger Hilt
- **Async**: Kotlin Coroutines & Flow
- **Image Loading**: Coil

## Project Structure

```
app/
├── data/
│   ├── local/
│   │   ├── dao/          # Database access objects
│   │   ├── database/     # Room database setup
│   │   └── entity/       # Data entities
│   └── repository/       # Data repositories
├── di/                   # Dependency injection modules
└── ui/
    ├── screens/          # UI screens
    │   ├── home/
    │   ├── course/
    │   ├── lesson/
    │   ├── quiz/
    │   └── progress/
    ├── navigation/       # Navigation setup
    └── theme/            # Theme configuration
```

## Database Schema

### Tables:
1. **courses** - Course information
2. **lessons** - Lesson content and metadata
3. **quizzes** - Quiz information
4. **quiz_questions** - Quiz questions and answers
5. **user_progress** - User progress tracking

## Getting Started

### Prerequisites
- Android Studio Giraffe or later
- Kotlin 1.8 or later
- Minimum SDK: API 24
- Target SDK: API 34

### Installation

1. Clone the repository
```bash
git clone https://github.com/Saurbhmeena12/App.git
```

2. Open in Android Studio
```bash
cd App
```

3. Build and run
```bash
gradle build
```

## Usage

### Screens

#### 1. Home Screen
- Displays all available courses
- Filter by category and level
- Course cards with preview information

#### 2. Course Detail Screen
- Full course description
- Instructor information
- List of lessons
- Progress indicator

#### 3. Lesson Screen
- Lesson content display
- Video player support
- Mark as complete
- Access to associated quiz

#### 4. Quiz Screen
- Multiple-choice questions
- Answer selection
- Progress indicator
- Score calculation
- Results display

#### 5. Progress Screen
- Overall progress summary
- Individual course progress
- Quiz scores
- Certificate tracking

## API Reference

### CourseRepository
```kotlin
- getAllCourses(): Flow<List<CourseEntity>>
- getCourseById(courseId: String): Flow<CourseEntity?>
- getCoursesByCategory(category: String): Flow<List<CourseEntity>>
- getCoursesByLevel(level: String): Flow<List<CourseEntity>>
```

### LessonRepository
```kotlin
- getLessonsByCourse(courseId: String): Flow<List<LessonEntity>>
- getLessonById(lessonId: String): Flow<LessonEntity?>
- markLessonAsCompleted(lessonId: String)
```

### QuizRepository
```kotlin
- getQuizByLesson(lessonId: String): Flow<QuizEntity?>
- getQuizQuestions(quizId: String): Flow<List<QuizQuestionEntity>>
```

### ProgressRepository
```kotlin
- getUserProgress(userId: String): Flow<List<UserProgressEntity>>
- getCourseProgress(userId: String, courseId: String): Flow<UserProgressEntity?>
- getOverallProgress(userId: String): Flow<Double?>
```

## Sample Data

The app comes with sample courses:
1. **Kotlin Basics** (Beginner) - 10 lessons
2. **Android UI Design** (Intermediate) - 15 lessons
3. **Advanced Coroutines** (Advanced) - 12 lessons

## Future Enhancements

- [ ] User authentication system
- [ ] Backend API integration
- [ ] Video streaming
- [ ] Offline support
- [ ] Push notifications
- [ ] Social features (comments, discussions)
- [ ] Certificate generation and sharing
- [ ] Multiple language support
- [ ] Adaptive learning paths
- [ ] Performance analytics

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

**Saurabh Meena** - Learning App Developer

## Support

For support, email support@learningapp.com or open an issue on GitHub.

## Acknowledgments

- Jetpack Compose documentation and samples
- Room Database best practices
- MVVM architecture patterns
- Kotlin coroutines and Flow
