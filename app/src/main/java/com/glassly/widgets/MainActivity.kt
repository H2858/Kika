package com.glassly.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

private data class WidgetEntry(
    @StringRes val name: Int,
    @StringRes val desc: Int,
    val provider: Class<*>
)

private val entries = listOf(
    WidgetEntry(R.string.w_clock, R.string.desc_clock, ClockWidget::class.java),
    WidgetEntry(R.string.w_date, R.string.desc_date, DateWidget::class.java),
    WidgetEntry(R.string.w_battery, R.string.desc_battery, BatteryWidget::class.java),
    WidgetEntry(R.string.w_quick, R.string.desc_quick, QuickWidget::class.java),
    WidgetEntry(R.string.w_quote, R.string.desc_quote, QuoteWidget::class.java)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { GlassTheme { MainScreen() } }
    }
}

private fun pin(context: Context, provider: Class<*>) {
    val manager = AppWidgetManager.getInstance(context)
    if (manager.isRequestPinAppWidgetSupported) {
        manager.requestPinAppWidget(ComponentName(context, provider), null, null)
    } else {
        Toast.makeText(context, R.string.pin_unsupported, Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun MainScreen() {
    val context = LocalContext.current
    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.main_title), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.main_sub), style = MaterialTheme.typography.bodyMedium)
                }
            }
            items(entries) { e ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(e.name), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(e.desc), style = MaterialTheme.typography.bodySmall)
                        }
                        Button(onClick = { pin(context, e.provider) }) {
                            Text(stringResource(R.string.add_widget))
                        }
                    }
                }
            }
            item {
                Text(stringResource(R.string.main_tip), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
