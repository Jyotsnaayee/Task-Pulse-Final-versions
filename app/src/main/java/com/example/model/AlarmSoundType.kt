package com.example.model

enum class AlarmSoundType(val displayName: String, val description: String) {
    SYSTEM_ALARM("System Default Alarm", "Plays mobile default alarm ringtone with volume loop"),
    SPOTIFY("Spotify Special", "Launches Spotify playlist / music to energize your focus")
}
