package com.example.digitidentifier

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DigitClassifier(context: Context) {
    private val interpreter: Interpreter

    init {
        val bytes = context.assets.open("digit_model_v3.tflite").use { it.readBytes() }
        val buffer = ByteBuffer.allocateDirect(bytes.size)
            .order(ByteOrder.nativeOrder())
            .apply { put(bytes); rewind() }
        interpreter = Interpreter(buffer)
    }

    // pixels: 784 floats in 0..1, row-major, 28x28
    fun classify(pixels: FloatArray): Pair<Int, Float> {
        val input = ByteBuffer.allocateDirect(4 * 28 * 28)
            .order(ByteOrder.nativeOrder())
        pixels.forEach { input.putFloat(it) }
        input.rewind()

        val output = Array(1) { FloatArray(10) }
        interpreter.run(input, output)

        val probs = output[0]
        val best = probs.indices.maxByOrNull { probs[it] }!!
        return best to probs[best]
    }
}