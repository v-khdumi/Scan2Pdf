package xyz.adhithyan.scan2pdf.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
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
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.util.FileUtils
import xyz.adhithyan.scan2pdf.R

import kotlinx.android.synthetic.main.activity_preview.*
import kotlinx.android.synthetic.main.content_preview.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import xyz.adhithyan.scan2pdf.extensions.toByteArray
import xyz.adhithyan.scan2pdf.util.FileUtil
import xyz.adhithyan.scan2pdf.util.ImageUtil
import xyz.adhithyan.scan2pdf.util.PdfUtil
import xyz.adhithyan.scan2pdf.util.ResultHolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class PreviewActivity : AppCompatActivity() {
  var i = 0
  val n = ResultHolder.images!!.size

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_preview)
    setSupportActionBar(toolbar)

    title = "Preview - Scan2Pdf"
    setBitmap(BitmapFactory.decodeByteArray(ResultHolder.image!!, 0, ResultHolder.image?.size!!))
  }

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (resultCode) {
      Activity.RESULT_OK -> {
        if(requestCode == UCrop.REQUEST_CROP) {
          setCroppedImage(data)
        }
      }
      UCrop.RESULT_ERROR -> { Toast.makeText(this, "Error occured while cropping.", Toast.LENGTH_LONG).show() }
    }
  }

  fun saveAsPdf(v: View) {
    val progress = ProgressDialog(this)
    progress.isIndeterminate = true
    progress.setMessage("Creating pdf..")
    progress.show()
    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),System.currentTimeMillis().toString() + ".pdf")
    file.createNewFile()
    PdfUtil(file.absolutePath, ResultHolder.images!!).createPdf()
    progress.dismiss()
    Toast.makeText(this, "Pdf created successfully.", Toast.LENGTH_LONG).show()
  }

  fun previousImage(v: View) {
    i -= 1
    val currentImage = ResultHolder.images?.get(i % n)
    val currentImageSize = currentImage?.size!!
    setBitmap(BitmapFactory.decodeByteArray(currentImage, 0, currentImageSize))
  }

  fun nextImage(v: View) {
    i += 1
    val currentImage = ResultHolder.images?.get(i % n)
    val currentImageSize = currentImage?.size!!
    setBitmap(BitmapFactory.decodeByteArray(currentImage, 0, currentImageSize))
  }

  fun cropImage(v: View) {
    val srcFile = FileUtil.createFile("temp_crop_src.jpg", filesDir)
    if(srcFile.exists()) {
      srcFile.delete()
    }
    srcFile.createNewFile()
    val fos = FileOutputStream(srcFile)
    fos.write(ResultHolder.images?.get(i % n))

    val destFile = File(filesDir, "temp_crop_dest.jpg")
    if(destFile.exists()) {
      destFile.delete()
    }
    destFile.createNewFile()
    UCrop.of(Uri.fromFile(srcFile), Uri.fromFile(destFile))
        //.withAspectRatio(16F, 9F)
        .withMaxResultSize(ResultHolder.currentImageWidth, ResultHolder.currentImageHeight)
        .start(this)
  }


  fun convertToBw(v: View) {
    val progress = ProgressDialog(this)
    progress.setMessage("Converting image to B&W ..")
    progress.isIndeterminate = true
    progress.show()
    val bwImage = ImageUtil.convertToGrayscale(ResultHolder.images?.get(i % n)!!)
    ResultHolder.images!![i % n] = bwImage.toByteArray()
    setBitmap(bwImage)
    progress.dismiss()
  }

  private fun setBitmap(image: Bitmap) {
    ResultHolder.currentImageWidth = image.width
    ResultHolder.currentImageHeight = image.height
    previewImage.setImageBitmap(image)
  }

  private fun setCroppedImage(data: Intent?) {
    val result = UCrop.getOutput(data!!)
    val croppedImage = FileUtil.getByteArray(result!!, contentResolver)
    ResultHolder.images!![i % n] = croppedImage
    previewImage.setImageBitmap(BitmapFactory.decodeByteArray(croppedImage, 0, croppedImage.size))
  }
}
