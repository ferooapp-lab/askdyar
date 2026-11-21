package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MoodInsightDay
import com.example.ui.LoveQuotesViewModel
import kotlin.math.roundToInt

@Composable
fun MoodInsights30DayCard(
    viewModel: LoveQuotesViewModel,
    onNavigateToNewNote: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.moodInsights30Day.collectAsState()
    var selectedIndex by remember { mutableStateOf<Int?>(summary.days.lastIndex.takeIf { it >= 0 }) }

    val cardShape = RoundedCornerShape(24.dp)

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(
                elevation = 10.dp,
                shape = cardShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
                    )
                ),
                cardShape
            )
            .testTag("mood_insights_30d_card"),
        shape = cardShape,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Duygu İçgörüleri (Son 30 Gün)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Günlük aşk notlarına göre duygusal eğilim ve romantik tutarlılık",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 3 Romantic Consistency KPI Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConsistencyKpiTile(
                    label = "Tutarlılık",
                    value = "%${summary.consistencyPercent}",
                    detail = "${summary.activeDaysCount}/30 gün",
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                ConsistencyKpiTile(
                    label = "Aktif Seri",
                    value = "${summary.currentStreakDays} Gün",
                    detail = "En uzun: ${summary.longestStreakDays}g",
                    accentColor = Color(0xFFE91E63),
                    modifier = Modifier.weight(1f)
                )
                ConsistencyKpiTile(
                    label = "Ortalama Mod",
                    value = "%${summary.averageMoodScore}",
                    detail = "${summary.totalNotesLast30Days} aşk notu",
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Interactive 30-Day Area + Smooth Bezier Line Chart
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "30 Günlük Duygu Eğrisi",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dokun & Kaydır",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                ThirtyDayAreaTrendChart(
                    days = summary.days,
                    selectedIndex = selectedIndex,
                    onSelectDay = { idx -> selectedIndex = idx }
                )

                // Axis Date Labels (30d ago, 15d ago, Bugün)
                if (summary.days.size >= 30) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = summary.days.first().fullDateLabel.take(6),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = summary.days[14].fullDateLabel.take(6),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Bugün",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Selected Day Tooltip Inspector
            AnimatedVisibility(visible = selectedIndex != null && selectedIndex!! in summary.days.indices) {
                selectedIndex?.takeIf { it in summary.days.indices }?.let { idx ->
                    val day = summary.days[idx]
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mood_30d_selected_day_tooltip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${day.fullDateLabel} • ${day.moodLabel}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (day.noteCount > 0) "${day.noteCount} aşk notu kaydedildi" else if (day.hasActivity) "Günlük duygu check-in kaydı var" else "Bu gün kayıtlı not bulunmuyor",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${day.emoji} %${day.moodScore}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 30-Day Consistency Heatmap Grid (10 columns x 3 rows)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Romantik Tutarlılık Takvimi (30 Gün)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                ConsistencyHeatmapGrid(
                    days = summary.days,
                    selectedIndex = selectedIndex,
                    onSelectDay = { idx -> selectedIndex = idx }
                )
            }

            // 30-Day Emotion Breakdown Bars
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "30 Günlük Baskın Duygu: ${summary.dominantEmotion}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                summary.breakdowns.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("${item.emoji} ${item.emotionName}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(128.dp))
                        LinearProgressIndicator(
                            progress = { item.percentage / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            drawStopIndicator = {}
                        )
                        Text(
                            text = "%${item.percentage}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Insight Summary + Note CTA
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.48f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Romantik Tutarlılık Analizi",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Text(
                        text = summary.consistencyInsight,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    if (onNavigateToNewNote != null) {
                        Button(
                            onClick = onNavigateToNewNote,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bugünün Aşk Notunu Yaz", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsistencyKpiTile(
    label: String,
    value: String,
    detail: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.22f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = detail,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThirtyDayAreaTrendChart(
    days: List<MoodInsightDay>,
    selectedIndex: Int?,
    onSelectDay: (Int) -> Unit
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(days) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .pointerInput(days.size) {
                detectTapGestures { offset ->
                    if (days.isNotEmpty() && size.width > 0) {
                        val idx = ((offset.x / size.width.toFloat()) * (days.size - 1))
                            .roundToInt()
                            .coerceIn(0, days.lastIndex)
                        onSelectDay(idx)
                    }
                }
            }
            .pointerInput(days.size) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    if (days.isNotEmpty() && size.width > 0) {
                        val idx = ((change.position.x / size.width.toFloat()) * (days.size - 1))
                            .roundToInt()
                            .coerceIn(0, days.lastIndex)
                        onSelectDay(idx)
                    }
                }
            }
            .testTag("mood_30d_area_chart")
    ) {
        if (days.size < 2) return@Canvas

        val padTop = 14.dp.toPx()
        val padBottom = 12.dp.toPx()
        val padH = 8.dp.toPx()
        val chartW = (size.width - padH * 2).coerceAtLeast(1f)
        val chartH = (size.height - padTop - padBottom).coerceAtLeast(1f)

        // Horizontal dashed reference lines (100%, 80%, 60%)
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        listOf(0f, 0.5f, 1f).forEach { ratio ->
            val y = padTop + chartH * ratio
            drawLine(
                color = gridColor,
                start = Offset(padH, y),
                end = Offset(size.width - padH, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashEffect
            )
        }

        val points = days.mapIndexed { i, day ->
            val x = padH + (i.toFloat() / (days.size - 1).toFloat()) * chartW
            val norm = ((day.moodScore.coerceIn(50, 100) - 50) / 50f) * animProgress.value
            val y = padTop + chartH * (1f - norm)
            Offset(x, y)
        }

        // Smooth cubic Bezier path
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.lastIndex) {
                val p1 = points[i]
                val p2 = points[i + 1]
                val midX = (p1.x + p2.x) / 2f
                cubicTo(midX, p1.y, midX, p2.y, p2.x, p2.y)
            }
        }

        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(points.last().x, padTop + chartH)
            lineTo(points.first().x, padTop + chartH)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.34f * animProgress.value),
                    primaryColor.copy(alpha = 0.03f)
                ),
                startY = padTop,
                endY = padTop + chartH
            )
        )

        drawPath(
            path = linePath,
            color = primaryColor,
            style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Activity nodes
        days.forEachIndexed { idx, day ->
            if (day.hasActivity) {
                val pt = points[idx]
                drawCircle(
                    color = secondaryColor,
                    radius = 3.8.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = 1.6.dp.toPx(),
                    center = pt
                )
            }
        }

        // Selected crosshair & highlight dot
        selectedIndex?.takeIf { it in points.indices }?.let { sel ->
            val pt = points[sel]
            drawLine(
                color = primaryColor.copy(alpha = 0.6f),
                start = Offset(pt.x, padTop),
                end = Offset(pt.x, padTop + chartH),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = dashEffect
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.22f),
                radius = 10.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = primaryColor,
                radius = 5.5.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = Color.White,
                radius = 2.5.dp.toPx(),
                center = pt
            )
        }
    }
}

@Composable
private fun ConsistencyHeatmapGrid(
    days: List<MoodInsightDay>,
    selectedIndex: Int?,
    onSelectDay: (Int) -> Unit
) {
    val rows = days.chunked(10)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEachIndexed { rowIdx, rowDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowDays.forEachIndexed { colIdx, day ->
                    val globalIdx = rowIdx * 10 + colIdx
                    val isSelected = selectedIndex == globalIdx
                    val tileColor = when {
                        day.hasActivity && day.moodScore >= 90 -> MaterialTheme.colorScheme.primary
                        day.hasActivity && day.moodScore >= 80 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
                        day.hasActivity -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(26.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(tileColor)
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                shape = RoundedCornerShape(7.dp)
                            )
                            .clickable { onSelectDay(globalIdx) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (globalIdx == days.lastIndex) "•" else day.shortLabel.take(2),
                            fontSize = 10.sp,
                            fontWeight = if (day.hasActivity || isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (day.hasActivity) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
