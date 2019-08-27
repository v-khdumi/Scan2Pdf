package xyz.adhithyan.scan2pdf.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

val ROOT_PATH = Environment.getExternalStorageDirectory().absolutePath + "/Scan2Pdf/"

class FileUtil {
  companion object {

    fun getByteArray(uri: Uri, contentResolver: ContentResolver): ByteArray {
      val inputStream = contentResolver.openInputStream(uri)
      val bos = ByteArrayOutputStream()
      val buffer = ByteArray(1024)
      var len = 0

      while(true) {
        len = inputStream.read(buffer)
        if(len < 0) { break }
        bos.write(buffer)
      }

      return bos.toByteArray()
    }

    fun createFile(filename: String, filesDir: File): File {
      val file = File(filesDir, "temp_crop_src.jpg")
      if(file.exists()) {
        file.delete()
      }
      file.createNewFile()
      return file
    }

    fun copyImage(image: ByteArray, filename: String, filesDir: File): File {
      val file = File(filesDir, filename)
      if(file.exists()) { file.delete() }
      file.createNewFile()

      val fos = FileOutputStream(file)
      fos.write(image)

      return file
    }
  }
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

fun createNewPdfFile(): File {
  val root = File(ROOT_PATH)
  if(!root.exists()) {
    root.mkdirs()
  }

  val file = File(ROOT_PATH, "${System.currentTimeMillis()}.pdf")
  file.createNewFile()

  return file
}

fun listAllFiles(): Array<String> {
  val root = File(ROOT_PATH)

  if(!root.exists()) {
    return arrayOf("No scans present. Go ahead and create a new scan.")
  }

  val files = root.listFiles().filter { it.name.contains(".pdf") }
  var pdfFiles = LinkedList<String>()
  for(file in files) {
    pdfFiles.add(file.name)
  }

  if(pdfFiles.isEmpty()) {
    return arrayOf("No scans present. Go ahead and create a new scan.")
  }

  return pdfFiles.toTypedArray()
}