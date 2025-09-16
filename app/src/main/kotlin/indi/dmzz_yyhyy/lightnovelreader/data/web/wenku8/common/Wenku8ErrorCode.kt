package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.common

/**
 * Wenku8 系统返回的纯数字错误/状态码映射。
 * 参考 MewX/light-novel-library_Wenku8_Android Wenku8Error.getSystemDefinedErrorCode。
 */
@Suppress("EnumEntryName")
enum class Wenku8ErrorCode(val code: Int) {
    SYSTEM_0_REQUEST_ERROR(0),
    SYSTEM_1_SUCCEEDED(1),
    SYSTEM_2_ERROR_USERNAME(2),
    SYSTEM_3_ERROR_PASSWORD(3),
    SYSTEM_4_NOT_LOGGED_IN(4),
    SYSTEM_5_ALREADY_IN_BOOKSHELF(5),
    SYSTEM_6_BOOKSHELF_FULL(6),
    SYSTEM_7_NOVEL_NOT_IN_BOOKSHELF(7),
    SYSTEM_8_TOPIC_NOT_EXIST(8),
    SYSTEM_9_SIGN_FAILED(9),
    SYSTEM_10_RECOMMEND_FAILED(10),
    SYSTEM_11_POST_FAILED(11),
    SYSTEM_22_REFER_PAGE_0(22),
    ERROR_DEFAULT(-1);

    companion object {
        private val map = entries.associateBy { it.code }
        fun fromInt(i: Int): Wenku8ErrorCode = map[i] ?: ERROR_DEFAULT
    }
}
