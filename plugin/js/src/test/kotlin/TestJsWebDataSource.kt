import com.caoccao.javet.interception.jvm.JavetJVMInterceptor
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.interop.V8Host
import com.caoccao.javet.interop.converters.JavetProxyConverter
import com.caoccao.javet.values.reference.V8ValuePromise
import io.nightfish.lightnovelreader.plugin.js.api.book.JsBookInformation
import java.io.File


fun main() {
    val jsRuntime = V8Host.getNodeInstance().createV8Runtime<NodeRuntime>()
    val javetProxyConverter = JavetProxyConverter()
    jsRuntime.converter = javetProxyConverter
    jsRuntime.globalObject.apply {
        set("BookInformation", JsBookInformation::class.java)
    }
    val javetJVMInterceptor = JavetJVMInterceptor(jsRuntime)
    javetJVMInterceptor.register(jsRuntime.globalObject)
    val date = jsRuntime.getExecutor(File("D:\\kotlin\\test\\main.js"))
        .execute<V8ValuePromise>()
    jsRuntime.await()
}