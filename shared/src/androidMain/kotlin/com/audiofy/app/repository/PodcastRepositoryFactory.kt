package com.audiofy.app.repository

/**
 * Android-specific PodcastRepository factory
 * Reuses the same Context as ConfigRepository
 */
actual fun createPodcastRepository(): PodcastRepository {
    return DataStorePodcastRepository(getAppContext())
}
