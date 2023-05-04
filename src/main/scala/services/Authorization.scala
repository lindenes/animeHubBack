package services

import io.circe.Json
import io.circe.literal.json

import java.security.MessageDigest
import java.sql.{DriverManager, ResultSet, SQLException}
object Authorization {

  def getAuth(login:String, password:String):Json =
    val checkPass = checkPassword(login, passHash(password) )
    val checkLogin = loginCheck(login)
    if(checkLogin._2){
      if (checkPass._2) {
        json"""{"personInfo":  "а откуда информейшн"}"""
      } else {
        json"""{"authError": ${checkPass._1} }"""
      }
    }else{
      json"""{"authError": ${checkLogin._1}}"""
    }
  
  def loginCheck(login:String):(String, Boolean) =
    try {
      val url = "jdbc:mysql://127.0.0.1/animeHub"
      val username = "root"
      val password = ""
      Class.forName("com.mysql.cj.jdbc.Driver")
      val connection = DriverManager.getConnection(url, username, password)

      // Создаем объект Statement и выполняем запрос
      val query = "SELECT id, login FROM user WHERE login = ?"
      val preparedStatement = connection.prepareStatement(query)
      preparedStatement.setString(1, login)
      val resultSet = preparedStatement.executeQuery()

      val message = if (resultSet.next() && resultSet.getInt(1) > 0) {
        ("", true)
      } else {
        ("Пользователь с таким логином не зарегистрирован", false)
      }
      message
    } catch {
      case ex: SQLException => ("Ошибка при проверке логина во время авторизации " + ex.getMessage, false)
    }
  def passHash(password:String):String =
    val md = MessageDigest.getInstance("SHA-256")
    val bytes = md.digest(password.getBytes("UTF-8"))
    bytes.map("%02x".format(_)).mkString

  def checkPassword(login:String,passHash:String):(String, Boolean) =
    try {
      val url = "jdbc:mysql://127.0.0.1/animeHub"
      val username = "root"
      val password = ""
      Class.forName("com.mysql.cj.jdbc.Driver")
      val connection = DriverManager.getConnection(url, username, password)

      // Создаем объект Statement и выполняем запрос
      val query = "SELECT login, password_hash FROM user WHERE login = ?"
      val preparedStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY )
      preparedStatement.setString(1, login)
      val resultSet = preparedStatement.executeQuery()
      
      resultSet.last()
      val count = resultSet.getRow  >= 2
      resultSet.beforeFirst()
      
      val message = if (resultSet.next() && count) {
        ("Не знаю как но пользователей с таким логином двое обратитесь к администрации", false)
      } else {
        val passHashFromDb = resultSet.getString("password_hash")
        if(passHash == passHashFromDb){
          ("", true)
        }else{
          ("Пароль не верный", false)
        }
      }
      message
    }
    catch {
      case ex: SQLException => ("Ошибка при проверке пароля " + ex.getMessage, false)
    }

}
