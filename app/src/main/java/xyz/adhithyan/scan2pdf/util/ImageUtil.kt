package xyz.adhithyan.scan2pdf.util

import android.graphics.*
import com.itextpdf.text.Rectangle
import android.opengl.ETC1.getHeight



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