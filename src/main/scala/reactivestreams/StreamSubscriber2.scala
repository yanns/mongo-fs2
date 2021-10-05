package reactivestreams

import cats.effect.IO
import cats.effect.std.Queue
import fs2._
import org.reactivestreams.{Publisher, Subscriber, Subscription}

import java.util.concurrent.atomic.AtomicReference
import scala.collection.mutable

final class StreamSubscriber2()

object StreamSubscriber2 {
  def fromPublisher[A](
      p: Publisher[A],
      bufferSize: Long
  ): IO[Stream[IO, Chunk[A]]] = {
    for {
      queue <- Queue.unbounded[IO, Option[Chunk[A]]]
      subscriber = new Subscriber[A] {
        var subscription: Subscription = _
        val buffer = new AtomicReference[mutable.Buffer[A]]()
        override def onSubscribe(s: Subscription): Unit = {
          println("onSubscribe")
          subscription = s
          buffer.set(mutable.Buffer.empty)
          s.request(bufferSize)
        }
        override def onNext(t: A): Unit = {
          import cats.effect.unsafe.implicits.global
          println(s"onNext $t")
          val current = buffer.get()
          if (current.size == bufferSize - 1) {
            val chunk = Chunk.buffer(current.append(t))
            queue.offer(Some(chunk)).unsafeRunSync()
            buffer.set(mutable.Buffer.empty)
            subscription.request(bufferSize)
          } else {
            buffer.set(current.append(t))
          }
        }
        override def onError(t: Throwable): Unit = {
          println("onError")
          t.printStackTrace()
        }

        override def onComplete(): Unit = {
          import cats.effect.unsafe.implicits.global
          println("onComplete")
          val current = buffer.get()
          if (current.nonEmpty) {
            val chunk = Chunk.buffer(current)
            queue.offer(Some(chunk)).unsafeRunSync()
          }
          queue.offer(None).unsafeRunSync()
        }
      }
    } yield {
      p.subscribe(subscriber)
      Stream.fromQueueNoneTerminated(queue)
    }
  }
}
