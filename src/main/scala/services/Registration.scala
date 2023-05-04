package services

import services.ServiceList.User
import io.circe.Json
import io.circe.literal.json

import java.security.MessageDigest
import java.sql.{Connection, DriverManager, SQLException}
object Registration {
    def addNewUser(user:User, validationSuccess: Boolean): Json = {

      val loginRegistrationError = checkLoginRegistration(user)
      val passwordRegistrationError = checkPasswordRegistration(user)

      if(validationSuccess && loginRegistrationError._2 && passwordRegistrationError._2){
        val userRegInfo = add(user)
          userRegInfo.fold(
            valueA => json"""{"registrationError" : "" }""",
            valueB => json"""{"registrationError" : ${valueB._1} }"""
          )
      }else{
        json"""{"registrationLoginError" : ${loginRegistrationError._1}, "registrationMailError": ${passwordRegistrationError._1} }"""
      }

    }

  def checkLoginRegistration(user: User): (String, Boolean) =
    try {
      val url = "jdbc:mysql://127.0.0.1/animeHub"
      val username = "root"
      val password = ""
      Class.forName("com.mysql.cj.jdbc.Driver")
      val connection = DriverManager.getConnection(url, username, password)

      // Создаем объект Statement и выполняем запрос
      val query = "SELECT id, login FROM user WHERE login = ?"
      val preparedStatement = connection.prepareStatement(query)
      preparedStatement.setString(1, user.login)
      val resultSet = preparedStatement.executeQuery()

      val message = if (resultSet.next() && resultSet.getInt(1) > 0) {
        ("Пользователь с таким логином уже зарегистрирован", false)
      } else {
        ("", true)
      }
      message
    } catch {
      case ex: SQLException => ("Ошибка при проверке логина во время регистрации " + ex.getMessage, false)
    }

  def checkPasswordRegistration(user:User): (String,Boolean) =
    try{
      val url = "jdbc:mysql://127.0.0.1/animeHub"
      val username = "root"
      val password = ""
      Class.forName("com.mysql.cj.jdbc.Driver")
      val connection = DriverManager.getConnection(url, username, password)

      // Создаем объект Statement и выполняем запрос
      val query = "SELECT id, login, email FROM user WHERE email = ?"
      val preparedStatement = connection.prepareStatement(query)
      preparedStatement.setString(1, user.email)
      val resultSet = preparedStatement.executeQuery()

      val message = if (resultSet.next() && resultSet.getInt(1) > 0) {
        ("Пользователь с такой почтой уже зарегистрирован", false)
      } else {
        ("", true)
      }
      message
    }
    catch {
      case ex: SQLException => ("Ошибка при проверке почты во время регистарции " + ex.getMessage, false)
    }

    def add(user:User):Either[Unit, (String, Boolean)] = {
      try {
        val url = "jdbc:mysql://127.0.0.1/animeHub"
        val username = "root"
        val password = ""
        Class.forName("com.mysql.cj.jdbc.Driver")
        val connection = DriverManager.getConnection(url, username, password)
        val sql = "INSERT INTO user (login, email, password_hash, age, avatar_path, role) VALUES (?, ?, ?, ? ,?, ?)"
        val preparedStatement = connection.prepareStatement(sql)

        preparedStatement.setString(1, user.login)
        preparedStatement.setString(2, user.email)
        preparedStatement.setString(3, passHash(user.password) )
        preparedStatement.setInt(4, user.age.getOrElse(0) )
        preparedStatement.setString(5, "" )
        preparedStatement.setInt(6, 0)

        preparedStatement.executeUpdate()

        Left( preparedStatement.close() )
      }
      catch {
        case ex: SQLException => Right( ("Ошибка при добавлении пользователя в бд " + ex.getMessage, false) )
      }

    }
  def passHash(password:String):String={
    val md = MessageDigest.getInstance("SHA-256")
    val bytes = md.digest(password.getBytes("UTF-8"))
    bytes.map("%02x".format(_)).mkString
  }
}