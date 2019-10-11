package xyz.adhithyan.scan2pdf.util

import android.graphics.BitmapFactory
import android.widget.Toast
import com.itextpdf.text.*
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.GrayColor
import com.itextpdf.text.pdf.PdfPageEventHelper
import com.itextpdf.text.pdf.PdfWriter
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*

class PdfUtil(internal val filename: String, internal val jpegs: LinkedList<ByteArray>) {
    fun createPdf() {
        val document = Document(PageSize.getRectangle("A4"), 50F, 38F, 50F, 38F)
        val documentRectangle = document.pageSize
        document.setMargins(50F, 38F, 50F, 38F)

        try {
            val writer = PdfWriter.getInstance(document, FileOutputStream(filename))
            writer.pageEvent = WatermarkEvent()
            document.open()

            for (jpeg in jpegs) {
                val image = Image.getInstance(jpeg, false)
                //image.compressionLevel = (30 * 0.9).toInt()
                image.border = Rectangle.BOX
                image.borderWidth = 0F

                val bitmap = BitmapFactory.decodeByteArray(ResultHolder.image!!, 0, ResultHolder.image?.size!!)
                image.scaleAbsolute(ImageUtil.calculateFitSize(bitmap.width.toFloat(), bitmap.height.toFloat(), documentRectangle))

                val width = (documentRectangle.width - image.scaledWidth) / 2
                val height = (documentRectangle.height - image.scaledHeight) / 2

                image.setAbsolutePosition(width, height)

                document.add(image)
                document.newPage()
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            if (document.isOpen) {
                document.close()
            }
        }
    }
}

class WatermarkEvent: PdfPageEventHelper() {
    val FONT = Font(Font.FontFamily.HELVETICA, 52F, Font.BOLD, GrayColor(0.85f))

    override fun onEndPage(writer: PdfWriter, document: Document?) {
        ColumnText.showTextAligned(writer.directContentUnder, Element.ALIGN_CENTER, Phrase("Scanned by Scan2Pdf", FONT), 297.5f, 421f, if (writer.pageNumber % 2 == 1) 45F else -45F)
    }
}