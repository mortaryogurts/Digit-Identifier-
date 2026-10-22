package com.example.digitidentifier

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.digitidentifier.ui.theme.DigitIdentifierTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DigitIdentifierTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    display(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun display(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var canvasW by remember { mutableIntStateOf(1) }
    var canvasH by remember { mutableIntStateOf(1) }
    var result by remember { mutableStateOf("Draw a digit") }
    var lastPredicted by remember { mutableStateOf<Int?>(null)}
    var lastConf by remember { mutableFloatStateOf(0f) }
    var stats by remember { mutableStateOf(ResultLogger.summary(context)) }
    val classifier = remember { DigitClassifier(context) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        DrawingPad(
            strokes = strokes,
            modifier = Modifier.fillMaxWidth().height(500.dp),
            onSizeChanged = { w, h -> canvasW = w; canvasH = h },
        )
        Row {
            Button(onClick = { strokes.clear(); result = "Draw a digit" }) { Text("Clear") }
            Spacer(Modifier.width(12.dp))
            Button(onClick = {
                val input = ImagePreprocessor.toMnistInput(strokes.toList(), canvasW, canvasH)
                val (digit, conf) = classifier.classify(input)
                result = "Predicted $digit (${"%.1f".format(conf * 100)}%)"
                lastPredicted = digit
                lastConf = conf
            }) { Text("Predict") }
        }
        Text(result, modifier = Modifier.padding(top = 16.dp))

        lastPredicted?.let { predicted ->
            Text("What did you actually draw?", modifier = Modifier.padding(top = 8.dp))
            Row {
                (0..4).forEach { d ->
                    Button(onClick = {
                        ResultLogger.log(context, predicted, d, lastConf)
                        stats = ResultLogger.summary(context)
                        lastPredicted = null
                        strokes.clear()
                        result = "Draw a digit"
                    }) { Text("$d") }
                }
            }
            Row {
                (5..9).forEach { d ->
                    Button(onClick = {
                        ResultLogger.log(context, predicted, d, lastConf)
                        stats = ResultLogger.summary(context)
                        lastPredicted = null
                        strokes.clear()
                        result = "Draw a digit"
                    }) { Text("$d") }
                }
            }
        }
        Text(stats, modifier = Modifier.padding(top = 16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DigitIdentifierTheme {

    }
}