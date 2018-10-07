package xyz.adhithyan.scan2pdf.util

import com.itextpdf.text.Rectangle


class ImageUtil {
  companion object {
    fun getFitSize(width: Float, height: Float, docSize: Rectangle): Rectangle {
      var scaledWidth = (width - docSize.width) / width
      var scaledHeight = (height - docSize.height) / height
      var changeFactor = if (scaledHeight >= scaledWidth) scaledHeight else scaledWidth

      var resizedWidth = width - (scaledWidth * changeFactor)
      var resizedHeight = height - (scaledHeight * changeFactor)

      return Rectangle(Math.abs(resizedWidth), Math.abs(resizedHeight))
    }

    fun calculateFitSize(originalWidth: Float, originalHeight: Float, documentSize: Rectangle): Rectangle {
      val widthChange = (originalWidth - documentSize.width) / originalWidth
      val heightChange = (originalHeight - documentSize.height) / originalHeight

      val changeFactor: Float
      if (widthChange >= heightChange) {
        changeFactor = widthChange
      } else {
        changeFactor = heightChange
      }
      val newWidth = originalWidth - originalWidth * changeFactor
      val newHeight = originalHeight - originalHeight * changeFactor

      return Rectangle(Math.abs(newWidth.toInt()).toFloat(), Math.abs(newHeight.toInt()).toFloat())
    }
  }


}