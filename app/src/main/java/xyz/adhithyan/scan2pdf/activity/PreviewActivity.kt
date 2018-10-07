package xyz.adhithyan.scan2pdf.activity

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.parser.InlineImageUtils
import xyz.adhithyan.scan2pdf.R

import kotlinx.android.synthetic.main.activity_preview.*
import kotlinx.android.synthetic.main.content_preview.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import xyz.adhithyan.scan2pdf.util.ImageUtil
import xyz.adhithyan.scan2pdf.util.PdfUtil
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

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
  }

  fun saveAsPdf(v: View) {
    Toast.makeText(this, "Save", Toast.LENGTH_LONG).show()
    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),System.currentTimeMillis().toString() + ".pdf")
    file.createNewFile()
    PdfUtil(file.absolutePath, arrayOf(ResultHolder.image!!)).createPdf()
  }

}
