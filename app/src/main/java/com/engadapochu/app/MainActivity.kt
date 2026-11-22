package com.engadapochu.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

private data class Tx(val id: Long, val kind: String, val person: String, val amount: Double, val at: Long)

class MainActivity : ComponentActivity() {
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) speak("Microphone permission is required")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) { if (it == TextToSpeech.SUCCESS) tts?.language = Locale("en", "IN") }
        setContent { EngadaApp() }
    }
    private fun speak(text: String) { tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "engada") }
    private fun listen(onText: (String) -> Unit) {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { permission.launch(Manifest.permission.RECORD_AUDIO); return }
        recognizer?.destroy(); recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(b: Bundle?) { onText(b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()); recognizer?.destroy() }
            override fun onError(error: Int) { speak("I could not understand. Please try again."); recognizer?.destroy() }
            override fun onReadyForSpeech(p: Bundle?) {}; override fun onBeginningOfSpeech() {}; override fun onRmsChanged(r: Float) {}; override fun onBufferReceived(b: ByteArray?) {}; override fun onEndOfSpeech() {}; override fun onPartialResults(b: Bundle?) {}; override fun onEvent(t: Int, b: Bundle?) {}
        })
        recognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("en", "IN")); putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false) })
    }
    @Composable private fun EngadaApp() {
        val context = LocalContext.current
        var text by remember { mutableStateOf("") }; var pending by remember { mutableStateOf<Tx?>(null) }
        var txs by remember { mutableStateOf(listOf<Tx>()) }; var status by remember { mutableStateOf("Tap the mic or type a command") }
        val sent = txs.filter { it.kind == "SENT" }.sumOf { it.amount }; val received = txs.filter { it.kind == "RECEIVED" }.sumOf { it.amount }
        MaterialTheme(colorScheme = darkColorScheme()) { Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF253B73), Color(0xFF090C17)))).padding(18.dp)) {
            Text("Engada Pochu", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Tamil + English • stored only on this phone", color = Color(0xFFB5C2DB))
            Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Summary("Sent", sent, Color(0xFFFF796D), Modifier.weight(1f)); Summary("Received", received, Color(0xFF00DFF5), Modifier.weight(1f)); Summary("Balance", received - sent, Color.White, Modifier.weight(1f)) }
            Button(onClick = { status = "Listening…"; speak("Listening"); listen { text = it; status = "Review before saving" } }, shape = CircleShape, modifier = Modifier.size(138.dp).align(Alignment.CenterHorizontally).semantics { contentDescription = "Start voice input" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00DFF5))) { Text("🎙", style = MaterialTheme.typography.displaySmall) }
            Text(status, Modifier.align(Alignment.CenterHorizontally).padding(8.dp), color = Color.White)
            OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth(), label = { Text("Voice result or manual command") })
            Button({ val parsed = parse(text); if (parsed != null) { pending = parsed; speak("Please confirm ${parsed.kind.lowercase()} ${parsed.amount} to ${parsed.person}") } else { status = "Use: sent 500 to Ravi"; speak("Please say an amount and person") } }, Modifier.fillMaxWidth()) { Text("Review") }
            pending?.let { p -> Card(Modifier.fillMaxWidth().padding(top = 10.dp)) { Column(Modifier.padding(14.dp)) { Text("Confirm: ${p.kind} ₹${p.amount} ${if (p.kind == "SENT") "to" else "from"} ${p.person}"); Row { Button({ txs = listOf(p) + txs; pending = null; text = ""; status = "Saved"; speak("Saved") }) { Text("Save") }; Spacer(Modifier.width(8.dp)); OutlinedButton({ pending = null; status = "Cancelled" }) { Text("Cancel") } } } } }
            LazyColumn(Modifier.fillMaxWidth().padding(top = 12.dp)) { items(txs) { t -> Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text("${t.kind}  ₹${t.amount}  ${if (t.kind == "SENT") "to" else "from"} ${t.person}\n${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(t.at))}", Modifier.padding(12.dp), color = if (t.kind == "SENT") Color(0xFFFF796D) else Color(0xFF00DFF5)) } } }
        } }
    }
    @Composable private fun Summary(label: String, value: Double, color: Color, modifier: Modifier = Modifier) { Card(modifier) { Column(Modifier.padding(10.dp)) { Text(label, color = Color.Gray); Text("₹${"%.2f".format(value)}", color = color, fontWeight = FontWeight.Bold) } } }
    private fun parse(raw: String): Tx? { val s = raw.trim(); val m = Regex("(?i)(sent|paid|received)\\s+([0-9]+(?:\\.[0-9]+)?)\\s+(?:to|from)\\s+(.+)").find(s) ?: return null; val kind = if (m.groupValues[1].lowercase() == "received") "RECEIVED" else "SENT"; return Tx(System.currentTimeMillis(), kind, m.groupValues[3].trim(), m.groupValues[2].toDouble(), System.currentTimeMillis()) }
    override fun onDestroy() { recognizer?.destroy(); tts?.shutdown(); super.onDestroy() }
}
