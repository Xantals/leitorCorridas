package com.example.uberridereader

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * Escuta TODAS as notificações do sistema e filtra apenas as do app do
 * motorista Uber (pacote com.ubercab.driver).
 *
 * IMPORTANTE:
 * - O usuário precisa habilitar manualmente em:
 *   Configurações > Apps > Acesso especial > Acesso a notificações
 * - O texto exato das notificações da Uber pode mudar com atualizações
 *   do app deles; os regex abaixo são heurísticos e podem precisar de ajuste.
 */
class UberNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "UberNotifListener"

        // Pacote do app Uber Driver. Confirme o valor real via
        // `adb shell dumpsys notification` enquanto uma notificação chega.
        private const val UBER_DRIVER_PACKAGE = "com.ubercab.driver"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        if (sbn.packageName != UBER_DRIVER_PACKAGE) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: text

        Log.d(TAG, "Notificação Uber recebida: title=$title text=$bigText")

        val ride = RideParser.parse(title, bigText)

        if (ride != null) {
            // Envia os dados extraídos para a Activity via broadcast local.
            val intent = Intent(RideParser.ACTION_RIDE_PARSED).apply {
                putExtra(RideParser.EXTRA_PICKUP, ride.pickupAddress)
                putExtra(RideParser.EXTRA_DROPOFF, ride.dropoffAddress)
                putExtra(RideParser.EXTRA_RAW, bigText)
                setPackage(packageName)
            }
            sendBroadcast(intent)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
    }
}
