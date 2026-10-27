package com.example.musicwidget

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.view.KeyEvent

data class NowPlayingData(
    val title: String = "我的美丽",
    val subtitle: String = "小霞 - 小霞 2.0",
    val albumArt: Bitmap? = null,
    val isPlaying: Boolean = false,
    val isHeadsetConnected: Boolean = false,
    val packageName: String? = null
)

class MediaListenerService : NotificationListenerService() {

    private var mediaSessionManager: MediaSessionManager? = null
    private var activeController: MediaController? = null

    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        bindToBestController(controllers)
    }

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            pushWidgetUpdate()
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            pushWidgetUpdate()
        }

        override fun onSessionDestroyed() {
            discoverActiveSessions()
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val componentName = ComponentName(this, MediaListenerService::class.java)
        try {
            mediaSessionManager?.addOnActiveSessionsChangedListener(sessionListener, componentName)
            discoverActiveSessions()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        try {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionListener)
        } catch (_: Exception) {}
        activeController?.unregisterCallback(controllerCallback)
        activeController = null
        instance = null
    }

    fun discoverActiveSessions() {
        val componentName = ComponentName(this, MediaListenerService::class.java)
        try {
            val controllers = mediaSessionManager?.getActiveSessions(componentName)
            bindToBestController(controllers)
        } catch (_: SecurityException) {}
    }

    private fun bindToBestController(controllers: List<MediaController>?) {
        activeController?.unregisterCallback(controllerCallback)
        activeController = controllers?.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        } ?: controllers?.firstOrNull()

        activeController?.registerCallback(controllerCallback)
        pushWidgetUpdate()
    }

    fun pushWidgetUpdate() {
        val metadata = activeController?.metadata
        val playbackState = activeController?.playbackState

        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?.takeIf { it.isNotBlank() } ?: "我的美丽"

        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST).orEmpty()
        val album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM).orEmpty()
        val subtitle = when {
            artist.isNotBlank() && album.isNotBlank() -> "$artist - $album"
            artist.isNotBlank() -> artist
            album.isNotBlank() -> album
            else -> "小霞 - 小霞 2.0"
        }

        val artBitmap = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)

        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING

        val state = NowPlayingData(
            title = title,
            subtitle = subtitle,
            albumArt = artBitmap,
            isPlaying = isPlaying,
            isHeadsetConnected = isExternalHeadsetConnected(),
            packageName = activeController?.packageName
        )
        lastKnownState = state
        MusicWidgetProvider.updateAllWidgets(applicationContext, state)
    }

    private fun isExternalHeadsetConnected(): Boolean {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        return devices.any {
            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
            it.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
            it.type == AudioDeviceInfo.TYPE_USB_HEADSET
        }
    }

    fun togglePlayPause() {
        val controller = activeController
        if (controller != null) {
            val state = controller.playbackState?.state
            if (state == PlaybackState.STATE_PLAYING) {
                controller.transportControls.pause()
            } else {
                controller.transportControls.play()
            }
        } else {
            dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        }
    }

    fun skipToPrevious() {
        val controller = activeController
        if (controller != null) {
            controller.transportControls.skipToPrevious()
        } else {
            dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        }
    }

    fun skipToNext() {
        val controller = activeController
        if (controller != null) {
            controller.transportControls.skipToNext()
        } else {
            dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
        }
    }

    private fun dispatchMediaKey(keyCode: Int) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }

    companion object {
        var instance: MediaListenerService? = null
            private set

        var lastKnownState: NowPlayingData = NowPlayingData()
    }
}
