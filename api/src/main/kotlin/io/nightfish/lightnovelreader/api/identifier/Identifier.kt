package io.nightfish.lightnovelreader.api.identifier

import android.os.Parcelable
import io.nightfish.lightnovelreader.api.identifier.Identifier.Companion.register
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

/**
 * 带有类型与命名空间的标识符
 * 字符串格式为 type:namespace:id, 数据源所属数据使用 插件命名空间.数据源id 作为命名空间
 * 插件可继承[Custom]并通过[register]注册自定义类型的解析方式
 *
 * @since Api 4
 */
@Serializable(IdentifierSerializer::class)
sealed class Identifier : Parcelable {
    /** 标识符类型 @since Api 4 */
    abstract val type: String

    /** 标识符所属的命名空间 @since Api 4 */
    abstract val namespace: String

    /** 命名空间内的唯一标识 @since Api 4 */
    abstract val id: String

    /**
     * 检查标识符格式
     * 子类应在属性完成初始化后调用
     *
     * @since Api 4
     */
    protected fun validate() {
        require(type.matches(Regex("[A-Za-z0-9_-]+"))) { "Invalid identifier type: $type" }
        require(namespace.matches(Regex("[A-Za-z0-9_.-]+")) || type == "book" && namespace == "*" && id == "*") { "Invalid namespace: $namespace" }
        require(id.isNotEmpty()) { "Identifier id must not be empty" }
        require(type != "datasource" || id.none { it == '.' || it == ':' || it == ',' }) { "Datasource id must not contain '.', ':' or ','" }
    }

    /**
     * 转换为 type:namespace:id 格式的字符串
     *
     * @return 标识符字符串
     *
     * @since Api 4
     */
    final override fun toString(): String = "$type:$namespace:$id"

    /**
     * 判断两个标识符的类型、命名空间和 id 是否相同
     *
     * @param other 另一个对象
     * @return 三个字段均相同则返回true
     *
     * @since Api 4
     */
    final override fun equals(other: Any?): Boolean =
        other is Identifier && type == other.type && namespace == other.namespace && id == other.id

    /**
     * 计算标识符的哈希值
     *
     * @return 基于类型、命名空间和 id 的哈希值
     *
     * @since Api 4
     */
    final override fun hashCode(): Int =
        31 * (31 * type.hashCode() + namespace.hashCode()) + id.hashCode()

    /**
     * 书本标识符
     *
     * @property namespace 书本所属的命名空间
     * @property id 命名空间内的唯一标识
     *
     * @since Api 4
     */
    @Parcelize
    @Serializable(Book.Serializer::class)
    class Book(override val namespace: String, override val id: String) : Identifier() {
        override val type: String get() = "book"

        init {
            validate()
        }

        /** 标识符的类型校验序列化器 @since Api 4 */
        object Serializer : KSerializer<Book> by TypedIdentifierSerializer(Book::class.java)
    }

    /**
     * 章节标识符
     *
     * @property namespace 章节所属的命名空间
     * @property id 命名空间内的唯一标识
     *
     * @since Api 4
     */
    @Parcelize
    @Serializable(Chapter.Serializer::class)
    class Chapter(override val namespace: String, override val id: String) : Identifier() {
        override val type: String get() = "chapter"

        init {
            validate()
        }

        /** 标识符的类型校验序列化器 @since Api 4 */
        object Serializer : KSerializer<Chapter> by TypedIdentifierSerializer(Chapter::class.java)
    }

    /**
     * 卷标识符
     *
     * @property namespace 卷所属的命名空间
     * @property id 命名空间内的唯一标识
     *
     * @since Api 4
     */
    @Parcelize
    @Serializable(Volume.Serializer::class)
    class Volume(override val namespace: String, override val id: String) : Identifier() {
        override val type: String get() = "volume"

        init {
            validate()
        }

        /** 标识符的类型校验序列化器 @since Api 4 */
        object Serializer : KSerializer<Volume> by TypedIdentifierSerializer(Volume::class.java)
    }

