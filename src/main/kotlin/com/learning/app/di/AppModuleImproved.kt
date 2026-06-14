package com.learning.app.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.learning.app.data.local.database.AppDatabase
import com.learning.app.data.local.database.OfflineDatabase
import com.learning.app.data.local.preferences.TokenManager
import com.learning.app.data.remote.api.ApiClient
import com.learning.app.data.remote.api.LearningApiService
import com.learning.app.data.remote.api.RetryPolicy
import com.learning.app.data.repository.*
import com.learning.app.utils.network.NetworkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideNetworkManager(@ApplicationContext context: Context): NetworkManager {
        return NetworkManager(context)
    }

    @Singleton
    @Provides
    fun provideRetryPolicy(): RetryPolicy {
        return RetryPolicy()
    }

    @Singleton
    @Provides
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Singleton
    @Provides
    fun provideLearningApiService(tokenManager: TokenManager): LearningApiService {
        val retrofit = ApiClient.createRetrofit(
            "https://api.learningapp.com/",
            tokenManager
        )
        return retrofit.create(LearningApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideMainDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "learning_app_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideOfflineDatabase(@ApplicationContext context: Context): OfflineDatabase {
        return Room.databaseBuilder(
            context,
            OfflineDatabase::class.java,
            "offline_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideSyncScheduler(@ApplicationContext context: Context): SyncScheduler {
        return SyncScheduler(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideAuthRepository(
        apiService: LearningApiService,
        tokenManager: TokenManager,
        retryPolicy: RetryPolicy
    ): AuthRepository {
        return AuthRepository(apiService, tokenManager, retryPolicy)
    }

    @Singleton
    @Provides
    fun provideCourseRepositoryImproved(
        courseDao: com.learning.app.data.local.dao.CourseDao,
        offlineContentDao: com.learning.app.data.local.dao.OfflineContentDao,
        apiService: LearningApiService,
        networkManager: NetworkManager,
        retryPolicy: RetryPolicy
    ): CourseRepositoryImproved {
        return CourseRepositoryImproved(
            courseDao,
            offlineContentDao,
            apiService,
            networkManager,
            retryPolicy
        )
    }

    @Singleton
    @Provides
    fun provideOfflineRepository(
        offlineDatabase: OfflineDatabase
    ): OfflineRepository {
        return OfflineRepository(
            offlineDatabase.offlineContentDao(),
            offlineDatabase.syncQueueDao()
        )
    }

    @Singleton
    @Provides
    fun provideRemoteRepository(
        apiService: LearningApiService
    ): RemoteRepository {
        return RemoteRepository(apiService)
    }
}
