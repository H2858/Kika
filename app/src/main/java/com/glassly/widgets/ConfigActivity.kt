package com.glassly.widgets

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

enum class WidgetKind { CLOCK, DATE, BATTERY, QUICK, QUOTE }

class ConfigActivity : ComponentActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setResult(RESULT_CANCELED)

        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val manager = AppWidgetManager.getInstance(this)
        val provider = manager.getAppWidgetInfo(widgetId)?.provider
        val cls = provider?.className.orEmpty()
        val kind = when {
            cls.endsWith("DateWidget") -> WidgetKind.DATE
            cls.endsWith("BatteryWidget") -> WidgetKind.BATTERY
            cls.endsWith("QuickWidget") -> WidgetKind.QUICK
            cls.endsWith("QuoteWidget") -> WidgetKind.QUOTE
            else -> WidgetKind.CLOCK
        }

        val initial = StyleStore.load(this, widgetId)
        setContent {
            GlassTheme {
                ConfigScreen(kind, initial) { style -> save(style, provider) }
            }
        }
    }

    private fun save(style: GlassStyle, provider: android.content.ComponentName?) {
        StyleStore.save(this, widgetId, style)
        if (provider != null) {
            sendBroadcast(
                Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    .setComponent(provider)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
            )
        }
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        finish()
    }
}

private val glassColors = listOf(
    0xFFFFFFFF.toInt(), 0xFF9FD8FF.toInt(), 0xFFC3A6FF.toInt(), 0xFFFFA6C9.toInt(),
    0xFFA6FFD9.toInt(), 0xFFFFD79F.toInt(), 0xFF1B1B2F.toInt(), 0xFF000000.toInt()
)

@Composable
private fun ConfigScreen(kind: WidgetKind, initial: GlassStyle, onSave: (GlassStyle) -> Unit) {
    val context = LocalContext.current
    var tint by remember { mutableIntStateOf(initial.tint) }
    var opacity by remember { mutableFloatStateOf(initial.opacity) }
    var radius by remember { mutableFloatStateOf(initial.radiusDp) }
    var border by remember { mutableStateOf(initial.border) }
    var textColor by remember { mutableIntStateOf(initial.textColor) }
    var hourMode by remember { mutableIntStateOf(initial.hourMode) }
    var text by remember { mutableStateOf(initial.text) }

    val style = GlassStyle(tint, opacity, radius, border, textColor, hourMode, text)
    val sampleQuote = stringArrayResource(R.array.quotes).first()

    Scaffold { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(stringResource(R.string.customize), style = MaterialTheme.typography.headlineSmall)

            // Live preview over a colorful backdrop so the glass effect is visible
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF5B3DF5), Color(0xFFFF6FB1), Color(0xFFFFB46B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val bitmap = remember(style) { GlassRenderer.render(context, style, 260, 120).asImageBitmap() }
                Box(modifier = Modifier.size(260.dp, 120.dp), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                    val sample = when (kind) {
                        WidgetKind.CLOCK -> if (hourMode == 1) "3:45" else "15:45"
                        WidgetKind.DATE -> "15"
                        WidgetKind.BATTERY -> "78%"
                        WidgetKind.QUICK -> "📶  🔷  ☀️  🔔  ⚙️"
                        WidgetKind.QUOTE -> text.ifBlank { sampleQuote }
                    }
                    Text(
                        text = sample,
                        color = Color(textColor),
                        fontSize = when (kind) {
                            WidgetKind.CLOCK -> 44.sp
                            WidgetKind.DATE -> 52.sp
                            WidgetKind.BATTERY -> 30.sp
                            WidgetKind.QUICK -> 20.sp
                            WidgetKind.QUOTE -> 15.sp
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Text(stringResource(R.string.glass_color), style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                glassColors.forEach { c -> Swatch(c, tint == c) { tint = c } }
            }

            Text(
                stringResource(R.string.opacity) + "  " + (opacity * 100).roundToInt() + "%",
                style = MaterialTheme.typography.titleSmall
            )
            Slider(value = opacity, onValueChange = { opacity = it }, valueRange = 0.05f..0.8f)

            Text(
                stringResource(R.string.corner_radius) + "  " + radius.roundToInt() + "dp",
                style = MaterialTheme.typography.titleSmall
            )
            Slider(value = radius, onValueChange = { radius = it }, valueRange = 0f..48f)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.border),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = border, onCheckedChange = { border = it })
            }

            Text(stringResource(R.string.text_color), style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = textColor == android.graphics.Color.WHITE,
                    onClick = { textColor = android.graphics.Color.WHITE },
                    label = { Text(stringResource(R.string.white)) }
                )
                FilterChip(
                    selected = textColor == android.graphics.Color.BLACK,
                    onClick = { textColor = android.graphics.Color.BLACK },
                    label = { Text(stringResource(R.string.black)) }
                )
            }

            if (kind == WidgetKind.CLOCK) {
                Text(stringResource(R.string.hour_format), style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = hourMode == 0,
                        onClick = { hourMode = 0 },
                        label = { Text(stringResource(R.string.fmt_system)) }
                    )
                    FilterChip(
                        selected = hourMode == 1,
                        onClick = { hourMode = 1 },
                        label = { Text(stringResource(R.string.fmt_12)) }
                    )
                    FilterChip(
                        selected = hourMode == 2,
                        onClick = { hourMode = 2 },
                        label = { Text(stringResource(R.string.fmt_24)) }
                    )
                }
            }

            if (kind == WidgetKind.QUOTE) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.custom_text)) },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = { onSave(style) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Composable
private fun Swatch(color: Int, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(color))
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}
