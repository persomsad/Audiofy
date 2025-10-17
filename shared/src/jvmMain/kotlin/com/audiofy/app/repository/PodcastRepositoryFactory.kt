package com.audiofy.app.repository

import java.io.File

/**
 * JVM-specific PodcastRepository factory
 * Uses file-based storage in user home directory
 */
actual fun createPodcastRepository(): PodcastRepository {
    val userHome = System.getProperty("user.home")
    val dataDir = File(userHome, ".audiofy/data")
    if (!dataDir.exists()) {
        dataDir.mkdirs()
    }
    return FilePodcastRepository(dataDir)
}
