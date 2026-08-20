package io.nightfish.potatoautoproxy

import org.jsoup.Connection
import org.jsoup.nodes.Document
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

private fun ignoreSSL() {
    try {
        val context = SSLContext.getInstance("TLS")
        context.init(
            null, arrayOf<X509TrustManager>(
            @Suppress("CustomX509TrustManager")
            object : X509TrustManager {
                @Suppress("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                @Suppress("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate?> {
                    return arrayOfNulls(0)
                }
            }
        ), SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(context.socketFactory)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


private fun resumeSSL() {
    HttpsURLConnection.setDefaultSSLSocketFactory(SSLSocketFactory.getDefault() as SSLSocketFactory)
}

fun Connection.ignoreSSLGet(): Document {
    ignoreSSL()
    return this.get().also {
        resumeSSL()
    }
}

fun Connection.ignoreSSLPost(): Document {
    ignoreSSL()
    return this.post().also {
        resumeSSL()
    }
}