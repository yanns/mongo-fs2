import fs2._
import cats.effect.kernel._
import cats.effect.std.Dispatcher
import org.reactivestreams._

/** Implementation of the reactivestreams protocol for fs2
  *
  * @example
  *   {{{ scala> import fs2._ scala> import fs2.interop.reactivestreams._ scala>
  *   import cats.effect.{IO, Resource}, cats.effect.unsafe.implicits.global
  *   scala> scala> val upstream: Stream[IO, Int] = Stream(1, 2, 3).covary[IO]
  *   scala> val publisher: Resource[IO, StreamUnicastPublisher[IO, Int]] =
  *   upstream.toUnicastPublisher scala> val downstream: Stream[IO, Int] =
  *   Stream.resource(publisher).flatMap(_.toStream[IO]) scala> scala>
  *   downstream.compile.toVector.unsafeRunSync() res0: Vector[Int] = Vector(1,
  *   2, 3) }}}
  *
  * @see
  *   [[http://www.reactive-streams.org/]]
  */
package object reactivestreams {

  /** Creates a lazy stream from an `org.reactivestreams.Publisher`.
    *
    * The publisher only receives a subscriber when the stream is run.
    */
  def fromPublisher[F[_]: Async, A](p: Publisher[A]): Stream[F, A] =
    Stream
      .eval(StreamSubscriber[F, A])
      .flatMap(s => s.sub.stream(Sync[F].delay(p.subscribe(s))))

  implicit final class PublisherOps[A](val publisher: Publisher[A])
      extends AnyVal {

    /** Creates a lazy stream from an `org.reactivestreams.Publisher` */
    def toStream[F[_]: Async]: Stream[F, A] =
      fromPublisher(publisher)
  }
}
