package com.audiofy.app.repository

/**
 * Android-specific StreakRepository factory
 */
actual fun createStreakRepository(): StreakRepository {
    return DataStoreStreakRepository(getAppContext())
}
