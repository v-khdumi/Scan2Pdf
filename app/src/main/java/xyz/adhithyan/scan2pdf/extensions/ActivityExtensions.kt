package xyz.adhithyan.scan2pdf.extensions

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.view.MenuItem
import android.widget.Toast
import ru.whalemare.sheetmenu.SheetMenu
import xyz.adhithyan.scan2pdf.R
import java.io.ByteArrayOutputStream

fun Context.getProgressBar(message: String): ProgressDialog {
  val progress = ProgressDialog(this)
  progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)
  progress.isIndeterminate = true
  progress.setMessage(message)
  progress.setCancelable(false)
  return progress
}


fun Context.showLongToast(info: String) {
  Toast.makeText(this, info, Toast.LENGTH_LONG).show()
}

fun Uri.toByteArray(context: Context): ByteArray {
  val istream = context.contentResolver.openInputStream(this)
  val byteBuffer = ByteArrayOutputStream()
  val bufferSize = 1024
  val buffer = ByteArray(bufferSize)
  while(true) {
    val len = istream.read(buffer)
    if(len == -1) break
    byteBuffer.write(buffer, 0, len)
  }
  return byteBuffer.toByteArray()
}