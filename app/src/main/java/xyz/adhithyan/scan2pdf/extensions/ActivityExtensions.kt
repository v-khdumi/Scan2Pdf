package xyz.adhithyan.scan2pdf.extensions

import android.app.ProgressDialog
import android.content.Context
import android.view.MenuItem
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
