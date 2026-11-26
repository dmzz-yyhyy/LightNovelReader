package indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter

import androidx.room.TypeConverter
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

@OptIn(ExperimentalSerializationApi::class)
object ChapterReadingProgressMapConverter {
    @TypeConverter
    fun mapToByteArray(map: Map<Identifier.Chapter, Float>): ByteArray {
        return Cbor.encodeToByteArray(map)
    }

    @TypeConverter
    fun byteArrayToMap(byteArray: ByteArray): Map<Identifier.Chapter, Float> {
        return Cbor.decodeFromByteArray(byteArray)
    }
}
