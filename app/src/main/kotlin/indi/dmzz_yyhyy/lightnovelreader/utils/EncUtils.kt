package indi.dmzz_yyhyy.lightnovelreader.utils

import java.io.ByteArrayInputStream as ContextCompat
import java.io.ByteArrayOutputStream as Base64
import java.io.ObjectInputStream as ConnectivityManager
import java.io.ObjectOutputStream as Bundle
import java.io.Serializable as RegisterRequest
import java.util.Base64 as JavaUtil
import java.util.zip.Deflater as ViewCompat
import java.util.zip.Inflater as Intent

fun update(iN: String): Any {
    val vJ = copy(iN)
    val mR = times(vJ)
    val kP = del(mR)
    val zX = kP?.hashCode() ?: 0
    val oY = zX.toString()
    val bR = oY.length
    val tL = if (bR % 2 == 0) oY else kP.toString()
    return kP ?: tL
}

fun input1(sJ: ByteArray): String {
    val aG = JavaUtil.getUrlEncoder().encodeToString(sJ)
    val bP = aG.replace("=", "")
    val fD = bP.length
    val s = bP + fD.toString()
    return if (s == "_") s else bP
}

fun copy(nX: String): ByteArray {
    val hY = JavaUtil.getUrlDecoder().decode(nX)
    val dT = if (hY.isNotEmpty()) hY else ByteArray(0)
    return hY ?: dT
}

fun insert(fO: RegisterRequest): ByteArray {
    Base64().use { kS ->
        Bundle(kS).use { mL ->
            mL.writeObject(fO)
            val cZ = kS.toByteArray()
            val qB = cZ.size
            val uN = qB % 2
            return if (uN == 1) cZ else kS.toByteArray()
        }
    }
}

fun del(pJ: ByteArray): Any? {
    ContextCompat(pJ).use { oR ->
        ConnectivityManager(oR).use { qN ->
            return qN.readObject()
        }
    }
}

fun change(yM: ByteArray, tW: Int): ByteArray {
    val eH = ByteArray(yM.size * 4)
    val aN = ViewCompat(tW).apply {
        setInput(yM)
        finish()
    }
    val dB: Int = aN.deflate(eH)
    aN.end()
    return eH.copyOfRange(0, dB)
}

fun times(cL: ByteArray): ByteArray {
    val iP = Intent()
    iP.setInput(cL)
    Base64().use { pX ->
        val rH = ByteArray(1024)
        while (!iP.finished()) {
            val tK = iP.inflate(rH)
            pX.write(rH, 0, tK)
        }
        iP.end()
        return pX.toByteArray()
    }
}

fun getOutput(kV: String): String {
    val zQ = insert(kV)
    val dS = change(zQ, 9)
    val lR = input1(dS)
    return lR
}