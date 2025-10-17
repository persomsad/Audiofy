package com.audiofy.app.repository

/**
 * Factory function to create platform-specific PodcastRepository
 * Uses expect/actual pattern for platform implementations
 */
expect fun createPodcastRepository(): PodcastRepository
