package indi.dmzz_yyhyy.lightnovelreader.utils

import java.text.DecimalFormat
import kotlin.math.log
import kotlin.math.pow

enum class FileSizeUnit(
    val symbol: String,
    val power: Int
) {
    B("B", 0),
    KB("KB", 1),
    MB("MB", 2),
    GB("GB", 3),
    TB("TB", 4);

    val bytes: Double
        get() = 1024.0.pow(power)
}

private val sizeFormat = DecimalFormat("#,##0.#")

fun formatSize(
    size: Long,
    minUnit: FileSizeUnit = FileSizeUnit.KB,
    maxUnit: FileSizeUnit = FileSizeUnit.GB
): String {
    require(minUnit.power <= maxUnit.power) {
        "minUnit must not be greater than maxUnit"
    }

    if (size <= 0L) {
        return "0 ${minUnit.symbol}"
    }

    val rawUnit = FileSizeUnit.entries
        .getOrElse(log(size.toDouble(), 1024.0).toInt()) {
            FileSizeUnit.TB
        }

    val unit = when {
        rawUnit.power < minUnit.power -> return "<1 ${minUnit.symbol}"
        rawUnit.power > maxUnit.power -> maxUnit
        else -> rawUnit
    }

    val value = size / unit.bytes
    return "${sizeFormat.format(value)} ${unit.symbol}"
}