package xyz.adhithyan.scan2pdf.activity

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_scan.*
import xyz.adhithyan.scan2pdf.R
import xyz.adhithyan.scan2pdf.R.id.camera
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Point
import com.wonderkiln.camerakit.CameraKitEventListener
import kotlinx.android.synthetic.main.activity_scan.view.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import xyz.adhithyan.scan2pdf.R.id.camera
import xyz.adhithyan.scan2pdf.util.ResultHolder


class ScanActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_scan)

    resizeButtons()
    title = "Scanner"
    ResultHolder.clearImages()
  }

  override fun onResume() {
    super.onResume()
    camera.start()
  }

  override fun onPause() {
    camera.stop()
    super.onPause()
  }

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
  }

  fun captureImage(v: View) {
    var progress = ProgressDialog(this)
    progress.isIndeterminate = true
    progress.setMessage("Processing image..")
    progress.show()

    camera.captureImage {
      val jpegBytes = it.jpeg
      ResultHolder.images?.add(jpegBytes)
    }

    progress.dismiss()
  }

  fun done(v: View) {
    startPreviewActivity()
  }

  private fun resizeButtons() {
    val display = windowManager.defaultDisplay
    val size = Point()
    display.getSize(size)
    val width = (size.x / 2)

    buttonCapture.width = width
    buttonDone.width = width
  }

  private fun startPreviewActivity() {
    ResultHolder.image = ResultHolder.images?.first
    val intent = Intent(this@ScanActivity, PreviewActivity::class.java)
    startActivity(intent)
  }
}
