package services

import io.circe.Json
import io.circe.literal.json
import services.ServiceList.User
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import java.sql.DriverManager

class Validation {
  //case class RegUser(id: Int, login: String, password: String, age: Int, photo: Option[String])
  case class validationInfo(passwordError:List[String], loginError:List[String], ageError:List[String], mailError:List[String])
  def checkValidation(user: User): (Json, Boolean) = {
 
    val passwordCheck = passwordChecker(user)
    val loginCheck = loginChecker(user)
    val ageCheck = ageChecker(user)
    val mailCheck = mailChecker(user)
    
    val validation = validationInfo(
      passwordCheck._1,
      loginCheck._1,
      ageCheck._1,
      mailCheck._1
    )
    val successful = passwordCheck._2 && loginCheck._2 && ageCheck._2 && mailCheck._2

    ( json"""{"passwordError": ${validation.passwordError}, "loginError": ${validation.loginError}, 
          "mailError": ${validation.mailError}, "ageError":  ${validation.ageError}}""", successful )
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
  
  def loginChecker(user:User): (List[String], Boolean)=
    var errorList = List.empty[String]
    
    if(user.login.length < 6)
      errorList = errorList :+ "Логин должен быть длиннее 6 символов"

    if (errorList.isEmpty)
      (errorList, true)
    else
      (errorList, false)
  def ageChecker(user:User): (List[String], Boolean)=

    var errorList = List.empty[String]

    if(user.age.get != ""){
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val date = LocalDate.parse(user.age.getOrElse(""), formatter)
      val currentDate = LocalDate.now()

      val age = currentDate.getYear - date.getYear

      if (age > 100 || age < 7) {
        errorList = errorList :+ "Ваш возраст не может быть больше 100 или меньше 7"
      }

      if (errorList.isEmpty) {
        (errorList, true)
      } else {
        (errorList, false)
      }
    }else{
      errorList = errorList :+ "Введите возраст"
      (errorList, false)
    }

  def mailChecker(user:User):(List[String], Boolean)=
    var errorList = List.empty[String]

    val emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$".r
    emailRegex.findFirstMatchIn(user.email) match {
      case Some(_) =>
        (errorList,true)
      case None =>
        errorList = errorList :+ "Вы ввели некоректный email"
        (errorList, false)
    }


}