    /**
     * 书架标识符
     *
     * @property namespace 书架所属的命名空间
     * @property id 命名空间内的唯一标识
     *
     * @since Api 4
     */
    @Parcelize
    @Serializable(Bookshelf.Serializer::class)
    class Bookshelf(override val namespace: String, override val id: String) : Identifier() {
        override val type: String get() = "bookshelf"

        init {
            validate()
        }

        /** 标识符的类型校验序列化器 @since Api 4 */
        object Serializer :
            KSerializer<Bookshelf> by TypedIdentifierSerializer(Bookshelf::class.java)
    }

    /**
     * 数据源标识符
     *
     * @property namespace 数据源所属的命名空间
     * @property id 命名空间内的唯一标识
     *
     * @since Api 4
     */
    @Parcelize
    @Serializable(DataSource.Serializer::class)
    class DataSource(override val namespace: String, override val id: String) : Identifier() {
        override val type: String get() = "datasource"

        init {
            validate()
        }

        /**
         * 数据源所属数据使用的命名空间
         *
         * @since Api 4
         */
        val sourceNamespace: String
            get() {
                return "$namespace.$id"
            }

        /**
         * 创建属于当前数据源的标识符
         *
         * @param T 创建的标识符类型
         * @param factory 标识符的构造函数
         * @param id 数据源内的唯一标识
         * @return 使用当前数据源命名空间的标识符
         *
         * @since Api 4
         */
        fun <T : Identifier> child(factory: (String, String) -> T, id: String): T =
            factory(sourceNamespace, id)

        /** 标识符的类型校验序列化器 @since Api 4 */
        object Serializer :
            KSerializer<DataSource> by TypedIdentifierSerializer(DataSource::class.java)
    }

    /**
     * 设置路径标识符
     *
     * @property namespace 设置路径所属的命名空间
     * @property id 命名空间内的唯一标识
     *
     * @since Api 4
     */
    @Parcelize
    @Serializable(SettingPath.Serializer::class)
    class SettingPath(override val namespace: String, override val id: String) : Identifier() {
        override val type: String get() = "setting_path"

        init {
            validate()
        }

        /** 标识符的类型校验序列化器 @since Api 4 */
        object Serializer :
            KSerializer<SettingPath> by TypedIdentifierSerializer(SettingPath::class.java)
    }

    /**
     * 内容组件标识符
     *
     * @property namespace 内容组件所属的命名空间
     * @property id 命名空间内的唯一标识
     *
     * @since Api 4
     */
    @Parcelize
    @Serializable(ContentComponent.Serializer::class)
    class ContentComponent(override val namespace: String, override val id: String) : Identifier() {
        override val type: String get() = "content_component"

        init {
            validate()
        }

        /** 标识符的类型校验序列化器 @since Api 4 */
        object Serializer :
            KSerializer<ContentComponent> by TypedIdentifierSerializer(ContentComponent::class.java)
    }

    /**
     * 文本处理器标识符
     *
     * @property namespace 文本处理器所属的命名空间
     * @property id 命名空间内的唯一标识
     *
     * @since Api 4
     */
    @Parcelize
    @Serializable(TextProcessor.Serializer::class)
    class TextProcessor(override val namespace: String, override val id: String) : Identifier() {
        override val type: String get() = "text_processor"

        init {
            validate()
        }

        /** 标识符的类型校验序列化器 @since Api 4 */
        object Serializer :
            KSerializer<TextProcessor> by TypedIdentifierSerializer(TextProcessor::class.java)
    }

    /**
     * 图片处理管线标识符
     *
     * @property namespace 图片处理管线所属的命名空间
     * @property id 命名空间内的唯一标识
     *
     * @since Api 4
     */
    @Parcelize
    @Serializable(ImagePipeline.Serializer::class)
    class ImagePipeline(override val namespace: String, override val id: String) : Identifier() {
        override val type: String get() = "image_pipeline"

        init {
            validate()
        }

        /** 标识符的类型校验序列化器 @since Api 4 */
        object Serializer :
            KSerializer<ImagePipeline> by TypedIdentifierSerializer(ImagePipeline::class.java)
    }

