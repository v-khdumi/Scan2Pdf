package xyz.adhithyan.scan2pdf.util

import android.graphics.*
import com.itextpdf.text.Rectangle
import android.opengl.ETC1.getHeight
import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.CvType





class ImageUtil {
  companion object {
    val THRESHOLD = 230

    fun calculateFitSize(originalWidth: Float, originalHeight: Float, documentSize: Rectangle): Rectangle {
      val widthChange = (originalWidth - documentSize.width) / originalWidth
      val heightChange = (originalHeight - documentSize.height) / originalHeight

      val changeFactor = if (heightChange >= widthChange) heightChange else widthChange
      val newWidth = (originalWidth - originalWidth * changeFactor).toInt()
      val newHeight = (originalHeight - originalHeight * changeFactor).toInt()

      return Rectangle(Math.abs(newWidth).toFloat(), Math.abs(newHeight).toFloat())
    }

    fun convertToGrayscale(image: ByteArray): Bitmap {
      val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
      val height = bitmap.height
      val width = bitmap.width

      val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
      val c = Canvas(bmpGrayscale)
      val paint = Paint()
      val cm = ColorMatrix()
      cm.setSaturation(0f)
      paint.colorFilter = ColorMatrixColorFilter(cm)
      c.drawBitmap(bitmap, 0F, 0F, paint)
      return bmpGrayscale
    }

    private fun colorToRgb(alpha: Int, red: Int, green: Int, blue: Int): Int {
      var newPixel = 0
      newPixel += alpha
      newPixel = newPixel shl 8
      newPixel += red
      newPixel = newPixel shl 8
      newPixel += green
      newPixel = newPixel shl 8
      newPixel += blue
      return newPixel
    }
  }
}

fun rotateBitmap(original: Bitmap, angle: Int): Bitmap {
  val matrix = Matrix()
  matrix.postRotate(90F)
  return Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
}

fun bitmapToMat(bitmap: Bitmap): Mat {
  val mat = Mat(bitmap.height, bitmap.width, CvType.CV_8U, Scalar(4.0))
  val bitmap32 = bitmap.copy(Bitmap.Config.ARGB_8888, true)
  Utils.bitmapToMat(bitmap32, mat)
  return mat
}

fun matToBitmap(mat: Mat): Bitmap {
  val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
  Utils.matToBitmap(mat, bitmap)
  return bitmap
}