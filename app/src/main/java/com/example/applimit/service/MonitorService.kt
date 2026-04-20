package com.example.applimit.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MonitorService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
