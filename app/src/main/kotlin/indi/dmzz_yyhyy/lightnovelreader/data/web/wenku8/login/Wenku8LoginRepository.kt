package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.login

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.common.Wenku8ErrorCode
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 负责执行 Wenku8 登录并提取会话相关 Cookie 的仓库。
 */
@Singleton
class Wenku8LoginRepository @Inject constructor() {

    private val loginUrl = "http://app.wenku8.com/android.php"

    private fun buildParamString(isEmail: Boolean, account: String, password: String): String {
        val encodedUser = encode(account)
        val encodedPwd = encode(password)
        return if (isEmail) {
            "action=loginemail&username=$encodedUser&password=$encodedPwd"
        } else {
            "action=login&username=$encodedUser&password=$encodedPwd"
        }
    }

    private fun encode(v: String): String = java.net.URLEncoder.encode(v, "UTF-8")

    /**
     * 执行登录。
     * @return Wenku8LoginResult 表示成功或失败原因。
     */
    suspend fun login(account: String, password: String): Wenku8LoginResult = withContext(Dispatchers.IO) {
        if (account.isBlank() || password.isBlank()) return@withContext Wenku8LoginResult.Failure.Unknown

        val isEmail = account.contains('@')
        val paramString = buildParamString(isEmail, account, password)

        fun applyEncrypted(connection: org.jsoup.Connection, raw: String) {
            val base64 = android.util.Base64.encodeToString(raw.toByteArray(), android.util.Base64.NO_WRAP)
            connection.data("appver", indi.dmzz_yyhyy.lightnovelreader.BuildConfig.VERSION_NAME)
            connection.data("request", base64)
            connection.data("timetoken", System.currentTimeMillis().toString())
        }

        val maxAttempts = 2
        var lastError: Exception? = null
        repeat(maxAttempts) { attempt ->
            try {
                val connection = Jsoup.connect(loginUrl)
                    .userAgent("")
                    .method(org.jsoup.Connection.Method.POST)

                applyEncrypted(connection, paramString)

                val response = connection.execute()
                val body = response.body()
                Log.d("Wenku8Login", "attempt=${attempt+1} body=$body")

                if (body.isBlank()) return@withContext Wenku8LoginResult.Failure.Unknown

                when {
                    body.contains("用户名", ignoreCase = true) && body.contains("错误") && body.contains("密码").not() -> return@withContext Wenku8LoginResult.Failure.Username
                    body.contains("密码", ignoreCase = true) && body.contains("错误") -> return@withContext Wenku8LoginResult.Failure.Password
                    body.contains("成功", ignoreCase = true) || body.contains("success", ignoreCase = true) -> {
                        return@withContext Wenku8LoginResult.Success(userName = account, cookieMap = response.cookies())
                    }
                    body.all { it.isDigit() } -> {
                        val codeInt = body.toIntOrNull()
                        if (codeInt != null) {
                            when (Wenku8ErrorCode.fromInt(codeInt)) {
                                Wenku8ErrorCode.SYSTEM_1_SUCCEEDED -> {
                                    return@withContext Wenku8LoginResult.Success(userName = account, cookieMap = response.cookies())
                                }
                                Wenku8ErrorCode.SYSTEM_2_ERROR_USERNAME -> return@withContext Wenku8LoginResult.Failure.Username
                                Wenku8ErrorCode.SYSTEM_3_ERROR_PASSWORD -> return@withContext Wenku8LoginResult.Failure.Password
                                Wenku8ErrorCode.SYSTEM_4_NOT_LOGGED_IN -> return@withContext Wenku8LoginResult.Failure.NotLoggedIn
                                else -> return@withContext Wenku8LoginResult.Failure.Code(codeInt)
                            }
                        }
                        return@withContext Wenku8LoginResult.Failure.Unknown
                    }
                    else -> {
                        if (attempt == maxAttempts - 1) return@withContext Wenku8LoginResult.Failure.Unknown
                    }
                }
            } catch (e: Exception) {
                lastError = e
                Log.w("Wenku8Login", "attempt failed ${attempt+1}", e)
                if (attempt == maxAttempts - 1) {
                    return@withContext Wenku8LoginResult.Failure.Network
                }
                delay(300L * (attempt + 1))
            }
        }
        lastError?.let { Log.e("Wenku8Login", "unreachable fallback", it) }
        Wenku8LoginResult.Failure.Unknown
    }
}
