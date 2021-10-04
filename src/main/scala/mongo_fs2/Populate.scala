package mongo_fs2

import cats.effect.{IO, IOApp}
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.result.{InsertManyResult, InsertOneResult}

object Populate extends IOApp.Simple {
  val toInsert = 5_000
  val chunkSize = 100
  val outputEvery = 100

  override def run: IO[Unit] = {
    MongoClient.mongoClient.use { client =>
      val db = client.getDatabase("mydb")
      val collection = db.getCollection("test")
      for {
        _ <- IO.fromFuture(IO(db.drop().head()))
        _ <- IO.println("=".*(toInsert / outputEvery))
        inserted <- fs2.Stream
          .range(0, toInsert)
          .lift[IO]
          .mapAsync(1) { i =>
            for {
              _ <- if (i % 100 == 0) IO.print(".") else IO.unit
              _ <- insertMany(collection)(i, chunkSize)
            } yield 1L
          }
          .foldMonoid
          .compile
          .lastOrError
        count <- IO.fromFuture(IO(collection.countDocuments().head()))
        _ <- IO.println(
          s"\ninserted $count items (should: ${toInsert * chunkSize}"
        )
      } yield ()
    }
  }

  private def insertMany(
      collection: MongoCollection[Document]
  )(from: Int, nbr: Int): IO[InsertManyResult] =
    IO.fromFuture(
      IO(
        collection
          .insertMany(
            (from.to(from + nbr)).map(i => Document("name" -> s"pif-$i"))
          )
          .head()
      )
    )
}
