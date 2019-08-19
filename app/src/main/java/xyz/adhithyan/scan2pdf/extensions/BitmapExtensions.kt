package xyz.adhithyan.scan2pdf.extensions

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.io.ByteArrayOutputStream

fun Bitmap.toByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return stream.toByteArray()
}

fun Bitmap.applyMagicFilter(): Bitmap {
    val copy = this.copy(this.config, true)

    val srcImage = Mat(this.width, this.height, CvType.CV_8UC1)
    Utils.bitmapToMat(copy, srcImage)

    val destImage = Mat(srcImage.size(), CvType.CV_8UC1)

    srcImage.convertTo(destImage, -1, 1.9, (-80).toDouble())
    srcImage.release()

    Utils.matToBitmap(destImage, copy)
    destImage.release()

    return copy
}