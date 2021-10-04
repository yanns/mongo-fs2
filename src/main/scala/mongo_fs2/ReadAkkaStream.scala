package mongo_fs2

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import cats.effect.{IO, IOApp, Resource}
import com.mongodb.reactivestreams.client.MongoCollection
import org.bson.Document

import scala.concurrent.ExecutionContext

object ReadAkkaStream extends IOApp.Simple {

  private val actorSystemR: Resource[IO, ActorSystem] =
    Resource.make(
      IO(ActorSystem())
    )(sys => IO.fromFuture(IO(sys.terminate())).as(()))

  override def run: IO[Unit] = actorSystemR.use { implicit system =>
    MongoClient.reactiveClient.use { client =>
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
  }

  private def readAllDocuments(
      collection: MongoCollection[Document]
  )(implicit system: ActorSystem): IO[Long] = {
    val findPublisher = DebugRX.readAllDocuments(collection)

    IO.fromFuture(
      IO(
        Source
          .fromPublisher(findPublisher)
          .map(_ => 1)
          .runFold(0L)(_ + _)
      )
    )
  }
}
