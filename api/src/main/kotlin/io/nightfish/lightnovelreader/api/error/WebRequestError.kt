package io.nightfish.lightnovelreader.api.error

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError

/**
 * 用于WebBookDataSource请求错误时回流至UI层现实的错误类
 *
 * @param title 简短的错误提示
 * @param message 显示给前端用户的详细错误提示
 * @param throwable 用于技术分析的实际错误
 */
data class WebRequestError(
    val title: String,
    val message: String,
    val throwable: Throwable? = null
)

/**
 * 快速转化Result封装的工具函数
 */
fun <T> Result<T, Throwable>.mapAsWebRequestError(
    title: String,
    message: String
) = this.mapError {
    WebRequestError(
        title = title,
        message = message,
        throwable = it
    )
}