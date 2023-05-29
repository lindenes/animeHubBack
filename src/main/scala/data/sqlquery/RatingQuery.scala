package data.sqlquery

import cats.effect.IO
import io.circe.Json
import io.circe.literal.json
import doobie.*
import doobie.implicits.*
import doobie.util.ExecutionContexts
import doobie.util.fragments

object RatingQuery {

  val xa = Transactor.fromDriverManager[IO](
    "com.mysql.cj.jdbc.Driver",
    "jdbc:mysql://127.0.0.1/animeHub",
    "root",
    ",tkstudjplbrb"
  )
  def setRating(postId: Int, rating: Int, personId: Int): IO[Json] =

    for {
      _ <- sql"UPDATE `post` SET rating_count = rating_count + $rating, people_count_like = people_count_like + 1 WHERE id = $postId"
        .update
        .run
        .transact(xa)
        .attemptSql
      _ <- sql"INSERT INTO user_rating_post (user_id, post_id, rating) VALUES ($personId, $postId, $rating)"
        .update
        .run
        .transact(xa)
        .attemptSql
    } yield json"""{"success": "true"}"""

  def getPersonPostRating(personId: Int, postId: Int): IO[Json] =

    sql"SELECT rating FROM `user_rating_post` WHERE post_id = $postId AND user_id = $personId"
      .query[Int]
      .option
      .transact(xa)
      .map {
        case Some(rating) => json"""{"rating": $rating}"""
        case None => json"""{"rating":  ""}"""
      }

  def updatePersonPostRating(personId:Int, postId:Int, rating:Int):IO[Json] =

    for {
      personRating <- sql"SELECT rating FROM `user_rating_post` WHERE user_id = $personId AND post_id = $postId"
        .query[Int]
        .option
        .transact(xa)
      _ <- sql"UPDATE `user_rating_post` SET rating = $rating WHERE post_id = $postId AND user_id = $personId"
        .update
        .run
        .transact(xa)
        .attemptSql
      _ <- sql"UPDATE `post` SET rating = rating - $personRating + $rating WHERE id=$postId"
        .update
        .run
        .transact(xa)
        .attemptSql
    }yield json"""{"success":  "true"}"""
}
