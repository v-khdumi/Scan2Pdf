package xyz.adhithyan.scan2pdf.extensions

import android.app.ProgressDialog
import android.content.Context
import android.view.MenuItem
import android.widget.Toast
import ru.whalemare.sheetmenu.SheetMenu
import xyz.adhithyan.scan2pdf.R

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