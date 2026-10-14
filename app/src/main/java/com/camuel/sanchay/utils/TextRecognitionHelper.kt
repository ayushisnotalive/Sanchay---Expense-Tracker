package com.camuel.sanchay.utils

import android.media.Image
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextRecognitionHelper {
    
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    @OptIn(ExperimentalGetImage::class)
    fun processImage(imageProxy: ImageProxy, onResult: (String?, String, List<Rect>) -> Unit) {
        val mediaImage: Image? = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    // A simple regex to find the first match of a price (e.g., $12.34 or 12.34)
                    val amountRegex = Regex("""\$?\s*(\d+\.\d{2})""")
                    val amountMatch = amountRegex.find(text)
                    val amount = amountMatch?.groups?.get(1)?.value
                    
                    val boundingBoxes = mutableListOf<Rect>()
                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            if (amount != null && line.text.contains(amount)) {
                                line.boundingBox?.let { boundingBoxes.add(it) }
                            }
                        }
                    }
                    
                    onResult(amount, text, boundingBoxes)
                }
                .addOnFailureListener {
                    onResult(null, "", emptyList())
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
            onResult(null, "", emptyList())
        }
    }
}
