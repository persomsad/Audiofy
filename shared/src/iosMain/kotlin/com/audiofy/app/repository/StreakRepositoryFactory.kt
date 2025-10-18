package com.audiofy.app.repository

/**
 * iOS-specific StreakRepository factory
 */
actual fun createStreakRepository(): StreakRepository {
    return NSUserDefaultsStreakRepository()
}
