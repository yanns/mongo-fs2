package mongo_fs2

import cats.effect.IO
import cats.effect.kernel.Resource

object MongoClient {

  def mongoClient: Resource[IO, org.mongodb.scala.MongoClient] =
    Resource.make(IO(org.mongodb.scala.MongoClient()))(client =>
      IO(client.close())
    )

}
