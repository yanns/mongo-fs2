package mongo_fs2

import cats.effect.IO
import cats.effect.kernel.Resource
import com.mongodb.reactivestreams.client.MongoClients

object MongoClient {

  def mongoClient: Resource[IO, org.mongodb.scala.MongoClient] =
    Resource.fromAutoCloseable(IO {
      org.mongodb.scala.MongoClient()
    })

  def reactiveClient
      : Resource[IO, com.mongodb.reactivestreams.client.MongoClient] = {
    Resource.fromAutoCloseable(IO {
      MongoClients.create()
    })
  }
}
