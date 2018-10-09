package xyz.adhithyan.scan2pdf.util

import com.itextpdf.text.Rectangle


class ImageUtil {
  companion object {

    fun calculateFitSize(originalWidth: Float, originalHeight: Float, documentSize: Rectangle): Rectangle {
      val widthChange = (originalWidth - documentSize.width) / originalWidth
      val heightChange = (originalHeight - documentSize.height) / originalHeight

      val changeFactor = if (heightChange >= widthChange) heightChange else widthChange
      val newWidth = (originalWidth - originalWidth * changeFactor).toInt()
      val newHeight = (originalHeight - originalHeight * changeFactor).toInt()

      return Rectangle(Math.abs(newWidth).toFloat(), Math.abs(newHeight).toFloat())
    }


  }


}