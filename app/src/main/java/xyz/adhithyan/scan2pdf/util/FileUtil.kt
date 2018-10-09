package xyz.adhithyan.scan2pdf.util

import android.content.ContentResolver
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File

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

  }
}