package services

import _root_.data.sqlquery
import _root_.data.sqlquery.PersonQuery
import _root_.data.sqlquery.PersonQuery.UserInfo
import io.circe.literal.json
import io.circe.Json
import java.security.MessageDigest
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

object Authorization:

  def getAuth(login: String, password: String): Json =

    val checkPass = checkPassword(login, passHash(password))

    val checkLogin = loginCheck(login)

    if checkLogin._2 then
      if checkPass._2 then
        PersonQuery.getPersonInfo(login) match
          case Left(value) =>
            json"""{"personId": ${value.id}, "personLogin":  ${value
                .login}, "createdData": ${value.createdAt},
              "personEmail":${value
                .email}, "personAge": ${value.age}, "personAvatar": ${value
                .avatarPath}, "personRole": ${value.role}, "xxxContent": ${value
                .xxxContent}  }"""
          case Right(value) => json"""{"getInfoError":  $value }"""
      else json"""{"authError": ${checkPass._1} }"""
    else json"""{"authError": ${checkLogin._1}}"""

  private def loginCheck(login: String): (String, Boolean) =
    try
      val url = "jdbc:mysql://127.0.0.1/animeHub"
      val username = "root"
      val password = ",tkstudjplbrb"
      Class.forName("com.mysql.cj.jdbc.Driver")
      val connection = DriverManager.getConnection(url, username, password)

      // Создаем объект Statement и выполняем запрос
      val query = "SELECT id, login FROM user WHERE login = ?"
      val preparedStatement = connection.prepareStatement(query)
      preparedStatement.setString(1, login)
      val resultSet = preparedStatement.executeQuery()

      val message =
        if resultSet.next() && resultSet.getInt(1) > 0 then ("", true)
        else ("Пользователь с таким логином не зарегистрирован", false)
      message
    catch
      case ex: SQLException => (
          "Ошибка при проверке логина во время авторизации " + ex.getMessage,
          false,
        )

  private def passHash(password: String): String =
    val md = MessageDigest.getInstance("SHA-256")
    val bytes = md.digest(password.getBytes("UTF-8"))
    bytes.map("%02x".format(_)).mkString

  private def checkPassword(
      login: String,
      passHash: String,
  ): (String, Boolean) =
    try
      val url = "jdbc:mysql://127.0.0.1/animeHub"
      val username = "root"
      val password = ",tkstudjplbrb"
      Class.forName("com.mysql.cj.jdbc.Driver")
      val connection = DriverManager.getConnection(url, username, password)

      // Создаем объект Statement и выполняем запрос
      val query = "SELECT login, password_hash FROM user WHERE login = ?"
      val preparedStatement = connection.prepareStatement(
        query,
        ResultSet.TYPE_SCROLL_INSENSITIVE,
        ResultSet.CONCUR_READ_ONLY,
      )
      preparedStatement.setString(1, login)
      val resultSet = preparedStatement.executeQuery()

      resultSet.last()
      val count = resultSet.getRow >= 2
      resultSet.beforeFirst()

      val message =
        if resultSet.next() && count then
          (
            "Не знаю как но пользователей с таким логином двое обратитесь к администрации",
            false,
          )
        else
          val passHashFromDb = resultSet.getString("password_hash")
          if passHash == passHashFromDb then ("", true)
          else ("Пароль не верный", false)
      message
    catch
      case ex: SQLException =>
        ("Ошибка при проверке пароля " + ex.getMessage, false)
