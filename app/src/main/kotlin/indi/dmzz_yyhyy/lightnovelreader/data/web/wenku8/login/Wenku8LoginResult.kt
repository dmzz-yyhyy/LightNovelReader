package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.login

sealed class Wenku8LoginResult {
    data class Success(
        val userName: String,
        val cookieMap: Map<String, String>,
    ) : Wenku8LoginResult()
    sealed class Failure : Wenku8LoginResult() {
        object Network : Failure()
        object Username : Failure()
        object Password : Failure()
        object NotLoggedIn : Failure() // code=4 when returned in contexts requiring auth
        data class Code(val code: Int) : Failure() // other mapped numeric codes
        object Unknown : Failure()
    }
}
