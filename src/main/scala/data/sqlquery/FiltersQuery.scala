package data.sqlquery

import cats.effect.{IO, IOApp}
import doobie.*
import doobie.implicits.*
import doobie.util.ExecutionContexts
import io.circe.Json
import io.circe.literal.json
object FiltersQuery {

  case class Type(id:Int, name:String)

  case class Genre(id:Int, name:String)
  def getFilterList:IO[Json] =
    getTypes.flatMap { types =>
      getGenre.map(genres =>
        types.deepMerge(genres)
      )
    }
  def getTypes:IO[Json] =

    val xa = Transactor.fromDriverManager[IO](
      "com.mysql.cj.jdbc.Driver",
      "jdbc:mysql://127.0.0.1/animeHub",
      "root",
      ",tkstudjplbrb",
    )

    sql"SELECT * FROM `type`"
      .query[Type]
      .to[List]
      .transact(xa)
      .map( types => json"""{"typeList": ${
        types.map( value =>
          json"""{ "typeId": ${value.id}, "typeName":  ${value.name} }"""
        )
      }}"""
      )

  def getGenre:IO[Json] =

    val xa = Transactor.fromDriverManager[IO](
      "com.mysql.cj.jdbc.Driver",
      "jdbc:mysql://127.0.0.1/animeHub",
      "root",
      ",tkstudjplbrb",
    )

    sql"SELECT * FROM `genre`"
      .query[Genre]
      .to[List]
      .transact(xa)
      .map(genres =>
        json"""{"genreList": ${
          genres.map(value =>
            json"""{ "genreId": ${value.id}, "genreName":  ${value.name} }"""
          )
        }}"""
      )

}
