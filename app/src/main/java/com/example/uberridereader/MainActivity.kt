package com.example.uberridereader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var pickupText: TextView
    private lateinit var dropoffText: TextView
    private lateinit var rawText: TextView

    private val rideReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val pickup = intent.getStringExtra(RideParser.EXTRA_PICKUP)
            val dropoff = intent.getStringExtra(RideParser.EXTRA_DROPOFF)
            val raw = intent.getStringExtra(RideParser.EXTRA_RAW)

            pickupText.text = "Embarque: ${pickup ?: "não identificado"}"
            dropoffText.text = "Desembarque: ${dropoff ?: "não identificado"}"
            rawText.text = "Texto original da notificação:\n$raw"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        pickupText = findViewById(R.id.pickupText)
        dropoffText = findViewById(R.id.dropoffText)
        rawText = findViewById(R.id.rawText)

        findViewById<android.widget.Button>(R.id.grantPermissionButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        updatePermissionStatus()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()

        val filter = IntentFilter(RideParser.ACTION_RIDE_PARSED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(rideReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(rideReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(rideReceiver)
    }

    private fun updatePermissionStatus() {
        val enabledListeners = Settings.Secure.getString(
            contentResolver, "enabled_notification_listeners"
        ) ?: ""
        val granted = enabledListeners.contains(packageName)

        statusText.text = if (granted) {
            "Permissão de notificações concedida. Aguardando corridas..."
        } else {
            "Permissão de notificações NÃO concedida. Toque no botão abaixo."
        }
    }
}
