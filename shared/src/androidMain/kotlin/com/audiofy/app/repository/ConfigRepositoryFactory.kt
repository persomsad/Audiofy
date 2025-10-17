package com.audiofy.app.repository

import android.content.Context

// Global context holder (will be initialized in MainActivity)
private lateinit var appContext: Context

/**
 * Initialize the global application context
 * Must be called from MainActivity.onCreate()
 */
fun initializeContext(context: Context) {
    appContext = context.applicationContext
}

/**
 * Get the application context
 * Internal function for use by other repositories
 */
internal fun getAppContext(): Context {
    if (!::appContext.isInitialized) {
        throw IllegalStateException("Context not initialized! Call initializeContext() from MainActivity first.")
    }
    return appContext
}

/**
 * Android-specific ConfigRepository factory
 * Requires Android Context to create DataStore
 */
actual fun createConfigRepository(): ConfigRepository {
    return ConfigRepositoryImpl(getAppContext())
}
