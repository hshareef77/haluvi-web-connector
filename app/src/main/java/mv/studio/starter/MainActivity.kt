package mv.haluvi.webconnector

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import java.net.HttpURLConnection
import java.net.URL
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

private data class DemoPost(val id: Int, val title: String, val body: String)

private data class ServiceReport(
    val id: Long,
    val url: String,
    val purpose: String,
    val body: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val reports = remember {
                        mutableStateListOf<ServiceReport>().apply {
                            addAll(loadReports(this@MainActivity))
                        }
                    }
                    ConnectorApp(
                        reports = reports,
                        onSaveReport = { report ->
                            reports.add(0, report)
                            saveReports(this@MainActivity, reports)
                        },
                        onShareReport = ::shareReport,
                        onLoadDemo = ::loadDemoPosts
                    )
                }
            }
        }
    }

    private fun loadDemoPosts(callback: (Result<List<DemoPost>>) -> Unit) {
        Thread {
            val result = runCatching {
                val connection = (URL("https://jsonplaceholder.typicode.com/posts").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                    setRequestProperty("Accept", "application/json")
                }
                try {
                    val status = connection.responseCode
                    check(status in 200..299) { "The sample service returned HTTP $status." }
                    val payload = connection.inputStream.bufferedReader().use { it.readText() }
                    val items = JSONArray(payload)
                    (0 until minOf(items.length(), 10)).map { index ->
                        val item = items.getJSONObject(index)
                        DemoPost(item.getInt("id"), item.getString("title"), item.getString("body"))
                    }
                } finally {
                    connection.disconnect()
                }
            }
            Handler(Looper.getMainLooper()).post { callback(result) }
        }.start()
    }

    private fun shareReport(report: ServiceReport) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Haluvi Web Connector report: ${report.url}")
            putExtra(Intent.EXTRA_TEXT, report.body)
        }
        startActivity(Intent.createChooser(shareIntent, "Share technical report"))
    }
}

private enum class Screen { HOME, ADD_SERVICE, REPORT, DEMO }

@Composable
private fun ConnectorApp(
    reports: List<ServiceReport>,
    onSaveReport: (ServiceReport) -> Unit,
    onShareReport: (ServiceReport) -> Unit,
    onLoadDemo: ((Result<List<DemoPost>>) -> Unit) -> Unit
) {
    var demoPosts by remember { mutableStateOf<List<DemoPost>>(emptyList()) }
    var demoLoading by remember { mutableStateOf(false) }
    var demoError by remember { mutableStateOf("") }
    var screen by remember { mutableStateOf(Screen.HOME) }
    var serviceUrl by remember { mutableStateOf("") }
    var servicePurpose by remember { mutableStateOf("") }
    var currentReport by remember { mutableStateOf<ServiceReport?>(null) }

    fun refreshDemo() {
        demoLoading = true
        demoError = ""
        onLoadDemo { result ->
            result.onSuccess { demoPosts = it }.onFailure { demoError = it.message ?: "Could not load sample posts." }
            demoLoading = false
        }
    }

    when (screen) {
        Screen.HOME -> HomeScreen(
            reports = reports,
            onTryDemo = {
                screen = Screen.DEMO
                if (demoPosts.isEmpty()) refreshDemo()
            },
            onAdd = {
                serviceUrl = ""
                servicePurpose = ""
                screen = Screen.ADD_SERVICE
            },
            onOpenReport = { report ->
                currentReport = report
                screen = Screen.REPORT
            }
        )
        Screen.ADD_SERVICE -> AddServiceScreen(
            url = serviceUrl,
            onUrlChange = { serviceUrl = it },
            purpose = servicePurpose,
            onPurposeChange = { servicePurpose = it },
            onBack = { screen = Screen.HOME },
            onCreateReport = {
                val report = ServiceReport(
                    id = System.currentTimeMillis(),
                    url = serviceUrl.trim(),
                    purpose = servicePurpose.trim().ifBlank { "Not specified" },
                    body = buildReport(serviceUrl.trim(), servicePurpose.trim())
                )
                onSaveReport(report)
                currentReport = report
                screen = Screen.REPORT
            }
        )
        Screen.DEMO -> DemoScreen(
            posts = demoPosts,
            loading = demoLoading,
            error = demoError,
            onRefresh = ::refreshDemo,
            onBack = { screen = Screen.HOME }
        )
        Screen.REPORT -> currentReport?.let { report ->
            ReportScreen(
                report = report,
                onBack = { screen = Screen.HOME },
                onShare = { onShareReport(report) }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    reports: List<ServiceReport>,
    onTryDemo: () -> Unit,
    onAdd: () -> Unit,
    onOpenReport: (ServiceReport) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Haluvi Web Connector",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() }
        )
        Text("Prepare accessible Android integrations for web services that provide a supported way to connect.")
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("Analyze a service") }
        OutlinedButton(onClick = onTryDemo, modifier = Modifier.fillMaxWidth()) { Text("Try the public API demo") }
        Text("Your reports are saved on this device. They do not contain passwords or account sessions.")
        Text("Saved reports", style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        if (reports.isEmpty()) {
            Text("No reports saved yet.")
        } else {
            reports.forEach { report ->
                OutlinedButton(onClick = { onOpenReport(report) }, modifier = Modifier.fillMaxWidth()) {
                    Text("${report.url}. Requested task: ${report.purpose}")
                }
            }
        }
    }
}


@Composable
private fun DemoScreen(
    posts: List<DemoPost>,
    loading: Boolean,
    error: String,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Public API demo", style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() })
        Text("Sample posts are fetched over HTTPS and shown with native Android text and buttons. No sign-in is needed.")
        Text("This is test content. It does not access a real account or change any service data.")
        Button(onClick = onRefresh, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
            Text(if (loading) "Loading sample posts" else "Refresh sample posts")
        }
        if (loading) {
            CircularProgressIndicator()
            Text("Loading sample posts. Please wait.")
        }
        if (error.isNotBlank()) Text("Could not load sample posts. $error")
        posts.forEach { post ->
            Text("Post ${post.id}: ${post.title}", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() })
            Text(post.body)
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to services") }
    }
}

