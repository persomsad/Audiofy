package com.audiofy.app.repository

/**
 * Factory function to create platform-specific StreakRepository
 */
expect fun createStreakRepository(): StreakRepository
