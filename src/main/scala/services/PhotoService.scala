package services

import java.io.{File, FileOutputStream}
import java.nio.file.{Files, Paths}
object PhotoService {
  def uploadPhoto(photoByte: Array[Byte] ): String =
    val path = Paths.get("C:/animeHub/avatarPhoto")

    val outputStream = new FileOutputStream(new File( "C:/animeHub/avatarPhoto/file.jpg" ) )

    if(!Files.exists(path)){
      Files.createDirectory(path)
    }
    
    outputStream.write(photoByte)
    outputStream.close()
    "C:/animeHub/avatarPhoto/file.jpg"
}
