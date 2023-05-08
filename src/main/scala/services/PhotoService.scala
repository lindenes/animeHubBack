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

    val inputStream = new ByteArrayInputStream(photoByte)
    val imageFormat = ImageIO.read(inputStream).getType match
      case BufferedImage.TYPE_INT_RGB | BufferedImage.TYPE_INT_ARGB =>
        if (ImageIO.getUseCache) "jpg" else "jpeg"
      case BufferedImage.TYPE_INT_BGR =>
        if (ImageIO.getUseCache) "bjpg" else "jpeg"
      case BufferedImage.TYPE_3BYTE_BGR => "jpg"
      case BufferedImage.TYPE_4BYTE_ABGR => "png"
      case BufferedImage.TYPE_BYTE_GRAY | BufferedImage.TYPE_USHORT_GRAY => "png"
      case BufferedImage.TYPE_BYTE_BINARY => "webp"
      case _ => "unknown"

    val outputStream = new FileOutputStream(new File( s"$avatarPath\\$login.$imageFormat"))

    outputStream.write(photoByte)
    outputStream.close()
    s"$avatarPath\\$login.$imageFormat"
}
