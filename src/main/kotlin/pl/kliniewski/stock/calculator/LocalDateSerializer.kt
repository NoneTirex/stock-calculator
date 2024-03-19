package pl.kliniewski.stock.calculator

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

open class LocalDateSerializer(private val formatter: DateTimeFormatter) : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("java.time.LocalDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate = run {
        formatter.parse(decoder.decodeString()) {
            LocalDate.from(it)
        }
    }

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(formatter.format(value))
    }
}