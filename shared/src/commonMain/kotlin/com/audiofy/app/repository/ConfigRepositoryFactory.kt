package com.audiofy.app.repository

/**
 * Factory function to create platform-specific ConfigRepository
 * Uses expect/actual pattern for platform implementations
 */
expect fun createConfigRepository(): ConfigRepository
