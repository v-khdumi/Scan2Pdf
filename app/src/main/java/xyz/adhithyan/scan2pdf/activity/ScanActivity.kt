package xyz.adhithyan.scan2pdf.activity

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
import com.wonderkiln.camerakit.CameraKitEventListener
import kotlinx.android.synthetic.main.activity_scan.view.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import xyz.adhithyan.scan2pdf.R.id.camera
import xyz.adhithyan.scan2pdf.util.ResultHolder


class ScanActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_scan)

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
    //camera.captureImage()
    val captureStartTime = System.currentTimeMillis()
    camera.captureImage {
      val jpegBytes = it.jpeg
      val callbackTime = System.currentTimeMillis()
      ResultHolder.image = jpegBytes
      ResultHolder.timeToCallback = callbackTime - captureStartTime
      val intent = Intent(this@ScanActivity, PreviewActivity::class.java)
      startActivity(intent)
    }
  }

}
