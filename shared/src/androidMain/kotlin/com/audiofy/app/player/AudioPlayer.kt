package com.audiofy.app.player

import android.media.MediaPlayer
import java.io.IOException

/**
 * Android Audio Player Implementation using MediaPlayer
 */
actual class AudioPlayer actual constructor() {
    private var mediaPlayer: MediaPlayer? = null
    private var currentState: PlayerState = PlayerState.IDLE
    private var stateListener: ((PlayerState) -> Unit)? = null

    actual fun loadAudio(filePath: String) {
        try {
            // Release previous player if exists
            release()

            updateState(PlayerState.LOADING)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setOnPreparedListener {
                    updateState(PlayerState.READY)
                }
                setOnCompletionListener {
                    updateState(PlayerState.IDLE)
                }
                setOnErrorListener { _, what, extra ->
                    updateState(PlayerState.ERROR)
                    true
                }
                prepareAsync()
            }
        } catch (e: IOException) {
            updateState(PlayerState.ERROR)
        }
    }

    actual fun play() {
        mediaPlayer?.let { player ->
            if (currentState == PlayerState.READY || currentState == PlayerState.PAUSED) {
                player.start()
                updateState(PlayerState.PLAYING)
            }
        }
    }

    actual fun pause() {
        mediaPlayer?.let { player ->
            if (currentState == PlayerState.PLAYING) {
                player.pause()
                updateState(PlayerState.PAUSED)
            }
        }
    }

    actual fun stop() {
        mediaPlayer?.let { player ->
            if (currentState == PlayerState.PLAYING || currentState == PlayerState.PAUSED) {
                player.stop()
                player.prepare()
                player.seekTo(0)
                updateState(PlayerState.READY)
            }
        }
    }

    actual fun seekTo(position: Long) {
        mediaPlayer?.let { player ->
            if (currentState != PlayerState.IDLE && currentState != PlayerState.LOADING) {
                player.seekTo(position.toInt())
            }
        }
    }

    actual fun getCurrentPosition(): Long {
        return mediaPlayer?.let { player ->
            if (currentState != PlayerState.IDLE && currentState != PlayerState.LOADING) {
                player.currentPosition.toLong()
            } else {
                0L
            }
        } ?: 0L
    }

    actual fun getDuration(): Long {
        return mediaPlayer?.let { player ->
            if (currentState != PlayerState.IDLE && currentState != PlayerState.LOADING) {
                player.duration.toLong()
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
        mediaPlayer?.release()
        mediaPlayer = null
        updateState(PlayerState.IDLE)
    }

    private fun updateState(newState: PlayerState) {
        if (currentState != newState) {
            currentState = newState
            stateListener?.invoke(newState)
        }
    }
}
