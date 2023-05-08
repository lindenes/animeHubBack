package services

import cats.effect.{IO, IOApp, Resource}

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File, FileOutputStream, IOException}
import java.nio.file.{FileSystems, Files, InvalidPathException, Paths}
import javax.imageio.ImageIO
import java.nio.file.StandardOpenOption.{CREATE, TRUNCATE_EXISTING}
import scala.jdk.CollectionConverters
object PhotoService {
  def uploadAvatarPhoto(photoByte: Array[Byte], login:String ): String =
    val rootPath = FileSystems.getDefault.getRootDirectories.iterator.next()
    val avatarPath = rootPath.resolve("animeHub\\avatarPhoto")

    Files.createDirectories(avatarPath)

    val outputStream = new FileOutputStream(new File( s"$avatarPath\\$login.jpg"))

    outputStream.write(photoByte)
    outputStream.close()
    s"$avatarPath\\$login.jpg"
}
