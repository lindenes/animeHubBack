package services

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO

object PhotoService:

  def uploadAvatarPhoto(photoByte: Array[Byte], login: String): String =
    val avatarPath = Paths.get("D:/developing/anime-hub/public/avatarPhoto")

    Files.createDirectories(avatarPath)

    val inputStream = new ByteArrayInputStream(photoByte)
    val imageFormat = ImageIO.read(inputStream).getType match
      case BufferedImage.TYPE_INT_RGB | BufferedImage.TYPE_INT_ARGB =>
        if ImageIO.getUseCache then "jpg" else "jpeg"
      case BufferedImage.TYPE_INT_BGR =>
        if ImageIO.getUseCache then "bjpg" else "jpeg"
      case BufferedImage.TYPE_3BYTE_BGR => "jpg"
      case BufferedImage.TYPE_4BYTE_ABGR => "png"
      case BufferedImage.TYPE_BYTE_GRAY | BufferedImage.TYPE_USHORT_GRAY =>
        "png"
      case BufferedImage.TYPE_BYTE_BINARY => "webp"
      case _ => "unknown"

    val outputStream =
      new FileOutputStream(new File(s"$avatarPath\\$login.$imageFormat"))

    outputStream.write(photoByte)
    outputStream.close()
    s"avatarPhoto/$login.$imageFormat"

  def uploadPostPhoto(photoByte: Array[Byte], title: String): String =
    val avatarPath = Paths.get("D:/developing/anime-hub/public/postMainPhoto")

    Files.createDirectories(avatarPath)

    val inputStream = new ByteArrayInputStream(photoByte)
    val imageFormat = ImageIO.read(inputStream).getType match
      case BufferedImage.TYPE_INT_RGB | BufferedImage.TYPE_INT_ARGB =>
        if ImageIO.getUseCache then "jpg" else "jpeg"
      case BufferedImage.TYPE_INT_BGR =>
        if ImageIO.getUseCache then "bjpg" else "jpeg"
      case BufferedImage.TYPE_3BYTE_BGR => "jpg"
      case BufferedImage.TYPE_4BYTE_ABGR => "png"
      case BufferedImage.TYPE_BYTE_GRAY | BufferedImage.TYPE_USHORT_GRAY =>
        "png"
      case BufferedImage.TYPE_BYTE_BINARY => "webp"
      case _ => "unknown"

    val outputStream =
      new FileOutputStream(new File(s"$avatarPath\\$title.$imageFormat"))

    outputStream.write(photoByte)
    outputStream.close()
    s"postMainPhoto/$title.$imageFormat"
