package com.deerclops99.notesbyvicsign99

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import com.deerclops99.notesbyvicsign99.settings.AppLanguage
import com.deerclops99.notesbyvicsign99.ui.AppRoot
import com.deerclops99.notesbyvicsign99.ui.theme.NotesTheme

class MainActivity : AppCompatActivity() {

    // Pestaña que pidió el widget (null si se abrió de forma normal)
    private val requestedTab = mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLanguage.syncFromSystem(this)
        enableEdgeToEdge()

        // Solo en un arranque nuevo: si la pantalla se recrea (por cambiar
        // idioma o tema), no volvemos a saltar a Tareas
        if (savedInstanceState == null) {
            requestedTab.value = tabFrom(intent)
        }

        setContent {
            NotesTheme {
                AppRoot(
                    requestedTab = requestedTab.value,
                    onTabRequestHandled = { requestedTab.value = null }
                )
            }
        }
    }

    // La app ya estaba abierta y el usuario tocó el widget
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedTab.value = tabFrom(intent)
    }

    private fun tabFrom(intent: Intent?): Int? =
        intent?.getIntExtra(EXTRA_TAB, -1)?.takeIf { it >= 0 }

    companion object {
        const val EXTRA_TAB = "open_tab"
        const val TAB_TASKS = 1
    }
}