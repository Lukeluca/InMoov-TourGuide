package com.lukeluca.androidthings.inmoov

import android.graphics.Bitmap
import com.google.firebase.ml.vision.face.FirebaseVisionFace


public class FaceExtractor {

    public fun getFaceBitmap (source: Bitmap , face : FirebaseVisionFace) : Bitmap? {

        val boundingBox = face.boundingBox

        // Extract face image
        val faceData = IntArray(boundingBox.width() * boundingBox.height())

        val left = if (boundingBox.left <= 0) 0 else boundingBox.left
        val top = if (boundingBox.top <= 0) 0 else boundingBox.top

        val width = if (left + boundingBox.width() > source.width) source.width - left else boundingBox.width()
        val height = if (top + boundingBox.height() > source.height) source.height - top else boundingBox.height()
        if (width > 0 && height > 0) {
            source.getPixels(
                    faceData,
                    0,
                    boundingBox.width(),
                    left,
                    top,
                    width,
                    height
            )

            return Bitmap.createBitmap(
                    faceData,
                    boundingBox.width(),
                    boundingBox.height(),
                    Bitmap.Config.ARGB_8888
            )
        } else {
            return null
        }

    }

}
