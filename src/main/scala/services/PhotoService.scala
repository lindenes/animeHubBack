package services

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File, FileOutputStream}
import java.nio.file.{Files, Paths}
import javax.imageio.ImageIO
object PhotoService {
  def uploadPhoto(photoByte: Array[Byte] ): String =
    val path = Paths.get("C:/animeHub/avatarPhoto")

    if (!Files.exists(path)) {
      Files.createDirectory(path)
    }

    val outputStream = new FileOutputStream(new File( "C:/animeHub/avatarPhoto/file.jpg" ) )
    
    outputStream.write(photoByte)
    outputStream.close()
    "C:/animeHub/avatarPhoto/file.jpg"
}
