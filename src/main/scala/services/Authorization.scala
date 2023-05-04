package services

import io.circe.Json
import io.circe.literal.json

import java.security.MessageDigest
import java.sql.{DriverManager, SQLException}
object Authorization {

  def getAuth(login:String, password:String):Json =
    val checkPass = checkPassword(login, passHash(password) )
    if(checkPass._2){
       json"""{"personInfo":  "а откуда информейшн"}""" 
    }else{
      json"""{"authError": ${checkPass._1} }"""
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
      val query = "SELECT login, password FROM user WHERE login = ?"
      val preparedStatement = connection.prepareStatement(query)
      preparedStatement.setString(1, login)
      val resultSet = preparedStatement.executeQuery()

      val message = if (resultSet.getInt(1) >= 2) {
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
