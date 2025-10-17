package com.audiofy.app.repository

/**
 * iOS-specific ConfigRepository factory
 * Uses UserDefaults for storage
 */
actual fun createConfigRepository(): ConfigRepository {
    return ConfigRepositoryImpl()
}
