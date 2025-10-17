package com.audiofy.app.repository

/**
 * iOS-specific PodcastRepository factory
 * Creates NSUserDefaults-based repository
 */
actual fun createPodcastRepository(): PodcastRepository {
    return NSUserDefaultsPodcastRepository()
}
