package mongo_fs2

import cats.effect.{IO, IOApp}
import com.mongodb.reactivestreams.client.MongoCollection
import org.bson.Document

object Read extends IOApp.Simple {
  override def run: IO[Unit] = MongoClient.reactiveClient.use { client =>
    val db = client.getDatabase("mydb")

    val collection = db.getCollection("test")
    for {
      start <- IO.monotonic
      _ <- fs2.Stream
        .range(0, 1)
        .lift[IO]
        .parEvalMap(10) { i =>
          for {
            _ <- IO.println(s"starting reading with worker $i")
            count <- readAllDocuments(collection)
            _ <- IO.println(s"working $i has read $count documents")
          } yield ()
        }
        .compile
        .drain
      end <- IO.monotonic
      _ <- IO.println(s"Time spend: ${(end - start).toSeconds} s")
    } yield ()
  }

  private def readAllDocuments(
      collection: MongoCollection[Document]
  ): IO[Long] = {
    import reactivestreams._

    val findPublisher = DebugRX.readAllDocuments(collection)

    findPublisher
      .toStream[IO](bufferSize = 421L)
      .map(_.size.toLong)
      .foldMonoid
      .compile
      .lastOrError
  }
}
