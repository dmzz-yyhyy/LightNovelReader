package io.nightfish.lightnovelreader.api.compiler

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * 用于自动将注解了[io.nightfish.lightnovelreader.api.plugin.Plugin]与[io.nightfish.lightnovelreader.api.web.WebDataSource]注解的类的路径与元数据写入清单文件内
 */
class EntryProcessor(
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {

    private fun generateManifest(pluginClass: String, webDataSourceClassList: List<String>) {
        val file = codeGenerator.createNewFile(
            Dependencies(false),
            "",
            "auto_register_manifest",
            "xml"
        )

        file.writer().use { writer ->
            writer.write(
                """
                <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                          xmlns:tools="http://schemas.android.com/tools">
                    <application>
                    <meta-data android:name="lnr_plugin" android:value="$pluginClass" tools:node="merge"/>
                    <meta-data android:name="lnr_web_data_source" android:value="${webDataSourceClassList.joinToString(separator = ";")}" tools:node="merge"/>
                    </application>
                </manifest>
                """.trimIndent()
            )
        }
    }

    /**
     * 注解处理器
     * 如果[io.nightfish.lightnovelreader.api.plugin.Plugin]注解不存在, 则跳过所有的处理
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val pluginSymbols = resolver
            .getSymbolsWithAnnotation("io.nightfish.lightnovelreader.api.plugin.Plugin")
            .filterIsInstance<KSClassDeclaration>()

        val webDataSourceSymbols = resolver
            .getSymbolsWithAnnotation("io.nightfish.lightnovelreader.api.web.WebDataSource")
            .filterIsInstance<KSClassDeclaration>()

        val pluginClass = pluginSymbols.firstNotNullOfOrNull {
            it.qualifiedName?.asString()
        } ?: return emptyList()

        val dataSourceClassList = webDataSourceSymbols.mapNotNull {
            it.qualifiedName?.asString()
        }.toList()

        generateManifest(pluginClass, dataSourceClassList)
        return emptyList()
    }
}
