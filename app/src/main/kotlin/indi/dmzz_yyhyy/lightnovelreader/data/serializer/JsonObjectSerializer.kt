package indi.dmzz_yyhyy.lightnovelreader.data.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class JsonObjectSerializer : KSerializer<JsonObject> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("JsonObject", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: JsonObject) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): JsonObject {
        val localDateString = decoder.decodeString()
        return Json.parseToJsonElement(localDateString).jsonObject
    }
}