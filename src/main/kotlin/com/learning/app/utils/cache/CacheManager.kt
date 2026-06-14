package com.learning.app.utils.cache

import android.content.Context
import java.io.File

class CacheManager(context: Context) {
    companion object {
        private const val MAX_CACHE_SIZE_MB = 100 // 100 MB max cache
        private const val CACHE_VALIDITY_DAYS = 7
    }

    private val cacheDir = File(context.cacheDir, "learning_app_cache")

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    fun getCacheSize(): Long {
        return if (cacheDir.exists()) {
            cacheDir.walkTopDown().map { it.length() }.sum()
        } else {
            0L
        }
    }

    fun isCacheFull(): Boolean {
        val currentSize = getCacheSize()
        return currentSize > (MAX_CACHE_SIZE_MB * 1024 * 1024)
    }

    fun clearOldCache() {
        val currentTime = System.currentTimeMillis()
        val validityMs = CACHE_VALIDITY_DAYS * 24 * 60 * 60 * 1000L

        cacheDir.walkTopDown().forEach { file ->
            if (file.isFile && (currentTime - file.lastModified()) > validityMs) {
                file.delete()
            }
        }
    }

    fun clearAllCache() {
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
    }
}
