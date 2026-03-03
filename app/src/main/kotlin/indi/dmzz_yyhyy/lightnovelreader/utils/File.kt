package indi.dmzz_yyhyy.lightnovelreader.utils

import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

fun InputStream.readAppLocalData() = ZipInputStream(this)
    .use {
        it.nextEntry
        it.readBytes()
    }

fun OutputStream.writeAppLocalData(data: ByteArray) = ZipOutputStream(this)
    .use {
        it.putNextEntry(ZipEntry("data"))
        it.write(data)
        it.closeEntry()
    }