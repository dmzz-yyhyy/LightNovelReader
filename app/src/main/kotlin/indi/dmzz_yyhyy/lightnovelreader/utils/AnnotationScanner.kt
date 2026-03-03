package indi.dmzz_yyhyy.lightnovelreader.utils

import android.annotation.SuppressLint
import android.util.Log
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.runCatching
import com.github.michaelbull.result.unwrap
import com.github.michaelbull.result.unwrapError
import dalvik.system.DexClassLoader
import dalvik.system.DexFile
import java.lang.reflect.Field

@Suppress("DEPRECATION")
object AnnotationScanner {
    private const val TAG = "AnnotationScanner"

    /**
     * 扫描DexClassLoader中带有指定注解的所有类
     *
     * @param classLoader 要扫描的DexClassLoader
     * @param annotationClass 要查找的注解类型
     * @return 带有指定注解的类列表
     */
    @SuppressLint("NewApi")
    fun findAnnotatedClasses(
        classLoader: DexClassLoader,
        annotationClass: Class<out Annotation?>,
        scanPackage: String = "",
    ): Result<MutableList<Class<*>>, Throwable> =
        findField(classLoader, "pathList")
            .andThen { pathListField ->
                pathListField.get(classLoader)?.let(::Ok) ?: Err(Error("Failed to get path list"))
            }.andThen { pathList ->
                findField(pathList, "dexElements")
                    .andThen { dexElementsField ->
                        runCatching {
                            @Suppress("UNCHECKED_CAST")
                            dexElementsField.get(pathList) as Array<Any>
                        }
                    }
            }.andThen { dexElements ->
                val result: MutableList<Class<*>> = ArrayList()
                for (dexElement in dexElements) {
                    val dexFileField = findField(dexElement, "dexFile")
                    if (dexFileField.isErr) {
                        dexFileField
                            .unwrapError()
                            .printStackTrace()
                        continue
                    }
                    val dexFile = dexFileField
                        .unwrap()
                        .get(dexElement) as? DexFile
                        ?: continue
                    val classNames = dexFile.entries()

                    while (classNames.hasMoreElements()) {
                        val className = classNames.nextElement()
                        if (className.isNullOrEmpty() || !className.contains(".") ||
                            (scanPackage.isNotEmpty() && !className.startsWith(scanPackage))) continue
                        val clazz = classLoader.loadClass(className)
                        if (clazz.isAnnotationPresent(annotationClass)) {
                            result.add(clazz)
                            Log.d(TAG, "Found annotated class: $className")
                        }
                    }

                }
                Ok(result)
            }

    private fun findField(instance: Any, name: String): Result<Field, Throwable> {
        var clazz: Class<*>? = instance.javaClass
        while (clazz != null) {
            try {
                val field = clazz.getDeclaredField(name)
                field.isAccessible = true
                return Ok(field)
            } catch (_: NoSuchFieldException) {
                clazz = clazz.superclass
            }
        }
        return Err(NoSuchFieldException("Field " + name + " not found in " + instance.javaClass))
    }
}
