package xyz.adhithyan.scan2pdf.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import com.wonderkiln.camerakit.CameraKit
import kotlinx.android.synthetic.main.activity_preview.*
import kotlinx.android.synthetic.main.content_scan.*
import ru.whalemare.sheetmenu.SheetMenu
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import xyz.adhithyan.scan2pdf.R
import xyz.adhithyan.scan2pdf.extensions.getProgressBar
import xyz.adhithyan.scan2pdf.util.ResultHolder


class ScanActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar?.hide()
    setContentView(R.layout.activity_scan)
    setSupportActionBar(toolbar)


    supportActionBar?.title = "New Scan"
    ResultHolder.clearImages()
    bottomNavigationScan.setOnNavigationItemSelectedListener(bottomNavigationClickListener)
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

  private fun captureImage() {
    val progress = getProgressBar("Capturing image..")
    progress.show()

    camera.captureImage {
      val jpegBytes = it.jpeg
      ResultHolder.images?.add(jpegBytes)
      progress.dismiss()
    }
  }

  private fun done() {
    if(ResultHolder.images?.size!! > 0) {
      startPreviewActivity()
    } else {
      Toast.makeText(this, "No picture was captured. Take a picture to convert to pdf.", Toast.LENGTH_LONG).show()
    }
  }

  private fun startPreviewActivity() {
    ResultHolder.image = ResultHolder.images?.first
    val intent = Intent(this@ScanActivity, PreviewActivity::class.java)
    startActivity(intent)
  }

  private val bottomNavigationClickListener by lazy {
    BottomNavigationView.OnNavigationItemSelectedListener { item ->
      when (item.itemId) {
        R.id.scan_take_photo -> { captureImage(); return@OnNavigationItemSelectedListener true }
        R.id.scan_convert -> { done(); return@OnNavigationItemSelectedListener true }
        R.id.scan_flash_options -> { showFlashOptionsMenu(); return@OnNavigationItemSelectedListener true }
      }
      false
    }
  }

  private fun showFlashOptionsMenu() {
    SheetMenu().apply {
      titleId = R.string.sheet_menu_flash
      click = MenuItem.OnMenuItemClickListener {
        when (it.itemId) {
          R.id.flash_auto -> { autoFlash();true }
          R.id.flash_on -> { turnFlashOn(); true }
          R.id.flash_off -> { turnFlashOff(); true }
          else -> {true}
        }
      }
      menu = R.menu.menu_flash
    }.show(this)
  }

  private fun turnFlashOn() { camera.flash = CameraKit.Constants.FLASH_ON }
  private fun turnFlashOff() { camera.flash = CameraKit.Constants.FLASH_OFF }
  private fun autoFlash() { camera.flash = CameraKit.Constants.FLASH_AUTO }
}
