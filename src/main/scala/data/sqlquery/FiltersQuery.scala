package data.sqlquery

import cats.effect.IO
import doobie.*
import doobie.implicits.*
import io.circe.literal.json
import io.circe.Json

object FiltersQuery:

  private val xa = Transactor.fromDriverManager[IO](
    "com.mysql.cj.jdbc.Driver",
    "jdbc:mysql://127.0.0.1/animeHub",
    "root",
    ",tkstudjplbrb",
  )

  private case class Type(id: Int, name: String)

  private case class Genre(id: Int, name: String)

  def getFilterList: IO[Json] = getTypes.flatMap { types =>
    getGenre.map(genres => types.deepMerge(genres))
  }

  private def getTypes: IO[Json] = sql"SELECT * FROM `type`"
    .query[Type]
    .to[List]
    .transact(xa)
    .map(types =>
      json"""{"typeList": ${types.map(value =>
          json"""{ "typeId": ${value.id}, "typeName":  ${value.name} }"""
        )}}"""
    )

  private def getGenre: IO[Json] = sql"SELECT * FROM `genre`"
    .query[Genre]
    .to[List]
    .transact(xa)
    .map(genres =>
      json"""{"genreList": ${genres.map(value =>
          json"""{ "genreId": ${value.id}, "genreName":  ${value.name} }"""
        )}}"""
    )
