package com.audiofy.app.player

/**
 * JVM Audio Player Implementation (stub for desktop)
 */
actual class AudioPlayer actual constructor() {
    private var currentState: PlayerState = PlayerState.IDLE
    private var stateListener: ((PlayerState) -> Unit)? = null

    actual fun loadAudio(filePath: String) {
        // TODO: Implement JVM audio loading (e.g., using javax.sound or JavaFX Media)
        currentState = PlayerState.ERROR
        stateListener?.invoke(currentState)
    }

    actual fun play() {
        // TODO: Implement JVM audio playback
    }

    actual fun pause() {
        // TODO: Implement JVM pause
    }

    actual fun stop() {
        // TODO: Implement JVM stop
    }

    actual fun seekTo(position: Long) {
        // TODO: Implement JVM seek
    }

    actual fun getCurrentPosition(): Long {
        return 0L
    }

    actual fun getDuration(): Long {
        return -1L
    }

    actual fun getState(): PlayerState = currentState

    actual fun setOnStateChangedListener(listener: (PlayerState) -> Unit) {
        stateListener = listener
    }

    actual fun release() {
        currentState = PlayerState.IDLE
    }
}
