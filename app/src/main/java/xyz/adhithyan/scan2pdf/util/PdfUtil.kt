package xyz.adhithyan.scan2pdf.util

import android.graphics.BitmapFactory
import android.widget.Toast
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfWriter
import java.io.FileOutputStream
import java.lang.Exception

class PdfUtil(internal val filename: String, internal val jpegs: Array<ByteArray>) {

  fun createPdf() {
    val document = Document(PageSize.getRectangle("A4"), 50F, 38F, 50F, 38F)
    val documentRectangle = document.pageSize
    document.setMargins(50F, 38F, 50F, 38F)

    try {
      PdfWriter.getInstance(document, FileOutputStream(filename))

      for(jpeg in jpegs) {
        val image = Image.getInstance(jpeg, false)
        image.compressionLevel = (30 * 0.75).toInt()
        image.border = Rectangle.BOX
        image.borderWidth = 0F

        val bitmap= BitmapFactory.decodeByteArray(ResultHolder.image!!, 0, ResultHolder.image?.size!!)
        val imageSize = ImageUtil.getFitSize(bitmap.width.toFloat(), bitmap.height.toFloat(), documentRectangle)
        image.scaleAbsolute(ImageUtil.calculateFitSize(bitmap.width.toFloat(), bitmap.height.toFloat(), documentRectangle))

        val width = (documentRectangle.width - image.scaledWidth) / 2
        val height = (documentRectangle.height - image.scaledHeight) / 2

        image.setAbsolutePosition(width, height)

        document.open()
        document.add(image)
        document.close()
      }

    } catch (ex: Exception) {
      ex.printStackTrace()
    }
  }

}