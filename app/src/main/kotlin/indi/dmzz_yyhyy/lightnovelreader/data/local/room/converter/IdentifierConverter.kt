package indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter

import androidx.room.TypeConverter
import io.nightfish.lightnovelreader.api.identifier.Identifier

object IdentifierConverter {
    @TypeConverter
    fun bookToString(value: Identifier.Book?): String? = value?.toString()
    @TypeConverter
    fun stringToBook(value: String?): Identifier.Book? =
        value?.let { Identifier.parseAs<Identifier.Book>(it) }

    @TypeConverter
    fun chapterToString(value: Identifier.Chapter?): String? = value?.toString()
    @TypeConverter
    fun stringToChapter(value: String?): Identifier.Chapter? =
        value?.let { Identifier.parseAs<Identifier.Chapter>(it) }

    @TypeConverter
    fun volumeToString(value: Identifier.Volume?): String? = value?.toString()
    @TypeConverter
    fun stringToVolume(value: String?): Identifier.Volume? =
        value?.let { Identifier.parseAs<Identifier.Volume>(it) }

    @TypeConverter
    fun bookshelfToString(value: Identifier.Bookshelf?): String? = value?.toString()
    @TypeConverter
    fun stringToBookshelf(value: String?): Identifier.Bookshelf? =
        value?.let { Identifier.parseAs<Identifier.Bookshelf>(it) }

    @TypeConverter
    fun dataSourceToString(value: Identifier.DataSource?): String? = value?.toString()
    @TypeConverter
    fun stringToDataSource(value: String?): Identifier.DataSource? =
        value?.let { Identifier.parseAs<Identifier.DataSource>(it) }

    @TypeConverter
    fun settingPathToString(value: Identifier.SettingPath?): String? = value?.toString()
    @TypeConverter
    fun stringToSettingPath(value: String?): Identifier.SettingPath? =
        value?.let { Identifier.parseAs<Identifier.SettingPath>(it) }

    @TypeConverter
    fun contentComponentToString(value: Identifier.ContentComponent?): String? = value?.toString()
    @TypeConverter
    fun stringToContentComponent(value: String?): Identifier.ContentComponent? =
        value?.let { Identifier.parseAs<Identifier.ContentComponent>(it) }

    @TypeConverter
    fun textProcessorToString(value: Identifier.TextProcessor?): String? = value?.toString()
    @TypeConverter
    fun stringToTextProcessor(value: String?): Identifier.TextProcessor? =
        value?.let { Identifier.parseAs<Identifier.TextProcessor>(it) }

    @TypeConverter
    fun imagePipelineToString(value: Identifier.ImagePipeline?): String? = value?.toString()
    @TypeConverter
    fun stringToImagePipeline(value: String?): Identifier.ImagePipeline? =
        value?.let { Identifier.parseAs<Identifier.ImagePipeline>(it) }

    @TypeConverter
    fun imageTransformationToString(value: Identifier.ImageTransformation?): String? =
        value?.toString()

    @TypeConverter
    fun stringToImageTransformation(value: String?): Identifier.ImageTransformation? =
        value?.let { Identifier.parseAs<Identifier.ImageTransformation>(it) }

    @TypeConverter
    fun pluginToString(value: Identifier.Plugin?): String? = value?.toString()
    @TypeConverter
    fun stringToPlugin(value: String?): Identifier.Plugin? =
        value?.let { Identifier.parseAs<Identifier.Plugin>(it) }
}
