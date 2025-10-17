package com.audiofy.app.player

/**
 * Cross-platform Audio Player Interface
 * Uses expect/actual pattern for platform-specific implementations
 */
expect class AudioPlayer() {
    /**
     * Load audio from file path
     * @param filePath Absolute file path to audio file
     */
    fun loadAudio(filePath: String)

    /**
     * Start or resume playback
     */
    fun play()

    /**
     * Pause playback
     */
    fun pause()

    /**
     * Stop playback and reset position
     */
    fun stop()

    /**
     * Seek to position in milliseconds
     * @param position Position in milliseconds
     */
    fun seekTo(position: Long)

    /**
     * Get current playback position in milliseconds
     * @return Current position in milliseconds
     */
    fun getCurrentPosition(): Long

    /**
     * Get total duration in milliseconds
     * @return Duration in milliseconds, -1 if not available
     */
    fun getDuration(): Long

    /**
     * Get current player state
     * @return Current PlayerState
     */
    fun getState(): PlayerState

    /**
     * Set state change listener
     * @param listener Callback when state changes
     */
    fun setOnStateChangedListener(listener: (PlayerState) -> Unit)

    /**
     * Release player resources
     * Must be called when player is no longer needed
     */
    fun release()
}
