package com.example.uberridereader

data class RideInfo(
    val pickupAddress: String?,
    val dropoffAddress: String?
)

/**
 * Heurística simples para extrair embarque/desembarque do texto da notificação.
 *
 * O texto de solicitação de corrida da Uber Driver costuma vir em formatos como:
 *   "Nova viagem - R$ 18,50"
 *   "Embarque: Rua das Flores, 123\nDestino: Av. Paulista, 900"
 *
 * Como a Uber não documenta esse formato oficialmente e ele muda com o tempo,
 * este parser tenta alguns padrões comuns. Ajuste as regex depois de observar
 * notificações reais no seu aparelho (use Log.d para capturar o texto cru
 * antes de refinar aqui).
 */
object RideParser {

    const val ACTION_RIDE_PARSED = "com.example.uberridereader.ACTION_RIDE_PARSED"
    const val EXTRA_PICKUP = "extra_pickup"
    const val EXTRA_DROPOFF = "extra_dropoff"
    const val EXTRA_RAW = "extra_raw"

    private val pickupRegex = Regex(
        pattern = "(?:embarque|coleta|partida|pickup)\\s*[:\\-]\\s*(.+)",
        option = RegexOption.IGNORE_CASE
    )

    private val dropoffRegex = Regex(
        pattern = "(?:destino|desembarque|drop ?off)\\s*[:\\-]\\s*(.+)",
        option = RegexOption.IGNORE_CASE
    )

    fun parse(title: String, body: String): RideInfo? {
        // Só processa se parecer ser uma notificação de solicitação de corrida.
        val looksLikeRideRequest = listOf("viagem", "corrida", "trip", "ride")
            .any { title.contains(it, ignoreCase = true) || body.contains(it, ignoreCase = true) }

        if (!looksLikeRideRequest) return null

        val lines = body.split("\n")

        var pickup: String? = null
        var dropoff: String? = null

        for (line in lines) {
            pickupRegex.find(line)?.let { pickup = it.groupValues[1].trim() }
            dropoffRegex.find(line)?.let { dropoff = it.groupValues[1].trim() }
        }

        // Se não achou nada estruturado, ainda assim retorna o corpo bruto
        // para você ver na tela e ajustar o parser manualmente.
        if (pickup == null && dropoff == null) {
            return RideInfo(pickupAddress = null, dropoffAddress = null)
        }

        return RideInfo(pickupAddress = pickup, dropoffAddress = dropoff)
    }
}
