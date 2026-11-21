package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.data.model.FilterPreset
import com.example.data.model.StoryTemplateFrame
import java.io.File
import java.io.FileOutputStream

enum class SocialCardFormat(
    val id: String,
    val label: String,
    val widthPx: Int,
    val heightPx: Int
) {
    POST_4_5("card", "Post (4:5)", 1080, 1350),
    SQUARE_1_1("square", "Kare (1:1)", 1080, 1080),
    STORY_9_16("story", "Hikaye (9:16)", 1080, 1920);

    companion object {
        fun fromId(id: String): SocialCardFormat {
            return values().find { it.id == id } ?: POST_4_5
        }
    }
}

enum class SharePlatform(val packageName: String?, val label: String) {
    INSTAGRAM("com.instagram.android", "Instagram"),
    WHATSAPP("com.whatsapp", "WhatsApp"),
    GENERAL(null, "Sosyal Medya")
}

data class SocialCardPalette(
    val id: String,
    val name: String,
    val startColor: Int,
    val endColor: Int,
    val accentColor: Int,
    val textColor: Int
)

object SocialImageShareHelper {

    val palettes = listOf(
        SocialCardPalette(
            id = "pastel_pembe",
            name = "Pastel Pembe",
            startColor = 0xFFFCE4EC.toInt(),
            endColor = 0xFFF8BBD0.toInt(),
            accentColor = 0xFFAD1457.toInt(),
            textColor = 0xFF3E1326.toInt()
        ),
        SocialCardPalette(
            id = "gece_yarisi_mavisi",
            name = "Gece Yarısı Mavisi",
            startColor = 0xFF1A237E.toInt(),
            endColor = 0xFF0D1333.toInt(),
            accentColor = 0xFF80DEEA.toInt(),
            textColor = 0xFFFFFFFF.toInt()
        ),
        SocialCardPalette(
            id = "romantik_kirmizi",
            name = "Romantik Kırmızı",
            startColor = 0xFF880E4F.toInt(),
            endColor = 0xFF4A072A.toInt(),
            accentColor = 0xFFFFD54F.toInt(),
            textColor = 0xFFFFFFFF.toInt()
        ),
        SocialCardPalette(
            id = "lavanta_ruyasi",
            name = "Lavanta Rüyası",
            startColor = 0xFF6A1B9A.toInt(),
            endColor = 0xFF380B5C.toInt(),
            accentColor = 0xFFF48FB1.toInt(),
            textColor = 0xFFFFFFFF.toInt()
        ),
        SocialCardPalette(
            id = "zumrut_altin",
            name = "Zümrüt & Altın",
            startColor = 0xFF004D40.toInt(),
            endColor = 0xFF00251A.toInt(),
            accentColor = 0xFFFFE082.toInt(),
            textColor = 0xFFFFFFFF.toInt()
        )
    )

