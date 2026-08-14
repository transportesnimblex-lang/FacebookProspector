package com.nimblex.facebookprospector

import android.content.*
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import java.util.concurrent.TimeUnit
import kotlin.math.max

data class Search(val id: Long, val keyword: String, val filter: String)
data class Group(val id: Long, val name: String, val groupUrl: String, val pcUrl: String,
                 val searches: List<Search>, val lastVisited: Long? = null)
data class Reply(val id: Long, val title: String, val body: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App(this) }
    }
}

@Composable
fun App(activity: Activity) {
    var dark by remember { mutableStateOf(false) }
    var tab by remember { mutableIntStateOf(0) }
    var groups by remember {
        mutableStateOf(listOf(
            Group(1, "Emprendedores Monterrey", "https://www.facebook.com/groups/",
                "", listOf(Search(1, "busco proveedor", "Publicaciones más recientes"))),
            Group(2, "Negocios Nuevo León", "https://www.facebook.com/groups/",
                "", listOf(Search(2, "necesito", "Publicaciones más recientes")))
        ))
    }
    var replies by remember {
        mutableStateOf(listOf(
            Reply(1, "Primer contacto",
                "Hola {nombre}, vi tu publicación sobre {necesidad}. Con gusto te comparto información. ¿Te interesa?")
        ))
    }

    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Facebook Prospector") },
                    actions = {
                        IconButton({ dark = !dark }) {
                            Icon(if (dark) Icons.Default.LightMode else Icons.Default.DarkMode, null)
                        }
                    })
            },
            bottomBar = {
                NavigationBar {
                    listOf(
                        "Grupos" to Icons.Default.Groups,
                        "Respuestas" to Icons.Default.Chat,
                        "Actividad" to Icons.Default.History
                    ).forEachIndexed { i, (label, icon) ->
                        NavigationBarItem(tab == i, { tab = i }, { Icon(icon, null) }, label = { Text(label) })
                    }
                }
            },
            floatingActionButton = {
                if (tab == 0) FloatingActionButton({
                    val id = (groups.maxOfOrNull { it.id } ?: 0) + 1
                    groups = groups + Group(id, "Nuevo grupo $id", "", "", emptyList())
                }) { Icon(Icons.Default.Add, null) }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when (tab) {
                    0 -> Groups(groups) { groups = it }
                    1 -> Replies(replies) { replies = it }
                    2 -> ActivityScreen(groups)
                }
            }
        }
    }
}

@Composable
fun Groups(groups: List<Group>, update: (List<Group>) -> Unit) {
    val clipboard = LocalClipboardManager.current
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("Prospección", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text("${groups.count { it.lastVisited == null }} pendientes") })
            AssistChip(onClick = {}, label = { Text("${groups.size} grupos") })
        }
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(groups, key = { it.id }) { g ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Groups, null)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(g.name, style = MaterialTheme.typography.titleMedium)
                                Text(g.lastVisited?.let { "🟢 ${ago(it)}" } ?: "🔴 SIN EXPLORAR")
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        g.searches.forEach { s ->
                            Text("🔎 ${s.keyword}  ·  ${s.filter}")
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                val keyword = g.searches.firstOrNull()?.keyword.orEmpty()
                                if (keyword.isNotBlank()) clipboard.setText(AnnotatedString(keyword))
                                update(groups.map { if (it.id == g.id) it.copy(lastVisited = System.currentTimeMillis()) else it })
                                try { activityStart(activityContext(activity), g.groupUrl) } catch (_: Exception) {}
                            }) {
                                Icon(Icons.Default.OpenInNew, null)
                                Spacer(Modifier.width(5.dp))
                                Text("Abrir")
                            }
                            OutlinedButton(onClick = {
                                g.searches.firstOrNull()?.keyword?.let { clipboard.setText(AnnotatedString(it)) }
                            }) { Text("Copiar búsqueda") }
                        }
                    }
                }
            }
        }
    }
}

fun activityContext(a: Activity): Context = a
fun activityStart(ctx: Context, url: String) {
    val safe = if (url.isBlank()) "https://www.facebook.com/" else url
    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(safe)))
}

@Composable
fun Replies(replies: List<Reply>, update: (List<Reply>) -> Unit) {
    val clipboard = LocalClipboardManager.current
    var text by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("Respuestas rápidas", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        replies.forEach { r ->
            Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text(r.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(5.dp))
                    Text(r.body)
                    Spacer(Modifier.height(7.dp))
                    Button({ clipboard.setText(AnnotatedString(r.body)) }) {
                        Icon(Icons.Default.ContentCopy, null)
                        Spacer(Modifier.width(5.dp))
                        Text("Copiar")
                    }
                }
            }
        }
        OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth(),
            label = { Text("Nueva respuesta") }, minLines = 3)
        Spacer(Modifier.height(7.dp))
        Button(enabled = text.isNotBlank(), onClick = {
            val id = (replies.maxOfOrNull { it.id } ?: 0) + 1
            update(replies + Reply(id, "Respuesta $id", text))
            text = ""
        }) { Text("Guardar") }
    }
}

@Composable
fun ActivityScreen(groups: List<Group>) {
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("Actividad", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        val visited = groups.filter { it.lastVisited != null }.sortedByDescending { it.lastVisited }
        if (visited.isEmpty()) Text("Todavía no has explorado grupos.")
        else LazyColumn { items(visited) {
            ListItem(
                headlineContent = { Text(it.name) },
                supportingContent = { Text("Explorado ${ago(it.lastVisited!!)}") },
                leadingContent = { Icon(Icons.Default.CheckCircle, null) }
            )
        }}
    }
}

fun ago(t: Long): String {
    val m = TimeUnit.MILLISECONDS.toMinutes(max(0, System.currentTimeMillis() - t))
    return when {
        m < 1 -> "hace menos de 1 min"
        m < 60 -> "hace $m min"
        m < 1440 -> "hace ${m / 60} h"
        else -> "hace ${m / 1440} d"
    }
}
