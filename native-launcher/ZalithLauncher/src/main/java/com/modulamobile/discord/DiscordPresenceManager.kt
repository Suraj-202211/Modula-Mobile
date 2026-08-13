package com.modulamobile.discord

import android.util.Log

enum class AppState {
    IN_MENUS, LAUNCHING, IN_GAME, IDLE
}

class DiscordPresenceManager {
    private var isEnabled = true
    private var currentState = AppState.IDLE
    private var startTime: Long = 0
    private val applicationId = "1325785006095597578" // Default Discord App ID

    companion object {
        val instance by lazy { DiscordPresenceManager() }
    }

    fun initialize(enabled: Boolean = true) {
        isEnabled = enabled
        if (!isEnabled) {
            disconnect()
            return
        }
        
        Log.i("DiscordRPC", "Initializing Modula Mobile Discord Presence with ID: $applicationId")
        startTime = System.currentTimeMillis()
        updateState(AppState.IN_MENUS)
    }

    fun updateState(state: AppState) {
        if (!isEnabled) return
        currentState = state
        
        val details = when (state) {
            AppState.IN_MENUS -> "Browsing Modula Mobile"
            AppState.LAUNCHING -> "Launching Game Engine"
            AppState.IN_GAME -> "Playing Minecraft: Java Edition"
            AppState.IDLE -> "AFK"
        }
        
        val stateText = when (state) {
            AppState.IN_MENUS -> "v1.0.0-GOLDEN"
            AppState.LAUNCHING -> "Warming up JVM"
            AppState.IN_GAME -> "Singleplayer / Multiplayer"
            AppState.IDLE -> ""
        }

        sendPresenceUpdate(details, stateText)
    }

    private fun sendPresenceUpdate(details: String, state: String) {
        // In a full implementation, this uses com.discord:discord-rpc-android or 
        // IPC broadcast to the Discord app.
        // For the scope of this update, we mock the IPC broadcast.
        Log.i("DiscordRPC", "Updating Presence -> Details: $details | State: $state | StartTime: $startTime")
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (enabled) {
            initialize(true)
        } else {
            disconnect()
        }
    }

    private fun disconnect() {
        Log.i("DiscordRPC", "Disconnecting Discord Presence.")
        currentState = AppState.IDLE
    }
}
