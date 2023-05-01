package services

import io.circe.Json
import io.circe.literal.json
import services.ServiceList.User

import java.sql.DriverManager

class Validation {
  case class RegUser(id: Int, login: String, password: String, age: Int, photo: Option[String])
  case class validationInfo(passwordError:List[String], loginError:List[String])
  def checkValidation(user: User): (validationInfo, Boolean) = {
    
    val passwordCheck = passwordChecker(user)
    val loginChek = loginCheker(user)
    
    val validation = validationInfo(
      passwordCheck._1,
      loginChek._1
    )
    val successful = if(passwordCheck._2 && loginChek._2){
      true
    }else{
      false
    }
    (validation, successful)
  }

    def passwordChecker(user:User): (List[String], Boolean) =
      
      var errorList = List.empty[String]
      
      if(user.password != user.passwordRepeat)
        errorList = errorList :+ "Пароли не совпадают"
        
      if (user.password == "" || user.password.isEmpty )
        errorList = errorList :+ "Пустое значение"
        
      if(user.password.length < 7)
        errorList = errorList :+ "Пароль должен быть длиннее 7 символов"
        
      if(errorList.isEmpty) 
        (errorList, true)
      else
        (errorList, false)
  
  def loginCheker(user:User): (List[String], Boolean)=
    var errorList = List.empty[String]
    
    if(user.login.length < 6)
      errorList = errorList :+ "Логин должен быть длиннее 6 символов"

    if (errorList.isEmpty)
      (errorList, true)
    else
      (errorList, false)
//  def doRegistration(user:User): Json = {
//
//    var objectList = List.empty[RegUser]
//    val url = "jdbc:mysql://127.0.0.1/animeHub"
//    val username = "root"
//    val password = ""
//    Class.forName("com.mysql.cj.jdbc.Driver")
//    val connection = DriverManager.getConnection(url, username, password)
//
//    // Создаем объект Statement и выполняем запрос
//    val statement = connection.createStatement()
//    val resultSet = statement.executeQuery("SELECT id, login, password, age, photo FROM users")
//
//    while (resultSet.next) {
//
//      val item:RegUser = RegUser(
//          resultSet.getInt("id"),
//          resultSet.getString("login"),
//          resultSet.getString("password"),
//          resultSet.getInt("age"),
//          Some(resultSet.getString("photo"))
//      )
//      objectList = objectList :+ item
//    }
//    regMessage(objectList)
//    }
//  def regMessage(listPeople:List[RegUser]):Json={
//    json"""{"login": ${listPeople.head.login}, "password":  ${listPeople.head.password}}"""
//  }

}