    fun renderQuoteCardBitmap(
        quoteText: String,
        author: String,
        categoryLabel: String = "Aşka Dair",
        submitterOrDedication: String = "",
        sticker: String? = "💖",
        palette: SocialCardPalette = palettes[2],
        format: SocialCardFormat = SocialCardFormat.POST_4_5,
        filterPreset: FilterPreset = FilterPreset.NORMAL,
        context: Context? = null,
        backgroundImageResId: Int? = null,
        imageScrimColor: Int? = null,
        fontResId: Int? = null,
        isItalicQuote: Boolean = true,
        storyFrame: StoryTemplateFrame = StoryTemplateFrame.NONE
    ): Bitmap {
        val w = format.widthPx
        val h = format.heightPx
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background: Preset Image (center-cropped + scrim) OR Gradient
        var drewImage = false
        if (backgroundImageResId != null && context != null) {
            try {
                val srcBitmap = BitmapFactory.decodeResource(context.resources, backgroundImageResId)
                if (srcBitmap != null) {
                    val srcRatio = srcBitmap.width.toFloat() / srcBitmap.height.toFloat()
                    val dstRatio = w.toFloat() / h.toFloat()
                    val srcRect = if (srcRatio > dstRatio) {
                        val cropW = (srcBitmap.height * dstRatio).toInt()
                        val left = (srcBitmap.width - cropW) / 2
                        Rect(left, 0, left + cropW, srcBitmap.height)
                    } else {
                        val cropH = (srcBitmap.width / dstRatio).toInt()
                        val top = (srcBitmap.height - cropH) / 2
                        Rect(0, top, srcBitmap.width, top + cropH)
                    }
                    canvas.drawBitmap(srcBitmap, srcRect, Rect(0, 0, w, h), Paint(Paint.FILTER_BITMAP_FLAG))
                    imageScrimColor?.let { scrim ->
                        canvas.drawColor(scrim)
                    }
                    drewImage = true
                }
            } catch (_: Exception) {
                drewImage = false
            }
        }

        if (!drewImage) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, w.toFloat(), h.toFloat(),
                    palette.startColor, palette.endColor,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)
        }

        // Optional Filter Tint Overlay
        when (filterPreset) {
            FilterPreset.WARM_GLOW -> {
                canvas.drawColor(Color.argb(45, 255, 193, 7))
            }
            FilterPreset.ROSE_BLOOM -> {
                canvas.drawColor(Color.argb(42, 233, 30, 99))
            }
            FilterPreset.VINTAGE_SEPIA -> {
                canvas.drawColor(Color.argb(72, 112, 66, 20))
            }
            FilterPreset.NOIR -> {
                canvas.drawColor(Color.argb(105, 0, 0, 0))
            }
            FilterPreset.MIDNIGHT_VELVET -> {
                canvas.drawColor(Color.argb(75, 26, 35, 126))
            }
            FilterPreset.NORMAL -> Unit
        }

        // 2. Decorative Glass Inner Frame
        val isPolaroid = storyFrame == StoryTemplateFrame.POLAROID_MEMORY
        val margin = if (isPolaroid) 84f else 64f
        val bottomMargin = if (isPolaroid) 190f else margin
        val frameRect = RectF(margin, margin, w - margin, h - bottomMargin)

        if (isPolaroid) {
            val polaroidBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(242, 255, 252, 248)
                style = Paint.Style.FILL
            }
            // Outer white polaroid mat around edges
            canvas.drawRect(0f, 0f, w.toFloat(), margin, polaroidBorderPaint)
            canvas.drawRect(0f, 0f, margin, h.toFloat(), polaroidBorderPaint)
            canvas.drawRect(w - margin, 0f, w.toFloat(), h.toFloat(), polaroidBorderPaint)
            canvas.drawRect(0f, h - bottomMargin, w.toFloat(), h.toFloat(), polaroidBorderPaint)

            val polaroidCaptionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF3E1326.toInt()
                textSize = 36f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Kalpten Bir Anı • Aşka Dair ♥", w / 2f, h - 80f, polaroidCaptionPaint)
        } else {
            val frameBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(24, Color.red(palette.textColor), Color.green(palette.textColor), Color.blue(palette.textColor))
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(frameRect, 48f, 48f, frameBgPaint)

            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(95, Color.red(palette.accentColor), Color.green(palette.accentColor), Color.blue(palette.accentColor))
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawRoundRect(frameRect, 48f, 48f, borderPaint)
        }

        // Story Template Frame Overlays
        drawStoryTemplateFrameOverlay(
            canvas = canvas,
            w = w.toFloat(),
            h = h.toFloat(),
            margin = margin,
            palette = palette,
            storyFrame = storyFrame
        )

        // 3. Top Header Pill (Category & Sticker)
        val topY = margin + 88f
        val categoryText = categoryLabel.uppercase()
        val pillTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.accentColor
            textSize = 28f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.08f
        }
        val pillTextWidth = pillTextPaint.measureText(categoryText)
        val pillPaddingX = 34f
        val pillHeight = 56f
        val pillRect = RectF(
            margin + 54f,
            topY,
            margin + 54f + pillTextWidth + pillPaddingX * 2,
            topY + pillHeight
        )
        val pillBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(38, Color.red(palette.accentColor), Color.green(palette.accentColor), Color.blue(palette.accentColor))
        }
        canvas.drawRoundRect(pillRect, 28f, 28f, pillBgPaint)
        canvas.drawText(
            categoryText,
            pillRect.left + pillPaddingX,
            pillRect.centerY() + 10f,
            pillTextPaint
        )

        if (!sticker.isNullOrBlank()) {
            val stickerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 54f
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(sticker, w - margin - 54f, topY + 46f, stickerPaint)
        }

        // 4. Watermark Quotation Mark
        val quoteMarkPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(42, Color.red(palette.accentColor), Color.green(palette.accentColor), Color.blue(palette.accentColor))
            textSize = 210f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText("“", margin + 52f, topY + 235f, quoteMarkPaint)

        // 5. Main Quote Text (Custom Bundled Typeface & Centered)
        val contentWidth = (w - (margin + 88f) * 2).toInt().coerceAtLeast(300)
        val baseFontSize = when {
            quoteText.length > 220 -> 44f
            quoteText.length > 140 -> 52f
            quoteText.length > 75 -> 60f
            else -> 68f
        }
        val loadedTypeface = if (fontResId != null && context != null) {
            try {
                ResourcesCompat.getFont(context, fontResId)
            } catch (_: Exception) {
                null
            }
        } else null
        val finalTypeface = when {
            loadedTypeface != null && isItalicQuote -> Typeface.create(loadedTypeface, Typeface.ITALIC)
            loadedTypeface != null -> loadedTypeface
            isItalicQuote -> Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            else -> Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }
        val quotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.textColor
            textSize = baseFontSize
            typeface = finalTypeface
        }
        val formattedQuote = "\"${quoteText.trim()}\""
        val layout = StaticLayout.Builder
            .obtain(formattedQuote, 0, formattedQuote.length, quotePaint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(16f, 1.15f)
            .build()

        val quoteStartY = ((h - bottomMargin + margin) / 2f) - (layout.height / 2f) - 20f
        canvas.save()
        canvas.translate((w - contentWidth) / 2f, quoteStartY.coerceAtLeast(topY + 170f))
        layout.draw(canvas)
        canvas.restore()

        // 6. Divider + Author Signature
        val afterQuoteY = (quoteStartY.coerceAtLeast(topY + 170f) + layout.height + 54f)
            .coerceAtMost(h - bottomMargin - 210f)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.accentColor
            strokeWidth = 4f
            alpha = 190
        }
        canvas.drawLine(w / 2f - 70f, afterQuoteY, w / 2f + 70f, afterQuoteY, linePaint)

        val authorPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.accentColor
            textSize = 40f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("— ${author.ifBlank { "Anonim" }}", w / 2f, afterQuoteY + 62f, authorPaint)

        if (submitterOrDedication.isNotBlank()) {
            val subPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(195, Color.red(palette.textColor), Color.green(palette.textColor), Color.blue(palette.textColor))
                textSize = 28f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(submitterOrDedication, w / 2f, afterQuoteY + 108f, subPaint)
        }

        // 7. Footer Branding
        if (!isPolaroid) {
            val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(155, Color.red(palette.textColor), Color.green(palette.textColor), Color.blue(palette.textColor))
                textSize = 26f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.08f
            }
            canvas.drawText("❤️ AŞKA DAİR", w / 2f, h - bottomMargin - 48f, footerPaint)
        }

        return bitmap
    }

    private fun drawStoryTemplateFrameOverlay(
        canvas: Canvas,
        w: Float,
        h: Float,
        margin: Float,
        palette: SocialCardPalette,
        storyFrame: StoryTemplateFrame
    ) {
        when (storyFrame) {
            StoryTemplateFrame.NONE, StoryTemplateFrame.POLAROID_MEMORY -> Unit
            StoryTemplateFrame.INSTA_STORY_BORDER -> {
                val outerRect = RectF(28f, 28f, w - 28f, h - 28f)
                val gradientStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 10f
                    shader = LinearGradient(
                        0f, 0f, w, h,
                        intArrayOf(0xFFF58529.toInt(), 0xFFDD2A7B.toInt(), 0xFF8134AF.toInt()),
                        null,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRoundRect(outerRect, 60f, 60f, gradientStroke)

                val pillRect = RectF(w / 2f - 180f, 44f, w / 2f + 180f, 94f)
                val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFDD2A7B.toInt() }
                canvas.drawRoundRect(pillRect, 25f, 25f, pillPaint)
                val txtPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 24f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                    letterSpacing = 0.1f
                }
                canvas.drawText("📸 GÜNÜN HİKAYESİ", w / 2f, 78f, txtPaint)
            }
            StoryTemplateFrame.WHATSAPP_STATUS -> {
                val outerRect = RectF(30f, 30f, w - 30f, h - 30f)
                val wpStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 10f
                    color = 0xFF25D366.toInt()
                }
                canvas.drawRoundRect(outerRect, 54f, 54f, wpStroke)

                val badgeRect = RectF(w / 2f - 190f, 44f, w / 2f + 190f, 94f)
                val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF128C7E.toInt() }
                canvas.drawRoundRect(badgeRect, 25f, 25f, badgePaint)
                val txtPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 24f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("💚 DURUM PAYLAŞIMI", w / 2f, 78f, txtPaint)
            }
            StoryTemplateFrame.GOLD_ROYAL_FRAME -> {
                val goldColor = 0xFFFFD54F.toInt()
                val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 8f
                    color = goldColor
                }
                val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                    color = Color.argb(190, 255, 213, 79)
                }
                canvas.drawRoundRect(RectF(28f, 28f, w - 28f, h - 28f), 42f, 42f, outerPaint)
                canvas.drawRoundRect(RectF(44f, 44f, w - 44f, h - 44f), 32f, 32f, innerPaint)
                val crownPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = goldColor
                    textSize = 30f
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                    letterSpacing = 0.14f
                }
                canvas.drawText("✦ ÖZEL KOLEKSİYON ✦", w / 2f, 82f, crownPaint)
            }
            StoryTemplateFrame.EDITORIAL_MAGAZINE -> {
                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 5f
                    color = palette.accentColor
                }
                canvas.drawRect(36f, 36f, w - 36f, h - 36f, linePaint)
                canvas.drawLine(36f, 114f, w - 36f, 114f, linePaint)
                val mastheadPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = palette.accentColor
                    textSize = 32f
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                    letterSpacing = 0.18f
                }
                canvas.drawText("ROMANCE • EDITION", w / 2f, 88f, mastheadPaint)
            }
        }
    }

    fun shareBitmapToSocialMedia(
        context: Context,
        bitmap: Bitmap,
        shareCaption: String
    ): Result<Unit> {
        return shareBitmapToPlatform(
            context = context,
            bitmap = bitmap,
            shareCaption = shareCaption,
            platform = SharePlatform.GENERAL
        )
    }

    fun shareBitmapToPlatform(
        context: Context,
        bitmap: Bitmap,
        shareCaption: String,
        platform: SharePlatform
    ): Result<Unit> {
        return try {
            val cardsDir = File(context.cacheDir, "shared_cards").apply {
                if (!exists()) mkdirs()
            }
            cardsDir.listFiles()?.forEach { old ->
                if (System.currentTimeMillis() - old.lastModified() > 3600_000L) {
                    old.delete()
                }
            }
            val file = File(cardsDir, "aska_dair_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val baseIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, shareCaption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val pkg = platform.packageName
            if (!pkg.isNullOrBlank()) {
                val targeted = Intent(baseIntent).apply {
                    setPackage(pkg)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (targeted.resolveActivity(context.packageManager) != null) {
                    context.startActivity(targeted)
                    return Result.success(Unit)
                }
            }

            val chooser = Intent.createChooser(baseIntent, "${platform.label} ile Görsel Gönderi Paylaş")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
