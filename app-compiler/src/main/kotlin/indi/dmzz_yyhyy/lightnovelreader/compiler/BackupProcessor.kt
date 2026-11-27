package indi.dmzz_yyhyy.lightnovelreader.compiler

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

/** 为宿主生成 Room 访问接口、备份表清单和合并调用 */
class BackupProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        BackupProcessor(environment.codeGenerator, environment.logger)
}

private class BackupProcessor(private val generator: CodeGenerator, private val logger: KSPLogger) :
    SymbolProcessor {
    private var generated = false
    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (generated) return emptyList()
        val database = resolver.getSymbolsWithAnnotation("androidx.room.Database")
            .filterIsInstance<KSClassDeclaration>()
            .firstOrNull { it.simpleName.asString() == "LightNovelReaderDatabase" }
            ?: return emptyList()
        val annotation =
            database.annotations.first { it.annotationType.resolve().declaration.qualifiedName?.asString() == "androidx.room.Database" }
        val entities =
            (annotation.arguments.first { it.name?.asString() == "entities" }.value as List<*>)
                .mapNotNull { (it as? KSType)?.declaration as? KSClassDeclaration }
        if (entities.isEmpty()) return emptyList()
        data class Table(
            val type: String,
            val method: String,
            val table: String,
            val group: String,
            val column: String,
            val prefix: String,
            val autoKey: String?
        )

        val tables = entities.map { entity ->
            fun KSAnnotation.arg(name: String) =
                arguments.firstOrNull { it.name?.asString() == name }?.value

            val room =
                entity.annotations.first { it.annotationType.resolve().declaration.qualifiedName?.asString() == "androidx.room.Entity" }
            val backup =
                entity.annotations.firstOrNull { it.annotationType.resolve().declaration.qualifiedName?.asString() == "indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupTable" }
            if (backup == null) {
                logger.error(
                    "Every Room entity must declare @BackupTable and its export category",
                    entity
                ); return emptyList()
            }
            val autoProperty = entity.getAllProperties().firstOrNull { property ->
                property.annotations.any {
                    it.annotationType.resolve().declaration.qualifiedName?.asString() == "androidx.room.PrimaryKey" && it.arg(
                        "autoGenerate"
                    ) == true
                }
            }
            val autoKey = autoProperty?.let { property ->
                property.annotations.firstOrNull { it.annotationType.resolve().declaration.qualifiedName?.asString() == "androidx.room.ColumnInfo" }
                    ?.arg("name")?.toString()?.takeUnless { it == "[field-name]" }
                    ?: property.simpleName.asString()
            }
            Table(
                entity.qualifiedName!!.asString(),
                entity.simpleName.asString(),
                room.arg("tableName") as String,
                backup.arg("group").toString().substringAfterLast('.'),
                backup.arg("scopeColumn") as String,
                backup.arg("scopeType") as String,
                autoKey
            )
        }
        val dependencies = Dependencies(
            true,
            *entities.mapNotNull { it.containingFile }.plus(database.containingFile!!).distinct()
                .toTypedArray()
        )
        val pkg = "indi.dmzz_yyhyy.lightnovelreader.data.local.room"
        generator.createNewFile(dependencies, pkg, "BackupAccess").writer().use { writer ->
            writer.appendLine("package $pkg")
            listOf(
                "androidx.room.Dao", "androidx.room.RawQuery", "androidx.room.Insert",
                "androidx.room.OnConflictStrategy", "androidx.sqlite.db.SupportSQLiteQuery"
            )
                .plus(tables.map { it.type }).forEach { writer.appendLine("import $it") }
            writer.appendLine("@Dao\ninterface BackupAccess {")
            tables.forEach { table ->
                writer.appendLine("@RawQuery suspend fun read${table.method}(query: SupportSQLiteQuery): List<${table.method}>")
                writer.appendLine("@Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun write${table.method}(entity: ${table.method})")
            }
            writer.appendLine("}")
        }
        generator.createNewFile(dependencies, pkg, "GeneratedBackup").writer().use { writer ->
            writer.appendLine("package $pkg")
            listOf(
                "androidx.sqlite.db.SupportSQLiteQuery",
                "indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupGroup",
                "indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupTableInfo"
            )
                .forEach { writer.appendLine("import $it") }
            writer.appendLine("object GeneratedBackup {\nval tables = listOf(")
            writer.appendLine(tables.joinToString(",\n") {
                "BackupTableInfo(\"${it.table}\", BackupGroup.${it.group}, \"${it.column}\", \"${it.prefix}\", ${it.autoKey?.let { key -> "\"$key\"" } ?: "null"})"
            })
            writer.appendLine(")")
            writer.appendLine("suspend fun merge(table: String, source: BackupAccess, target: BackupAccess, incoming: SupportSQLiteQuery, existing: SupportSQLiteQuery) {\nwhen(table) {")
            tables.forEach { table ->
                writer.appendLine("\"${table.table}\" -> { val old = target.read${table.method}(existing).single(); val next = source.read${table.method}(incoming).single(); target.write${table.method}(old.merge(next)) }")
            }
            writer.appendLine("else -> error(\"Unregistered backup table: \" + table)\n}\n}")
            writer.appendLine("suspend fun validate(table: String, source: BackupAccess, query: SupportSQLiteQuery) { when(table) {")
            tables.forEach { table -> writer.appendLine("\"${table.table}\" -> source.read${table.method}(query).single()") }
            writer.appendLine("else -> error(table)\n} }\n}")
        }
        generated = true
        return emptyList()
    }
}