@Composable
private fun AddServiceScreen(
    url: String,
    onUrlChange: (String) -> Unit,
    purpose: String,
    onPurposeChange: (String) -> Unit,
    onBack: () -> Unit,
    onCreateReport: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Service details", style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() })
        Text("Enter the service's public home page. Never enter a password or secret here.")
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Website address") },
            singleLine = true
        )
        OutlinedTextField(
            value = purpose,
            onValueChange = onPurposeChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("What do you want to do in the app?") },
            minLines = 3
        )
        Button(onClick = onCreateReport, enabled = url.startsWith("https://"), modifier = Modifier.fillMaxWidth()) {
            Text("Create technical report")
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}

@Composable
private fun ReportScreen(report: ServiceReport, onBack: () -> Unit, onShare: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Initial feasibility report", style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() })
        Text(report.body)
        Button(onClick = onShare, modifier = Modifier.fillMaxWidth()) { Text("Share report text") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to saved reports") }
    }
}

private fun buildReport(url: String, purpose: String): String {
    val host = url.removePrefix("https://").removePrefix("http://").substringBefore('/').trim()
    val task = purpose.ifBlank { "Not specified" }
    return """
Service: $host
Requested task: $task

Initial status: More inspection is needed. This report is based only on the address and task you entered; it has not inspected the live website.

Recommended connection order
1. Check whether the service publishes an official API or Android SDK for the requested task.
2. Check whether it supports OAuth 2.0 / OpenID Connect for native apps, including an approved redirect method.
3. If it has no API, request an official integration or export option from the service owner. Do not automate a private website login without permission.
4. Map each supported action to labeled native controls, then verify focus order and announcements with TalkBack and Jieshuo+.

Session and security notes
- OAuth access and refresh tokens can be stored locally using encryption backed by Android Keystore, if the service permits this client.
- Website cookies are different from API tokens. They are often tied to browser rules, CSRF checks, device signals, or short expiry; they cannot be promised as reusable app sessions.
- Never enter account passwords or API secrets into this report flow. Secrets belong in a protected sign-in flow and must not be included in exported reports.

Possible blocker
A website that provides only interactive HTML and no supported API or SDK cannot be reliably converted into a standalone native app automatically. Each service may need a custom adapter, and some services do not allow third-party clients.

Next information needed for a full adapter
- Exact pages and actions you need.
- Whether sign-in is required and the service's official API / OAuth documentation.
- Any forms, buttons, lists, file uploads, payment steps, or confirmation pages involved.
- Permission from the service owner if the integration is not publicly documented.
""".trimIndent()
}

private fun saveReports(context: Context, reports: List<ServiceReport>) {
    val array = JSONArray()
    reports.forEach { report ->
        array.put(JSONObject().apply {
            put("id", report.id)
            put("url", report.url)
            put("purpose", report.purpose)
            put("body", report.body)
        })
    }
    context.getSharedPreferences("saved_reports", Context.MODE_PRIVATE)
        .edit().putString("reports", array.toString()).apply()
}

private fun loadReports(context: Context): List<ServiceReport> {
    val value = context.getSharedPreferences("saved_reports", Context.MODE_PRIVATE)
        .getString("reports", null) ?: return emptyList()
    return runCatching {
        val array = JSONArray(value)
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            ServiceReport(
                id = item.getLong("id"),
                url = item.getString("url"),
                purpose = item.getString("purpose"),
                body = item.getString("body")
            )
        }
    }.getOrDefault(emptyList())
}
