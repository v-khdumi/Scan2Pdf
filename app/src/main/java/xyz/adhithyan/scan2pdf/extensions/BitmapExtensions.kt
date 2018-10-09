package xyz.adhithyan.scan2pdf.extensions

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

fun Bitmap.toByteArray(): ByteArray {
  val stream = ByteArrayOutputStream()
  this.compress(Bitmap.CompressFormat.JPEG, 100, stream)
  return stream.toByteArray()
}