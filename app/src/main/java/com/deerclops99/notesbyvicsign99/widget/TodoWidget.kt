package com.deerclops99.notesbyvicsign99.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.deerclops99.notesbyvicsign99.MainActivity
import com.deerclops99.notesbyvicsign99.R
import com.deerclops99.notesbyvicsign99.data.AppDatabase
import com.deerclops99.notesbyvicsign99.data.TodoItem
import com.deerclops99.notesbyvicsign99.settings.AppLanguage
import com.deerclops99.notesbyvicsign99.settings.AppTheme
import com.deerclops99.notesbyvicsign99.settings.AppThemes

private data class WidgetTexts(
    val title: String,
    val empty: String,
    val permanent: String,
    val daily: String
)

private fun buildTexts(context: Context, langTag: String): WidgetTexts {
    val localized = AppLanguage.localized(context, langTag)
    return WidgetTexts(
        title = localized.getString(R.string.widget_title),
        empty = localized.getString(R.string.widget_empty),
        permanent = localized.getString(R.string.todo_section_permanent),
        daily = localized.getString(R.string.todo_section_daily)
    )
}

class TodoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = AppDatabase.get(context).todoDao()
        val initialTodos = dao.getAll()
        val initialLang = AppLanguage.savedTag(context)
        val initialTheme = AppThemes.selected(context)

        provideContent {
            // Tareas, idioma y tema se observan DENTRO del widget: se
            // redibuja solo cuando cambian, aunque la sesión siga abierta
            val todos by remember { dao.observeAll() }
                .collectAsState(initial = initialTodos)
            val langTag by remember { AppLanguage.savedTagFlow(context) }
                .collectAsState(initial = initialLang)
            val appTheme by remember { AppThemes.selectedFlow(context) }
                .collectAsState(initial = initialTheme)
            val texts = remember(langTag) { buildTexts(context, langTag) }

            TodoWidgetContent(todos, texts, appTheme)
        }
    }
}

@Composable
private fun TodoWidgetContent(
    todos: List<TodoItem>,
    texts: WidgetTexts,
    theme: AppTheme
) {
    val context = LocalContext.current
    val scheme = theme.colorScheme
    val permanent = todos.filter { it.permanent }
    val daily = todos.filter { !it.permanent }

    // Abre la app directamente en la pestaña Tareas
    val openTasks = actionStartActivity(
        Intent(context, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_TAB, MainActivity.TAB_TASKS)
    )

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(theme.widgetBody))
            .appWidgetBackground()
            .cornerRadius(16.dp)
            .clickable(openTasks)
    ) {
        // ---------- Encabezado: color 1 con letras del color 3 ----------
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ColorProvider(scheme.primary))
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .clickable(openTasks),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = texts.title,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ColorProvider(scheme.onPrimary)
                )
            )
        }

        // ---------- Cuerpo ----------
        if (todos.isEmpty()) {
            Text(
                text = texts.empty,
                style = TextStyle(
                    color = ColorProvider(scheme.onSurfaceVariant)
                ),
                modifier = GlanceModifier.padding(12.dp)
            )
        } else {
            // La lista abajo y encima una capa transparente que recibe
            // todos los toques y abre la app en Tareas
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
            ) {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    if (permanent.isNotEmpty()) {
                        item { SectionLabel(texts.permanent, theme, openTasks) }
                        items(permanent, itemId = { it.id }) {
                            TodoRow(it, theme, openTasks)
                        }
                    }
                    if (daily.isNotEmpty()) {
                        item { SectionLabel(texts.daily, theme, openTasks) }
                        items(daily, itemId = { it.id }) {
                            TodoRow(it, theme, openTasks)
                        }
                    }
                }
                Spacer(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .clickable(openTasks)
                )
            }
        }
    }
}

// Etiqueta "Permanentes" / "Del día": rellena con el color 1
@Composable
private fun SectionLabel(text: String, theme: AppTheme, onClick: Action) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 2.dp)
            .clickable(onClick)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = ColorProvider(theme.pillText)
            ),
            modifier = GlanceModifier
                .background(ColorProvider(theme.colorScheme.primary))
                .cornerRadius(12.dp)
                .padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun TodoRow(item: TodoItem, theme: AppTheme, onClick: Action) {
    val scheme = theme.colorScheme
    val textColor = ColorProvider(
        if (item.done) scheme.onSurfaceVariant else scheme.onSurface
    )

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clickable(onClick)
    ) {
        Text(
            text = "•",
            style = TextStyle(fontSize = 14.sp, color = textColor)
        )
        Spacer(GlanceModifier.width(8.dp))
        Text(
            text = item.text,
            style = TextStyle(
                fontSize = 14.sp,
                color = textColor,
                textDecoration = if (item.done) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                }
            ),
            modifier = GlanceModifier.defaultWeight()
        )
    }
}
