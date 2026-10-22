# On-Device Handwritten Digit Recognizer

An Android app that recognizes handwritten digits (0–9) drawn on screen. A small CNN is trained in TensorFlow on MNIST, converted to TensorFlow Lite, and run entirely on the phone. No network connection or cloud API is used.

## Results

**Model accuracy on the MNIST test set (10,000 images)**

| Model | Changes | Test accuracy |
|---|---|---|
| Baseline CNN | 5 epochs | 96.31% |
| CNN + Dropout | Dropout(0.3), 10 epochs | 99.16% |
| CNN + Dropout + augmentation (final) | RandomTranslation, RandomRotation, RandomZoom, 15 epochs | 99.17% |

**On-device check:** the TFLite model matched the Keras model on 100 of 100 test images, and the Android app returned the correct label for a known MNIST test image (784-pixel input verified).

**Hand-drawn accuracy (self-collected, logged in the app)**

| Version | Input preprocessing | Correct | Accuracy |
|---|---|---|---|
| v1-simple | Resize the canvas directly to 28×28 | 48 / 70 | 68.6% |
| v2-bbox | Crop to the drawn digit, scale to 20 px, center in 28×28 | 64 / 72 | 88.9% |
| v3-aug+bbox | v2 preprocessing + model trained with augmentation | 103 / 112 | 92.0% |

The hand-drawn set is small and drawn by a single person, so treat these numbers as indicative rather than definitive. Cropping and centering the input gave the largest improvement. Augmentation gave a smaller gain that is within the margin of error for this sample size.

## How it works

The project has two parts that share one file, the `.tflite` model.

**Training (Python, offline)**

1. Load MNIST and normalize pixels to the 0–1 range, with shape (28, 28, 1).
2. Train a CNN: augmentation layers (training only), then Conv2D → MaxPool → Conv2D → MaxPool → Flatten → Dense → Dropout → Dense with softmax.
3. Evaluate on the held-out test set.
4. Convert to TFLite with post-training quantization and check that its predictions match the Keras model.

**Inference (Android, on-device)**

1. The user draws a digit on a Compose canvas. Strokes are stored as screen coordinates.
2. The strokes are redrawn as white on black at 280×280, matching MNIST's format.
3. The bounding box of the drawing is found, cropped, scaled so its longer side is 20 px, and centered in a 28×28 frame.
4. Pixels are read as grayscale values in the 0–1 range.
5. The TFLite interpreter runs the model and returns 10 probabilities. The predicted digit is the index of the largest probability.

## Tech stack

- **Training:** Python, TensorFlow / Keras, NumPy, Matplotlib
- **Conversion:** TensorFlow Lite Converter with `Optimize.DEFAULT`
- **Android:** Kotlin, Jetpack Compose, TensorFlow Lite Android runtime, minimum SDK 24
- **Logging:** results written to a CSV file in app storage, tagged with the preprocessing and model version, so runs can be compared

## Project structure

```
digit-recognizer/
├── training/
│   └── digit_recognizer.ipynb     # training, evaluation, TFLite conversion
├── models/
│   └── digit_model_v3.tflite      # model used in the app
├── results/
│   └── results.csv                # logged hand-drawn predictions
├── android-app/
│   └── app/src/main/
│       ├── assets/digit_model_v3.tflite
│       └── java/.../
│           ├── MainActivity.kt         # UI: canvas, predict, feedback buttons
│           ├── DrawingPad.kt           # Compose canvas that collects strokes
│           ├── ImagePreprocessor.kt    # strokes → 28×28 input tensor
│           ├── DigitClassifier.kt      # wraps the TFLite interpreter
│           └── ResultLogger.kt         # writes results.csv
└── README.md
```

## Running it

**Train and convert**

1. Open the notebook in Google Colab or a local Jupyter environment with TensorFlow 2.x installed.
2. Run all cells. This produces `digit_model_v3.keras` and `digit_model_v3.tflite`.

**Run the app**

1. Copy `digit_model_v3.tflite` into `android-app/app/src/main/assets/`.
2. Open `android-app/` in Android Studio and run the app on a device or emulator.
3. Draw a digit, tap **Predict**, then tap the true digit to log the result.

**Export results**

```bash
adb shell run-as com.example.digitidentifier cat files/results.csv > results.csv
```

## Known limitations

- The model is most often wrong on **1 predicted as 2**, and sometimes on **3 predicted as 2**. Several of the 1 → 2 errors were made with high confidence, which suggests the model associates a drawn 1 with a 2 when the stroke has an extra flag or foot.
- Accuracy on hand-drawn digits is lower than on MNIST, mainly because drawing style, stroke width, and position vary more than in the training data.
- Hand-drawn results come from a single drawer and a small sample. They are not a general benchmark.

## Possible next steps

- Test with several different drawers to measure how well the model generalizes.
- Collect more examples of 1s and 3s and retrain.
- Add a "clear" and "undo" option to the canvas.
- Show the top-3 predictions with their probabilities to help users see uncertainty.

## Acknowledgments

Built following Google's TensorFlow Lite and Android codelab for handwritten digit recognition, extended with image preprocessing, augmentation, and on-device result logging.