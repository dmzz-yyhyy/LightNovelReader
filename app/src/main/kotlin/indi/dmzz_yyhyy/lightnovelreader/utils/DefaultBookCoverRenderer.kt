package indi.dmzz_yyhyy.lightnovelreader.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.withTranslation
import androidx.core.net.toUri
import indi.dmzz_yyhyy.lightnovelreader.R
import java.io.File

object DefaultBookCoverRenderer {
    const val DEFAULT_WIDTH = 600
    const val DEFAULT_HEIGHT = 870

    private const val BACKGROUND_COLOR = 0xFF302F36.toInt()
    private const val BORDER_COLOR = 0xFF45434B.toInt()
    private const val DECORATION_COLOR = 0xFF44424A.toInt()
    private const val CONTENT_COLOR = 0xFFC9C7CF.toInt()

    fun render(
        context: Context,
        title: String,
        width: Int = DEFAULT_WIDTH,
        height: Int = DEFAULT_HEIGHT
    ): Bitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        canvas.drawColor(BACKGROUND_COLOR)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BORDER_COLOR
            style = Paint.Style.STROKE
            strokeWidth = width * 0.009f
        }
        canvas.drawRect(
            width * 0.055f,
            height * 0.045f,
            width * 0.945f,
            height * 0.955f,
            strokePaint
        )
        strokePaint.strokeWidth = width * 0.004f
        canvas.drawRect(
            width * 0.105f,
            height * 0.08f,
            width * 0.895f,
            height * 0.92f,
            strokePaint
        )

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = DECORATION_COLOR
            strokeWidth = width * 0.006f
            strokeCap = Paint.Cap.ROUND
        }
        drawCenteredLine(canvas, linePaint, width, height * 0.115f, 0.28f)
        drawCenteredLine(canvas, linePaint, width, height * 0.16f, 0.20f)
        drawCenteredLine(canvas, linePaint, width, height * 0.565f, 0.42f)
        drawCenteredLine(canvas, linePaint, width, height * 0.80f, 0.46f)
        drawCenteredLine(canvas, linePaint, width, height * 0.88f, 0.30f)

        drawTitle(canvas, title, width, height)
        drawBookIcon(context, canvas, width, height)
        return bitmap
    }

    fun writeTo(
        context: Context,
        file: File,
        title: String
    ) {
        file.parentFile?.mkdirs()
        file.outputStream().use {
            val format = if (file.extension.equals("png", ignoreCase = true)) {
                Bitmap.CompressFormat.PNG
            } else {
                Bitmap.CompressFormat.JPEG
            }
            render(context, title).compress(format, 100, it)
        }
    }

    fun cacheUri(context: Context, title: String): Uri {
        val file = File(
            File(context.cacheDir, "default_book_covers"),
            "v1_${title.hashCode().toUInt().toString(16)}.png"
        )
        if (!file.exists()) writeTo(context, file, title)
        return file.toUri()
    }

    private fun drawCenteredLine(
        canvas: Canvas,
        paint: Paint,
        width: Int,
        y: Float,
        widthFraction: Float
    ) {
        val halfLength = width * widthFraction / 2f
        canvas.drawLine(width / 2f - halfLength, y, width / 2f + halfLength, y, paint)
    }

    private fun drawTitle(canvas: Canvas, title: String, width: Int, height: Int) {
        val displayTitle = title.ifBlank { "未命名" }
        val textSize = when {
            displayTitle.length <= 6 -> width * 0.13f
            displayTitle.length <= 12 -> width * 0.105f
            else -> width * 0.085f
        }
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = CONTENT_COLOR
            this.textSize = textSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val layoutWidth = (width * 0.72f).toInt()
        val layout = StaticLayout.Builder
            .obtain(displayTitle, 0, displayTitle.length, textPaint, layoutWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setIncludePad(false)
            .setMaxLines(3)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()
        canvas.withTranslation(
            (width - layoutWidth) / 2f,
            height * 0.36f - layout.height / 2f
        ) {
            layout.draw(this)
        }
    }

    private fun drawBookIcon(context: Context, canvas: Canvas, width: Int, height: Int) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.menu_book_24px) ?: return
        val tintedDrawable = DrawableCompat.wrap(drawable).mutate()
        DrawableCompat.setTint(tintedDrawable, CONTENT_COLOR)
        val iconSize = (width * 0.17f).toInt()
        val left = (width - iconSize) / 2
        val top = (height * 0.64f).toInt()
        tintedDrawable.setBounds(left, top, left + iconSize, top + iconSize)
        tintedDrawable.draw(canvas)
    }
}
