package services

import cats.effect.IO

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File, FileOutputStream}
import java.nio.file.{Files, Paths}
import javax.imageio.ImageIO
import java.nio.file.StandardOpenOption.{CREATE, TRUNCATE_EXISTING}
object PhotoService {
  def uploadAvatarPhoto(photoByte: Array[Byte], login:String ): IO[String] =
    for{
      pathForPhoto <- IO.delay(Paths.get("C:/animeHub/avatarPhoto") )
      _ <- IO.delay( if (!Files.exists(pathForPhoto)) Files.createDirectory(pathForPhoto) )
      filePath = new File("C:/animeHub/avatarPhoto" + login )
      _ <- IO.blocking( Files.write(filePath.toPath, photoByte, CREATE, TRUNCATE_EXISTING) )
    }yield  filePath.getAbsolutePath
}
