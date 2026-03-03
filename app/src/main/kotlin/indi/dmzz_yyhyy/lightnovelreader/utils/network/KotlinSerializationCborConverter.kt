package indi.dmzz_yyhyy.lightnovelreader.utils.network

import cxhttp.converter.CxHttpConverter
import cxhttp.response.CxHttpResult
import cxhttp.response.Response
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.cbor.CborBuilder
import kotlinx.serialization.serializer
import java.lang.reflect.Type

@ExperimentalSerializationApi
@Suppress("UNCHECKED_CAST")
class KotlinSerializationCborConverter (
    cbor: Cbor = Cbor,
    builderAction: CborBuilder.() -> Unit = {}
): CxHttpConverter {

    override val contentType: String = "KotlinSerializationJson"
    private val cbor = Cbor(cbor, builderAction)

    override fun <T> convert(body: Response.Body, tType: Class<T>): T {
        return cbor.decodeFromByteArray(cbor.serializersModule.serializer(tType), body.bytes()) as T
    }

    override fun <T, RESULT : CxHttpResult<T>> convertResult(body: Response.Body, resultType: Class<RESULT>, tType: Type): RESULT {
        return cbor.decodeFromByteArray(cbor.serializersModule.serializer(resultType), body.bytes()) as RESULT
    }

    override fun <T, RESULT : CxHttpResult<List<T>>> convertResultList(body: Response.Body, resultType: Class<RESULT>, tType: Type): RESULT {
        return cbor.decodeFromByteArray(cbor.serializersModule.serializer(resultType), body.bytes()) as RESULT
    }

    override fun <T> convert(value: T, tType: Class<out T>): ByteArray {
        return cbor.encodeToByteArray(cbor.serializersModule.serializer(tType), value as Any)
    }
}