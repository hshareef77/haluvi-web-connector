package mv.haluvi.webconnector

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
                        onShareReport = ::shareReport
                    )
                }
            }
        }
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

private enum class Screen { HOME, ADD_SERVICE, REPORT }

@Composable
private fun ConnectorApp(
    reports: List<ServiceReport>,
    onSaveReport: (ServiceReport) -> Unit,
    onShareReport: (ServiceReport) -> Unit
) {
    var screen by remember { mutableStateOf(Screen.HOME) }
    var serviceUrl by remember { mutableStateOf("") }
    var servicePurpose by remember { mutableStateOf("") }
    var currentReport by remember { mutableStateOf<ServiceReport?>(null) }

    when (screen) {
        Screen.HOME -> HomeScreen(
            reports = reports,
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
