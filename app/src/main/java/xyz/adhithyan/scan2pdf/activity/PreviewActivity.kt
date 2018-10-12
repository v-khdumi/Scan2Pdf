package xyz.adhithyan.scan2pdf.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import com.yalantis.ucrop.UCrop
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_preview.*
import kotlinx.android.synthetic.main.content_preview.*
import ru.whalemare.sheetmenu.SheetMenu
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import xyz.adhithyan.scan2pdf.R
import xyz.adhithyan.scan2pdf.extensions.getProgressBar
import xyz.adhithyan.scan2pdf.extensions.toByteArray
import xyz.adhithyan.scan2pdf.listeners.SwypeListener
import xyz.adhithyan.scan2pdf.util.FileUtil
import xyz.adhithyan.scan2pdf.util.ImageUtil
import xyz.adhithyan.scan2pdf.util.PdfUtil
import xyz.adhithyan.scan2pdf.util.ResultHolder
import java.io.File
import java.io.FileOutputStream

class PreviewActivity : AppCompatActivity() {
  var i = 0
  val n = ResultHolder.images!!.size

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_preview)
    setSupportActionBar(toolbar)

    title = "Preview - Scan2Pdf"
    setBitmap(BitmapFactory.decodeByteArray(ResultHolder.image!!, 0, ResultHolder.image?.size!!))
    bottomNavigationPreview.setOnNavigationItemSelectedListener(bottomNavigationClickListener)
    setImageviewSwypeListener()
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

  fun saveAsPdf() {
    val progress = getProgressBar("Creating pdf..")
    progress.show()

    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),System.currentTimeMillis().toString() + ".pdf")
    file.createNewFile()

    Observable.just<Unit>(Unit)
        .map { PdfUtil(file.absolutePath, ResultHolder.images!!).createPdf() }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          Toast.makeText(this, "Pdf created successfully.", Toast.LENGTH_LONG).show()
          progress.dismiss()
        }
  }

  fun previousImage() {
    i -= 1
    setCurrentImage()
  }

  fun nextImage() {
    i += 1
    setCurrentImage()
  }

  fun cropImage() {
    val srcFile = FileUtil.createFile("temp_crop_src.jpg", filesDir)
    if(srcFile.exists()) {
      srcFile.delete()
    }
    srcFile.createNewFile()
    val fos = FileOutputStream(srcFile)
    fos.write(ResultHolder.images?.get(Math.abs(i % n)))

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


  fun convertToBw() {
    val progress = getProgressBar("Converting to black & white..")
    progress.show()

    Observable.just<Unit>(Unit)
        .map { ImageUtil.convertToGrayscale(ResultHolder.images?.get(Math.abs(i % n))!!) }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          ResultHolder.images!![Math.abs(i % n)] = it.toByteArray()
          setBitmap(it)
          progress.dismiss()
        }
  }

  private fun setCurrentImage() {
    val currentImage = ResultHolder.images?.get(Math.abs(i % n))
    val currentImageSize = currentImage?.size!!
    setBitmap(BitmapFactory.decodeByteArray(currentImage, 0, currentImageSize))
  }

  private fun setBitmap(image: Bitmap) {
    ResultHolder.currentImageWidth = image.width
    ResultHolder.currentImageHeight = image.height
    previewImage.setImageBitmap(image)
  }

  private fun setCroppedImage(data: Intent?) {
    val result = UCrop.getOutput(data!!)
    val croppedImage = FileUtil.getByteArray(result!!, contentResolver)
    ResultHolder.images!![Math.abs(i % n)] = croppedImage
    previewImage.setImageBitmap(BitmapFactory.decodeByteArray(croppedImage, 0, croppedImage.size))
  }

  private fun setImageviewSwypeListener() {
    previewImage.setOnTouchListener(object : SwypeListener(this@PreviewActivity) {
      override fun onSwipeTop() { }

      override fun onSwipeRight() { nextImage() }

      override fun onSwipeLeft() { previousImage() }

      override fun onSwipeBottom() { }
    })
  }

  private val bottomNavigationClickListener by lazy {
    BottomNavigationView.OnNavigationItemSelectedListener { item ->
      when (item.itemId) {
        R.id.preview_menu_crop -> {
          cropImage()
          return@OnNavigationItemSelectedListener true
        }
        R.id.preview_menu_filters-> {
          showPhotoFilters()
          return@OnNavigationItemSelectedListener true
        }
        R.id.preview_menu_convert -> {
          saveAsPdf()
          return@OnNavigationItemSelectedListener true
        }
      }
      false
    }
  }

  private fun showPhotoFilters() {
    SheetMenu().apply {
      titleId = R.string.sheet_menu_title
      click = MenuItem.OnMenuItemClickListener {
        when (it.itemId) {
          R.id.filter_bw -> { convertToBw();true}
          else -> {true}
        }
      }
      menu = R.menu.photo_filters
    }.show(this)
  }
}
