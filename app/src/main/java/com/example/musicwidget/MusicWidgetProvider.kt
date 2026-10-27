package com.example.musicwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.RemoteViews

class MusicWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        MediaListenerService.instance?.discoverActiveSessions()
        updateAllWidgets(context, MediaListenerService.lastKnownState)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_PLAY_PAUSE -> MediaListenerService.instance?.togglePlayPause()
            ACTION_PREV -> MediaListenerService.instance?.skipToPrevious()
            ACTION_NEXT -> MediaListenerService.instance?.skipToNext()
            ACTION_OPEN_PLAYER -> {
                val pkg = MediaListenerService.lastKnownState.packageName
                val launchIntent = pkg?.let { context.packageManager.getLaunchIntentForPackage(it) }
                    ?: Intent(context, MainActivity::class.java)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
            ACTION_AUDIO_OUTPUT -> openSystemAudioOutputSwitcher(context)
        }
    }

    private fun openSystemAudioOutputSwitcher(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent("com.android.settings.panel.action.MEDIA_OUTPUT").apply {
                MediaListenerService.lastKnownState.packageName?.let {
                    putExtra("com.android.settings.panel.extra.PACKAGE_NAME", it)
                }
            }
        } else {
            Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.musicwidget.ACTION_PLAY_PAUSE"
        const val ACTION_PREV = "com.example.musicwidget.ACTION_PREV"
        const val ACTION_NEXT = "com.example.musicwidget.ACTION_NEXT"
        const val ACTION_OPEN_PLAYER = "com.example.musicwidget.ACTION_OPEN_PLAYER"
        const val ACTION_AUDIO_OUTPUT = "com.example.musicwidget.ACTION_AUDIO_OUTPUT"

        fun updateAllWidgets(context: Context, data: NowPlayingData) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, MusicWidgetProvider::class.java)
            )
            if (ids.isEmpty()) return

            val views = RemoteViews(context.packageName, R.layout.widget_music_player)

            val bgBitmap = BitmapUiHelper.createGradientCardBackground(data.albumArt)
            val roundedCover = BitmapUiHelper.createRoundedAlbumArt(data.albumArt)
            views.setImageViewBitmap(R.id.iv_widget_bg, bgBitmap)
            views.setImageViewBitmap(R.id.iv_album_art, roundedCover)

            views.setTextViewText(R.id.tv_song_title, data.title)
            views.setTextViewText(R.id.tv_artist_album, data.subtitle)

            val playPauseIcon = if (data.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            views.setImageViewResource(R.id.btn_play_pause, playPauseIcon)

            views.setOnClickPendingIntent(R.id.btn_prev, buildBroadcastPendingIntent(context, ACTION_PREV, 1))
            views.setOnClickPendingIntent(R.id.btn_play_pause, buildBroadcastPendingIntent(context, ACTION_PLAY_PAUSE, 2))
            views.setOnClickPendingIntent(R.id.btn_next, buildBroadcastPendingIntent(context, ACTION_NEXT, 3))
            views.setOnClickPendingIntent(R.id.iv_album_art, buildBroadcastPendingIntent(context, ACTION_OPEN_PLAYER, 4))
            views.setOnClickPendingIntent(R.id.ll_track_info, buildBroadcastPendingIntent(context, ACTION_OPEN_PLAYER, 5))
            views.setOnClickPendingIntent(R.id.btn_audio_output, buildBroadcastPendingIntent(context, ACTION_AUDIO_OUTPUT, 6))

            for (id in ids) {
                appWidgetManager.updateAppWidget(id, views)
            }
        }

        private fun buildBroadcastPendingIntent(
            context: Context,
            action: String,
            requestCode: Int
        ): PendingIntent {
            val intent = Intent(context, MusicWidgetProvider::class.java).apply {
                this.action = action
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
