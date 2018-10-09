package xyz.adhithyan.scan2pdf

import android.app.Application
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class App: Application() {

  override fun onCreate() {
    super.onCreate()

    val font = CalligraphyConfig.Builder()
        .setDefaultFontPath("fonts/GoogleSans-Regular.ttf")
        .setFontAttrId(R.attr.fontPath)
        .build()
    CalligraphyConfig.initDefault(font)
  }

}