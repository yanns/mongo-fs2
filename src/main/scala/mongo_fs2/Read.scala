package mongo_fs2

import cats.effect.{IO, IOApp}

object Read extends IOApp.Simple {
  override def run: IO[Unit] = MongoClient.mongoClient.use { client =>
    val db = client.getDatabase("mydb")
    val collection = db.getCollection("test")
    import fs2.interop.reactivestreams._

    collection
      .find()
      .toStream[IO]
      .map(_ => 1L)
      .foldMonoid
      .compile
      .lastOrError
      .flatMap(count => IO.println(s"stream $count documents"))
  }
}
