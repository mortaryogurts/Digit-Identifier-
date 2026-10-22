package com.example.digitidentifier

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged

@Composable
fun DrawingPad(
    strokes: SnapshotStateList<List<Offset>>,
    modifier: Modifier = Modifier,
    onSizeChanged: (Int, Int) -> Unit,
) {
    Canvas(
        modifier = modifier
            .background(Color.Black)
            .onSizeChanged { onSizeChanged(it.width, it.height) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { start -> strokes.add(listOf(start)) },
                    onDrag = { change, _ ->
                        change.consume()
                        val i = strokes.lastIndex
                        strokes[i] = strokes[i] + change.position
                    },
                )
            }
    ) {
        strokes.forEach { stroke ->
            for (i in 1 until stroke.size) {
                drawLine(
                    color = Color.White,
                    start = stroke[i - 1],
                    end = stroke[i],
                    strokeWidth = 40f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}