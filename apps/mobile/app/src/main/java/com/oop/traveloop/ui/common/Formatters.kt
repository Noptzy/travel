package com.oop.traveloop.ui.common

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.oop.traveloop.domain.model.Budget
import com.oop.traveloop.domain.model.TransportOption
import com.oop.traveloop.domain.model.TravelPackage
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToLong

fun rupiah(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).format(value.roundToLong()).replace(",00", "")

fun formatDate(value: String): String = runCatching {
    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID")))
}.getOrDefault(value)

fun mapsUrl(sourceUrl: String?, name: String, address: String): String {
    val url = sourceUrl.orEmpty()
    if (url.startsWith("http") && (url.contains("google.", ignoreCase = true) || url.contains("maps", ignoreCase = true))) return url
    val query = URLEncoder.encode("$name $address", StandardCharsets.UTF_8.toString())
    return "https://www.google.com/maps/search/?api=1&query=$query"
}

fun adjustedBudget(pack: TravelPackage, transport: TransportOption): Budget {
    val base = pack.budget
    val subtotal = base.estimated - base.buffer - base.intercityTransport + transport.estimatedCost
    val buffer = subtotal * 0.10
    val estimated = subtotal + buffer
    return base.copy(
        intercityTransport = transport.estimatedCost,
        buffer = buffer,
        estimated = estimated,
        remaining = base.total - estimated,
    )
}

object IdrVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (text.text.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val formatted = "IDR ${NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID")).format(text.text.toLongOrNull() ?: 0)}"
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 4
                var seen = 0
                formatted.forEachIndexed { index, char ->
                    if (char.isDigit() && ++seen == offset) return index + 1
                }
                return formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int =
                formatted.take(offset.coerceIn(0, formatted.length)).count(Char::isDigit).coerceAtMost(text.length)
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
