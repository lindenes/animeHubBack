package services

import services.ServiceList.User
import io.circe.Json
import io.circe.literal.json
import java.sql.DriverManager
import java.sql.Connection
object Registration {
    def addNewUser(user:User, validationSucces: Boolean): Json = {
      val checkRegistrationError = checkRegistration(user)
      if(validationSucces && checkRegistrationError._2){
        add(user)
      }
      json"""{"registrationError" : ${checkRegistrationError._1} }"""
    }
    def checkRegistration(user:User):(String, Boolean) = {
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

        val message = if(resultSet.next() && resultSet.getInt(1) > 0){
          ("Пользователь с таким логином уже зарегистрирован", false)
        }else{
          ("", true)
        }
      message
    }

    def add(user:User):Unit ={
      val url = "jdbc:mysql://127.0.0.1/animeHub"
      val username = "root"
      val password = ""
      Class.forName("com.mysql.cj.jdbc.Driver")
      val connection = DriverManager.getConnection(url, username, password)
      val sql = "INSERT INTO user (login, password) VALUES (?, ?)"
      val preparedStatement = connection.prepareStatement(sql)

      preparedStatement.setString(1, user.login) // установить значение первого параметра (id) на 123
      preparedStatement.setString(2, user.password) // установить значение второго параметра (login) на "john"

      preparedStatement.executeUpdate()

      preparedStatement.close()
      preparedStatement.close()
    }
}
