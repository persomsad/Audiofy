package com.audiofy.app.player

import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSError
import platform.Foundation.NSURL

/**
 * iOS Audio Player Implementation using AVAudioPlayer
 */
actual class AudioPlayer actual constructor() {
    private var audioPlayer: AVAudioPlayer? = null
    private var currentState: PlayerState = PlayerState.IDLE
    private var stateListener: ((PlayerState) -> Unit)? = null

    actual fun loadAudio(filePath: String) {
        try {
            // Release previous player if exists
            release()

            updateState(PlayerState.LOADING)

            val url = NSURL.fileURLWithPath(filePath)
            val error: NSError? = null

            audioPlayer = AVAudioPlayer(contentsOfURL = url, error = error).apply {
                if (error != null) {
                    updateState(PlayerState.ERROR)
                    return
                }

                prepareToPlay()
                updateState(PlayerState.READY)
            }
        } catch (e: Exception) {
            updateState(PlayerState.ERROR)
        }
    }

    actual fun play() {
        audioPlayer?.let { player ->
            if (currentState == PlayerState.READY || currentState == PlayerState.PAUSED) {
                val success = player.play()
                if (success) {
                    updateState(PlayerState.PLAYING)
                } else {
                    updateState(PlayerState.ERROR)
                }
            }
        }
    }

    actual fun pause() {
        audioPlayer?.let { player ->
            if (currentState == PlayerState.PLAYING) {
                player.pause()
                updateState(PlayerState.PAUSED)
            }
        }
    }

    actual fun stop() {
        audioPlayer?.let { player ->
            if (currentState == PlayerState.PLAYING || currentState == PlayerState.PAUSED) {
                player.stop()
                player.currentTime = 0.0
                player.prepareToPlay()
                updateState(PlayerState.READY)
            }
        }
    }

    actual fun seekTo(position: Long) {
        audioPlayer?.let { player ->
            if (currentState != PlayerState.IDLE && currentState != PlayerState.LOADING) {
                player.currentTime = position / 1000.0 // Convert milliseconds to seconds
            }
        }
    }

    actual fun getCurrentPosition(): Long {
        return audioPlayer?.let { player ->
            if (currentState != PlayerState.IDLE && currentState != PlayerState.LOADING) {
                (player.currentTime * 1000).toLong() // Convert seconds to milliseconds
            } else {
                0L
            }
        } ?: 0L
    }

    actual fun getDuration(): Long {
        return audioPlayer?.let { player ->
            if (currentState != PlayerState.IDLE && currentState != PlayerState.LOADING) {
                (player.duration * 1000).toLong() // Convert seconds to milliseconds
            } else {
                -1L
            }
        } ?: -1L
    }

    actual fun getState(): PlayerState = currentState

    actual fun setOnStateChangedListener(listener: (PlayerState) -> Unit) {
        stateListener = listener
    }

    actual fun release() {
        audioPlayer?.stop()
        audioPlayer = null
        updateState(PlayerState.IDLE)
    }

    private fun updateState(newState: PlayerState) {
        if (currentState != newState) {
            currentState = newState
            stateListener?.invoke(newState)
        }
    }
}
