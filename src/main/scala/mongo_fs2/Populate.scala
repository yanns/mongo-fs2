package mongo_fs2

import cats.effect.{IO, IOApp}
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.result.InsertOneResult

object Populate extends IOApp.Simple {
  val toInsert = 5_000
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
              _ <- insert(collection)(i)
            } yield 1L
          }
          .foldMonoid
          .compile
          .lastOrError
        count <- IO.fromFuture(IO(collection.countDocuments().head()))
        _ <- IO.println(s"\ninserted $count items (should: $toInsert)")
      } yield ()
    }
  }

  private def insert(
      collection: MongoCollection[Document]
  )(i: Int): IO[InsertOneResult] =
    IO.fromFuture(
      IO(collection.insertOne(Document("name" -> s"pif-$i")).head())
    )
}
