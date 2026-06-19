package io.nightfish.lightnovelreader.api.identifier

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 用于注册使用的标识符
 *
 * @property namespace 标识符的命名控件
 * @property id 标识符的名称
 *
 * @since APi 4
 */
@Parcelize
@Serializable(IdentifierSerializer::class)
data class Identifier(
    val namespace: String,
    val id: String
): Parcelable {


    /**
     * 将对象变为文字标识符
     * 格式为"$namespace:$id"
     */
    override fun toString() = "$namespace:$id"

    override fun equals(other: Any?): Boolean {
        if (other is Identifier) {
            return other.namespace == this.namespace && other.id == this.id
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = namespace.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }
}