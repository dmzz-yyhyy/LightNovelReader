package indi.dmzz_yyhyy.lightnovelreader.utils

import com.android.apksig.ApkVerifier
import java.io.File
import java.security.MessageDigest
import java.security.PublicKey
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import java.util.Date
import java.util.EnumSet
import kotlin.experimental.and

enum class ApkSignatureScheme { V1, V2, V3, V31, V4, UNKNOWN }

data class ApkSignatureInfo(
    val schemes: Set<ApkSignatureScheme>,
    val issuer: String,
    val subject: String,
    val notBefore: Date,
    val notAfter: Date,
    val publicKeyAlgorithm: String,
    val publicKeyLength: Int,
    val sha1: String,
    val sha256: String
)

fun bytesToHex(bytes: ByteArray): String {
    val sb = StringBuilder(bytes.size * 2)
    for (b in bytes) {
        sb.append(String.format("%02x", b and 0xff.toByte()))
    }
    return sb.toString()
}

private fun digestHex(algorithm: String, data: ByteArray): String {
    val md = MessageDigest.getInstance(algorithm)
    return bytesToHex(md.digest(data))
}

private fun publicKeyLengthBits(pub: PublicKey): Int {
    return if (pub is RSAPublicKey) pub.modulus.bitLength() else -1
}

fun getApkSignatures(apkFile: File): List<ApkSignatureInfo>? {
    if (!apkFile.exists() || !apkFile.isFile) return null
    try {
        val verifier = ApkVerifier.Builder(apkFile).build()
        val result = verifier.verify()
        if (!result.isVerified && result.signerCertificates.isEmpty()) return null

        val certMap = HashMap<String, Pair<X509Certificate, MutableSet<ApkSignatureScheme>>>()

        fun recordCert(cert: X509Certificate?, scheme: ApkSignatureScheme) {
            if (cert == null) return
            val key = bytesToHex(cert.encoded)
            val pair =
                certMap.getOrPut(key) { Pair(cert, EnumSet.noneOf(ApkSignatureScheme::class.java)) }
            pair.second.add(scheme)
        }

        result.v1SchemeSigners.forEach { recordCert(it.certificate, ApkSignatureScheme.V1) }
        result.v1SchemeIgnoredSigners.forEach { recordCert(it.certificate, ApkSignatureScheme.V1) }
        result.v2SchemeSigners.forEach { recordCert(it.certificate, ApkSignatureScheme.V2) }
        result.v3SchemeSigners.forEach { recordCert(it.certificate, ApkSignatureScheme.V3) }
        result.v31SchemeSigners.forEach { recordCert(it.certificate, ApkSignatureScheme.V31) }
        result.v4SchemeSigners.forEach { recordCert(it.certificate, ApkSignatureScheme.V4) }

        val out = ArrayList<ApkSignatureInfo>()

        fun cleanDn(dn: String): String {
            return dn.split(",")
                .filterNot { it.trim().startsWith("1.2.840.113549.1.9.1=") }
                .joinToString(",") { it.trim() }
        }

        for ((_, pair) in certMap) {
            val cert = pair.first
            val schemes = pair.second
            val issuer = cleanDn(cert.issuerX500Principal.name)
            val subject = cleanDn(cert.subjectX500Principal.name)
            val notBefore = cert.notBefore
            val notAfter = cert.notAfter
            val pub = cert.publicKey
            val pubAlg = pub.algorithm
            val pubLen = publicKeyLengthBits(pub)
            val sha1 = digestHex("SHA-1", cert.encoded)
            val sha256 = digestHex("SHA-256", cert.encoded)

            out.add(
                ApkSignatureInfo(
                    schemes = schemes,
                    issuer = issuer,
                    subject = subject,
                    notBefore = notBefore,
                    notAfter = notAfter,
                    publicKeyAlgorithm = pubAlg,
                    publicKeyLength = pubLen,
                    sha1 = sha1,
                    sha256 = sha256
                )
            )
        }
        println("GET sig == $out")
        return out
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun isSignatureMatch(
    existing: List<ApkSignatureInfo>?,
    incoming: List<ApkSignatureInfo>?
): Boolean {
    println("CHECK > \n$incoming\n$existing")
    if (existing.isNullOrEmpty() && incoming.isNullOrEmpty()) return true
    if (existing.isNullOrEmpty() || incoming.isNullOrEmpty()) return false

    val existingSet = existing.map { it.sha256 }.toSet()
    val incomingSet = incoming.map { it.sha256 }.toSet()

    return existingSet == incomingSet
}
