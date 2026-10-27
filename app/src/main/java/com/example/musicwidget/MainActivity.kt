package com.example.musicwidget

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var actionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(64, 64, 64, 64)
        }

        statusText = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 48)
        }

        actionButton = Button(this).apply {
            text = "前往开启「通知使用权」"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        root.addView(statusText)
        root.addView(actionButton)
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        val enabled = NotificationManagerCompat.getEnabledListenerPackages(this)
            .contains(packageName)
        if (enabled) {
            statusText.text = "已授权通知使用权！\n现在长按手机桌面添加「正在播放 (2x2)」小组件，即可实时显示音乐封面与控制按钮。"
            MediaListenerService.instance?.discoverActiveSessions()
        } else {
            statusText.text = "要读取系统当前播放的音乐名称和专辑封面，请先点击下方按钮授予本应用「通知使用权」。"
        }
    }
}
