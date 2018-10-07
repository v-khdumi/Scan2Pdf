package xyz.adhithyan.scan2pdf.activity

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfWriter
import xyz.adhithyan.scan2pdf.R

import kotlinx.android.synthetic.main.activity_preview.*
import kotlinx.android.synthetic.main.content_preview.*
import xyz.adhithyan.scan2pdf.util.ResultHolder
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class PreviewActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_preview)
    setSupportActionBar(toolbar)

    previewImage.setImageBitmap(BitmapFactory.decodeByteArray(ResultHolder.image!!, 0, ResultHolder.image?.size!!))
  }

  fun saveAsPdf(v: View) {
    Toast.makeText(this, "Save", Toast.LENGTH_LONG).show()
    //val name =
    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),System.currentTimeMillis().toString() + ".pdf")
    file.createNewFile()
    val document = Document()
    try {
      //Toast.makeText(this, file.absolutePath, Toast.LENGTH_LONG).show()
      val instance = PdfWriter.getInstance(document, FileOutputStream(file.absolutePath))
      val image = Image.getInstance(ResultHolder.image!!, false)
      image.compressionLevel = (30 * 0.75).toInt()
      image.border = Rectangle.BOX

      document.open()
      document.add(image)
      document.close()
      Log.d("PODA", file.absolutePath)
      Log.d("PODA", file.path)

    } catch (ex: Exception) {
      ex.printStackTrace()
      Toast.makeText(this, "Save FAIL", Toast.LENGTH_LONG).show()

    }
    //Toast.makeText(this, "Save SUCCESS", Toast.LENGTH_LONG).show()

  }

}
