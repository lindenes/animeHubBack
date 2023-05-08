package data.sqlquery

import java.sql.{DriverManager, SQLException}

object PersonQuery {

  case class UserInfo(id:Int, createdAt:String, login:String, email:String, age:Int, avatarPath:String, role:Int)
  def getPersonInfo(login:String):Either[UserInfo, String] =
    try {
      val url = "jdbc:mysql://127.0.0.1/animeHub"
      val username = "root"
      val password = ""
      Class.forName("com.mysql.cj.jdbc.Driver")
      val connection = DriverManager.getConnection(url, username, password)

      // Создаем объект Statement и выполняем запрос
      val query = "SELECT * FROM user WHERE login = ?"
      val preparedStatement = connection.prepareStatement(query)
      preparedStatement.setString(1, login)
      val resultSet = preparedStatement.executeQuery()

      if( resultSet.next() ){
        Left(
          UserInfo(
            resultSet.getInt("id"),
            resultSet.getString("created_at"),
            resultSet.getString("login"),
            resultSet.getString("email"),
            resultSet.getInt("age"),
            resultSet.getString("avatar_path"),
            resultSet.getInt("role")
          )
        )
      }else{
        Right("Запись не найдена")
      }
    } catch {
      case ex: SQLException => Right("Ошибка при проверке логина во время авторизации " + ex.getMessage)
    }

}
