package data.sqlquery

import cats.effect.IO
import data.PersonRole

import java.sql.{DriverManager, SQLException}
import scala.concurrent.ExecutionContext.Implicits.global
import doobie.*
import doobie.implicits.*
import doobie.util.ExecutionContexts
import doobie.util.fragments
import io.circe.Json
import io.circe.literal.json
import io.circe.syntax.*
import data.sqlquery.PersonQuery.UserInfo
object PersonQuery {

  case class UserInfo(id:Int, createdAt:String, login:String, email:String, age:Int, avatarPath:String, role:Int, xxxContent:Int)
  def getPersonInfo(login:String):Either[UserInfo, String] =
    try {
      val url = "jdbc:mysql://127.0.0.1/animeHub"
      val username = "root"
      val password = "cegthuthjq"
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
            resultSet.getInt("role"),
            resultSet.getInt("xxx_content")
          )
        )
      }else{
        Right("Запись не найдена")
      }
    } catch {
      case ex: SQLException => Right("Ошибка при проверке логина во время авторизации " + ex.getMessage)
    }

  def getCurrentUser(personId: Int ):IO[Option[UserInfo]] =

    val xa = Transactor.fromDriverManager[IO](
      "com.mysql.cj.jdbc.Driver",
      "jdbc:mysql://127.0.0.1/animeHub",
      "root",
      "cegthuthjq",
    )

    sql"SELECT id, created_at, login, email, age, avatar_path, role, xxx_content FROM `user` WHERE id = $personId"
    .query[UserInfo]
    .option
    .transact(xa)
    .map{
      _.orElse(None)
    }
    .handleErrorWith { error =>
      println(s"An error occurred while retrieving user: ${error.getMessage}")
      IO.raiseError(error)
    }

  def getPersonList(personId: Int): IO[List[Json]] =

    getCurrentUser( personId ).flatMap { user =>

      if user.nonEmpty && (user.get.role == PersonRole.Admin.id) then

        val xa = Transactor.fromDriverManager[IO](
          "com.mysql.cj.jdbc.Driver",
          "jdbc:mysql://127.0.0.1/animeHub",
          "root",
          "cegthuthjq",
        )

        sql"SELECT id, created_at, login, email, age, avatar_path, role, xxx_content FROM `user`"
          .query[UserInfo]
          .to[List]
          .transact(xa)
          .map(users =>
            users.map { user =>
              json"""{"personId": ${user.id}, "createdAt": ${user.createdAt}, "login": ${user.login}, "email":  ${user.email},
                    "age":  ${user.age}, "avatarPath":  ${user.avatarPath}, "role":  ${user.role}, "xxxContent":  ${user.xxxContent}}"""
            }

          )
          .handleErrorWith(ex => IO.pure(List.empty[Json]))
      else
        IO.pure(List(json"""{"error": "Вы не администратор, досвидание"}"""))

    }.handleErrorWith(ex =>
      IO.raiseError(ex)
    )

  def getPersonList(personId: Int,loginSearchValue: String): IO[List[Json]] =

    getCurrentUser( personId ).flatMap{ user =>

      if user.nonEmpty && (user.get.role == PersonRole.Admin.id) then

        val xa = Transactor.fromDriverManager[IO](
          "com.mysql.cj.jdbc.Driver",
          "jdbc:mysql://127.0.0.1/animeHub",
          "root",
          "cegthuthjq",
        )
        val searchLogin = loginSearchValue + "%"

        sql"SELECT id, created_at, login, email, age, avatar_path, role, xxx_content FROM `user` WHERE `title` LIKE $searchLogin LIMIT 10"
          .query[UserInfo]
          .to[List]
          .transact(xa)
          .map { users =>
            users.map(user =>
              json"""{"personId": ${user.id}, "createdAt": ${user.createdAt}, "login": ${user.login}, "email":  ${user.email},
                    "age":  ${user.age}, "avatarPath":  ${user.avatarPath}, "role":  ${user.role}, "xxxContent":  ${user.xxxContent}}"""
            )
          }
          .handleErrorWith(e => IO.pure(List.empty[Json]))
      else
        IO.pure(List(json"""{"error": "Вы не администратор, досвидание"}"""))

    }

  def updateRole(personId: Int, role:Int):IO[Json] =

    val xa = Transactor.fromDriverManager[IO](
      "com.mysql.cj.jdbc.Driver",
      "jdbc:mysql://127.0.0.1/animeHub",
      "root",
      "cegthuthjq",
    )

    sql"UPDATE `person` SET role = $role WHERE id = $personId"
      .update
      .run
      .transact(xa)
      .flatMap { rowsUpdated =>
        if rowsUpdated > 0 then
          IO.pure(json"""{"success":  "Роль обновлена"}""")
        else
          IO.pure(json"""{"success":  "Не получилось обновить роль"}""")
      }


}
