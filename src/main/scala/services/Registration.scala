package services

import _root_.services.ServiceList.User
import io.circe.literal.json
import io.circe.Json
import java.security.MessageDigest
import java.sql.DriverManager
import java.sql.SQLException
import java.util.Base64

object Registration:

  def addNewUser(user: User, validationSuccess: Boolean): Json =

    val loginRegistrationError = checkLoginRegistration(user)
    val passwordRegistrationError = checkPasswordRegistration(user)

    if validationSuccess && loginRegistrationError._2 &&
      passwordRegistrationError._2
    then
      val userRegInfo = add(user)
      userRegInfo.fold(
        valueA => json"""{"registrationError" : "" }""",
        valueB => json"""{"registrationError" : ${valueB._1} }""",
      )
    else
      json"""{"registrationLoginError" : ${loginRegistrationError
          ._1}, "registrationMailError": ${passwordRegistrationError._1} }"""

  private def checkLoginRegistration(user: User): (String, Boolean) =
    try
      val url = "jdbc:mysql://127.0.0.1/animeHub"
      val username = "root"
      val password = ",tkstudjplbrb"
      Class.forName("com.mysql.cj.jdbc.Driver")
      val connection = DriverManager.getConnection(url, username, password)

      // Создаем объект Statement и выполняем запрос
      val query = "SELECT id, login FROM user WHERE login = ?"
      val preparedStatement = connection.prepareStatement(query)
      preparedStatement.setString(1, user.login)
      val resultSet = preparedStatement.executeQuery()

      val message =
        if resultSet.next() && resultSet.getInt(1) > 0 then
          ("Пользователь с таким логином уже зарегистрирован", false)
        else ("", true)
      message
    catch
      case ex: SQLException => (
          "Ошибка при проверке логина во время регистрации " + ex.getMessage,
          false,
        )

  private def checkPasswordRegistration(user: User): (String, Boolean) =
    try
      val url = "jdbc:mysql://127.0.0.1/animeHub"
      val username = "root"
      val password = ",tkstudjplbrb"
      Class.forName("com.mysql.cj.jdbc.Driver")
      val connection = DriverManager.getConnection(url, username, password)

      // Создаем объект Statement и выполняем запрос
      val query = "SELECT id, login, email FROM user WHERE email = ?"
      val preparedStatement = connection.prepareStatement(query)
      preparedStatement.setString(1, user.email)
      val resultSet = preparedStatement.executeQuery()

      val message =
        if resultSet.next() && resultSet.getInt(1) > 0 then
          ("Пользователь с такой почтой уже зарегистрирован", false)
        else ("", true)
      message
    catch
      case ex: SQLException => (
          "Ошибка при проверке почты во время регистарции " + ex.getMessage,
          false,
        )

  def add(user: User): Either[Unit, (String, Boolean)] =
    try
      val url = "jdbc:mysql://127.0.0.1/animeHub"
      val username = "root"
      val password = ",tkstudjplbrb"
      Class.forName("com.mysql.cj.jdbc.Driver")
      val connection = DriverManager.getConnection(url, username, password)
      val sql =
        "INSERT INTO user (login, email, password_hash, age, avatar_path, role, xxx_content) VALUES (?, ?, ?, ? ,?, ?, ?)"
      val preparedStatement = connection.prepareStatement(sql)

      val photoPath = PhotoService
        .uploadAvatarPhoto(Base64.getDecoder.decode(user.photo), user.login)

      preparedStatement.setString(1, user.login)
      preparedStatement.setString(2, user.email)
      preparedStatement.setString(3, passHash(user.password))
      preparedStatement.setInt(4, user.age.getOrElse(0))
      preparedStatement.setString(5, photoPath)
      preparedStatement.setInt(6, 2)
      preparedStatement.setInt(7, 0)

      preparedStatement.executeUpdate()

      preparedStatement.close()

      foundUserAfterRegistration(user.login)

      Left(())
    catch
      case ex: SQLException => Right(
          ("Ошибка при добавлении пользователя в бд " + ex.getMessage, false)
        )

  private def foundUserAfterRegistration(login: String): Unit =
    val url = "jdbc:mysql://127.0.0.1/animeHub"
    val username = "root"
    val password = ",tkstudjplbrb"
    Class.forName("com.mysql.cj.jdbc.Driver")
    val connection = DriverManager.getConnection(url, username, password)

    val sql = "SELECT id FROM user WHERE login = ?"
    val statement = connection.prepareStatement(sql)
    statement.setString(1, login)
    val resultSet = statement.executeQuery

    if resultSet.next() then
      val id = resultSet.getInt("id")
      connection.close()
      addDefaultPlaylists(id)

  private def addDefaultPlaylists(userId: Int): Unit =
    val url = "jdbc:mysql://127.0.0.1/animeHub"
    val username = "root"
    val password = ",tkstudjplbrb"
    Class.forName("com.mysql.cj.jdbc.Driver")
    val connection = DriverManager.getConnection(url, username, password)

    val names = Array("Просмотрено", "Буду смотреть", "Смотрю")
    names.foreach(name =>
      val sql =
        "INSERT INTO playlist (title, user_id, is_private) VALUES (?,?,?)"
      val preparedStatement = connection.prepareStatement(sql)
      preparedStatement.setString(1, name)
      preparedStatement.setInt(2, userId)
      preparedStatement.setInt(3, 1)
      preparedStatement.executeUpdate()
      preparedStatement.close()
    )

  def passHash(password: String): String =
    val md = MessageDigest.getInstance("SHA-256")
    val bytes = md.digest(password.getBytes("UTF-8"))
    bytes.map("%02x".format(_)).mkString
