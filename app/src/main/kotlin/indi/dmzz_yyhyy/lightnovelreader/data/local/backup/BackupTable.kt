package indi.dmzz_yyhyy.lightnovelreader.data.local.backup

/** 声明实体的导出分类和数据归属列 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class BackupTable(
    val group: BackupGroup,
    val scopeColumn: String = "id",
    val scopeType: String = ""
)

enum class BackupGroup { CACHE, BOOKSHELF, READING, SETTINGS, USER_DATA, METADATA }
