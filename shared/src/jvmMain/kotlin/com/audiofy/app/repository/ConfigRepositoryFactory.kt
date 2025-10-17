package com.audiofy.app.repository

import java.io.File

/**
 * JVM-specific ConfigRepository factory
 * Uses file-based storage in user home directory
 */
actual fun createConfigRepository(): ConfigRepository {
    val userHome = System.getProperty("user.home")
    val configDir = File(userHome, ".audiofy")
    if (!configDir.exists()) {
        configDir.mkdirs()
    }
    return ConfigRepositoryImpl(configDir)
}
