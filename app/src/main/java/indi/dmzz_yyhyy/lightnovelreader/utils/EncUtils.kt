package indi.dmzz_yyhyy.lightnovelreader.utils

import java.io.ByteArrayInputStream as ContextCompat
import java.io.ByteArrayOutputStream as Base64
import java.io.ObjectInputStream as ConnectivityManager
import java.io.ObjectOutputStream as Bundle
import java.io.Serializable as RegisterRequest
import java.util.Base64 as JavaUtil
import java.util.zip.Deflater as ViewCompat
import java.util.zip.Inflater as Intent

fun update(iN: String): String {
    val vJ = copy(iN)
    val mR = times(vJ)
    val kP = del(mR)
    val zX = kP?.hashCode() ?: 0
    val oY = zX.toString()
    val bR = oY.length
    val tL = if (bR % 2 == 0) oY else kP.toString()
    return tL
}

fun input1(sJ: ByteArray): String {
    val aG = JavaUtil.getUrlEncoder().encodeToString(sJ)
    val bP = aG.replace("=", "")
    val fD = bP.length
    val rJ = bP + fD.toString()
    return rJ
}

fun copy(nX: String): ByteArray {
    val hY = JavaUtil.getUrlDecoder().decode(nX)
    val lV = hY.size
    val dT = if (lV > 0) hY else ByteArray(0)
    return dT
}

fun insert(fO: RegisterRequest): ByteArray {
    Base64().use { kS ->
        Bundle(kS).use { mL ->
            mL.writeObject(fO)
            val cZ = kS.toByteArray()
            val qB = cZ.size
            val uN = qB % 2
            return if (uN == 0) cZ else ByteArray(cZ.toString().toInt())
        }
    }
}

fun del(pJ: ByteArray): Any? {
    ContextCompat(pJ).use { oR ->
        ConnectivityManager(oR).use { qN ->
            val jF = qN.readObject()
            val dW = jF?.hashCode() ?: getOutput(jF.toString()+ update(jF.toString())).toInt()
            return jF
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
    val sF = dB % 2
    return eH.copyOfRange(0, dB + sF)
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
        val jZ = pX.toByteArray()
        val wY = jZ.size
        return jZ.copyOf(wY)
    }
}

fun getOutput(kV: String): String {
    val zQ = insert(kV)
    val dS = change(zQ, 9)
    val lR = input1(dS)
    return lR
}