    /**
     * 图片变换标识符
     *
     * @property namespace 图片变换所属的命名空间
     * @property id 命名空间内的唯一标识
     *
     * @since Api 4
     */
    @Parcelize
    @Serializable(ImageTransformation.Serializer::class)
    class ImageTransformation(override val namespace: String, override val id: String) :
        Identifier() {
        override val type: String get() = "image_transformation"

        init {
            validate()
        }

        /** 标识符的类型校验序列化器 @since Api 4 */
        object Serializer :
            KSerializer<ImageTransformation> by TypedIdentifierSerializer(ImageTransformation::class.java)
    }

    /**
     * 插件标识符
     *
     * @property namespace 插件所属的命名空间
     * @property id 命名空间内的唯一标识
     *
     * @since Api 4
     */
    @Parcelize
    @Serializable(Plugin.Serializer::class)
    class Plugin(override val namespace: String, override val id: String) : Identifier() {
        override val type: String get() = "plugin"

        init {
            validate()
        }

        /** 标识符的类型校验序列化器 @since Api 4 */
        object Serializer : KSerializer<Plugin> by TypedIdentifierSerializer(Plugin::class.java)
    }

    /**
     * 插件自定义标识符
     * 插件可直接使用自定义类型字符串, 或继承此类并注册解析工厂
     *
     * @property type 插件自定义的类型字符串
     * @property namespace 标识符所属的命名空间
     * @property id 命名空间内的唯一标识
     *
     * @since Api 4
     */
    @Parcelize
    open class Custom(
        final override val type: String,
        final override val namespace: String,
        final override val id: String
    ) : Identifier() {
        init {
            validate()
        }
    }

    /** 标识符解析与公共定义 @since Api 4 */
    companion object {
        /** 对所有书源生效的通用书籍标识符 @since Api 4 */
        val ALL_BOOKS: Book by lazy { Book("*", "*") }

        /** 宿主软件的命名空间 @since Api 4 */
        const val HOST_NAMESPACE = "lightnovelreader"

        private val factories: ConcurrentHashMap<String, (String, String) -> Identifier> =
            ConcurrentHashMap(
                mapOf(
                    "book" to ::Book,
                    "chapter" to ::Chapter,
                    "volume" to ::Volume,
                    "bookshelf" to ::Bookshelf,
                    "datasource" to ::DataSource,
                    "setting_path" to ::SettingPath,
                    "content_component" to ::ContentComponent,
                    "text_processor" to ::TextProcessor,
                    "image_pipeline" to ::ImagePipeline,
                    "image_transformation" to ::ImageTransformation,
                    "plugin" to ::Plugin
                )
            )

        /**
         * 注册插件自定义标识符的解析工厂
         * 已存在的类型不允许重复注册, 工厂必须保留传入的命名空间与 id
         *
         * @param type 自定义类型字符串
         * @param factory 接收命名空间与 id 并创建自定义标识符的函数
         *
         * @since Api 4
         */
        fun register(type: String, factory: (String, String) -> Custom) {
            require(type.matches(Regex("[A-Za-z0-9_-]+"))) { "Invalid identifier type: $type" }
            require(
                factories.putIfAbsent(
                    type,
                    factory
                ) == null
            ) { "Identifier type already registered: $type" }
        }

        /**
         * 解析指定类型的标识符
         * 字符串类型与目标类型不一致时拒绝解析
         *
         * @param T 预期的标识符类型
         * @param value 标识符字符串
         * @return 指定类型的标识符
         *
         * @since Api 4
         */
        inline fun <reified T : Identifier> parseAs(value: String): T {
            val parsed = parse(value)
            require(parsed is T) { "Unexpected identifier type: $value" }
            return parsed
        }

        /**
         * 从 type:namespace:id 格式的字符串解析标识符
         * 未注册的类型保留为[Custom], 注册后的类型由其工厂创建
         *
         * @param value 标识符字符串
         * @return 对应类型的标识符
         *
         * @since Api 4
         */
        fun parse(value: String): Identifier {
            val parts = value.split(':', limit = 3)
            require(parts.size == 3) { "Expected type:namespace:id: $value" }
            val (type, namespace, id) = parts
            val result = factories[type]?.invoke(namespace, id) ?: Custom(type, namespace, id)
            require(result.type == type && result.namespace == namespace && result.id == id) { "Identifier factory changed identity: $value" }
            return result
        }
    }
}
