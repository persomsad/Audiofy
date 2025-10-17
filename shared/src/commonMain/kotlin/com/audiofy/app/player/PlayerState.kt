package com.audiofy.app.player

/**
 * Audio Player State
 */
enum class PlayerState {
    IDLE,       // Not loaded
    LOADING,    // Loading audio file
    READY,      // Loaded, ready to play
    PLAYING,    // Currently playing
    PAUSED,     // Paused
    ERROR       // Error occurred
}
