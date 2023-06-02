package data.sqlquery

import _root_.data.PersonRole
import cats.effect.IO
import doobie.*
import doobie.implicits.*
import io.circe.literal.json
import io.circe.Json
import java.sql.DriverManager
import java.sql.SQLException

object PersonQuery:

  private val xa = Transactor.fromDriverManager[IO](
    "com.mysql.cj.jdbc.Driver",
    "jdbc:mysql://127.0.0.1/animeHub",
    "root",
    ",tkstudjplbrb",
  )

  case class UserInfo(
      id: Int,
      createdAt: String,
      login: String,
      email: String,
      age: Int,
      avatarPath: String,
      role: Int,
      xxxContent: Int,
  )

  def getPersonInfo(login: String): Either[UserInfo, String] =
    try
      val url = "jdbc:mysql://127.0.0.1/animeHub"
      val username = "root"
      val password = ",tkstudjplbrb"
      Class.forName("com.mysql.cj.jdbc.Driver")
      val connection = DriverManager.getConnection(url, username, password)

      val query = "SELECT * FROM user WHERE login = ?"
      val preparedStatement = connection.prepareStatement(query)
      preparedStatement.setString(1, login)
      val resultSet = preparedStatement.executeQuery()

      if resultSet.next() then
        Left(UserInfo(
          resultSet.getInt("id"),
          resultSet.getString("created_at"),
          resultSet.getString("login"),
          resultSet.getString("email"),
          resultSet.getInt("age"),
          resultSet.getString("avatar_path"),
          resultSet.getInt("role"),
          resultSet.getInt("xxx_content"),
        ))
      else Right("Record not found")
    catch case ex: SQLException => Right("Authorization error: " + ex.getMessage)

  def getCurrentUser(personId: Int): IO[Option[UserInfo]] =
    sql"SELECT id, created_at, login, email, age, avatar_path, role, xxx_content FROM `user` WHERE id = $personId"
      .query[UserInfo]
      .option
      .transact(xa)
      .map(_.orElse(None))
      .handleErrorWith { error =>
        println(s"An error occurred while retrieving user: ${error.getMessage}")
        IO.raiseError(error)
      }

  def getPersonList(personId: Int): IO[List[Json]] = getCurrentUser(personId)
    .flatMap { user =>
      if user.nonEmpty && user.get.role == PersonRole.Admin.ordinal then

        sql"SELECT id, created_at, login, email, age, avatar_path, role, xxx_content FROM `user`"
          .query[UserInfo]
          .to[List]
          .transact(xa)
          .map(users =>
            users.map { user =>
              json"""{"personId": ${user.id}, "createdAt": ${user
                  .createdAt}, "login": ${user.login}, "email":  ${user.email},
                    "age":  ${user.age}, "avatarPath":  ${user
                  .avatarPath}, "role":  ${user.role}, "xxxContent":  ${user
                  .xxxContent}}"""
            }
          )
          .handleErrorWith(ex => IO.pure(List.empty[Json]))
      else IO.pure(List(json"""{"error": "Not an admin"}"""))

    }
    .handleErrorWith(ex => IO.raiseError(ex))

  def getPersonList(personId: Int, loginSearchValue: String): IO[List[Json]] =
    getCurrentUser(personId).flatMap { user =>
      if user.nonEmpty && user.get.role == PersonRole.Admin.ordinal then

        val searchLogin = loginSearchValue + "%"

        sql"SELECT id, created_at, login, email, age, avatar_path, role, xxx_content FROM `user` WHERE `title` LIKE $searchLogin LIMIT 10"
          .query[UserInfo]
          .to[List]
          .transact(xa)
          .map { users =>
            users.map(user =>
              json"""{"personId": ${user.id}, "createdAt": ${user
                  .createdAt}, "login": ${user.login}, "email":  ${user.email},
                    "age":  ${user
                  .age}, "avatarPath":  ${user.avatarPath}, "role":  ${user
                  .role}, "xxxContent":  ${user.xxxContent}}"""
            )
          }
          .handleErrorWith(e => IO.pure(List.empty[Json]))
      else IO.pure(List(json"""{"error": "Not an admin"}"""))

    }

  def updateRole(personId: Int, role: Int): IO[Json] =
    sql"UPDATE `user` SET role = $role WHERE id = $personId"
      .update
      .run
      .transact(xa)
      .flatMap { rowsUpdated =>
        if rowsUpdated > 0 then IO.pure(json"""{"success":  "Role updated"}""")
        else IO.pure(json"""{"success":  "Failed to update the role"}""")
      }

  def updateXxxContent(personId: Int, xxxContent: Int): IO[Json] =
    sql"UPDATE `user` SET xxx_content = $xxxContent WHERE id = $personId"
      .update
      .run
      .transact(xa)
      .flatMap { rowsUpdated =>
        if rowsUpdated > 0 then IO.pure(json"""{"success":  "Role updated"}""")
        else IO.pure(json"""{"success":  "Failed to update the role"}""")
      }
