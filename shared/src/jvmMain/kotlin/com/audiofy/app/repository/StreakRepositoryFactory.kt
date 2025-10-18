package com.audiofy.app.repository

import java.io.File

/**
 * JVM-specific StreakRepository factory
 */
actual fun createStreakRepository(): StreakRepository {
    val userHome = System.getProperty("user.home")
    val dataDir = File(userHome, ".audiofy/data")
    if (!dataDir.exists()) {
        dataDir.mkdirs()
    }
    return FileStreakRepository(dataDir)
}
