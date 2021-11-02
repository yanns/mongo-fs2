package mongo_fs2

import com.mongodb.{CursorType, ExplainVerbosity}
import com.mongodb.client.model.Collation
import com.mongodb.reactivestreams.client.{FindPublisher, MongoCollection}
import org.bson.Document
import org.bson.conversions.Bson
import org.reactivestreams.{Publisher, Subscriber, Subscription}

import java.util.concurrent.TimeUnit

object DebugRX {

  def debuggingPublisher(
      originalPublisher: FindPublisher[Document]
  ): FindPublisher[Document] = {
    val findPublisher = new FindPublisher[Document] {
      type D >: Document
      override def first(): Publisher[Document] = originalPublisher.first()
      override def filter(filter: Bson): FindPublisher[Document] =
        originalPublisher.filter(filter)
      override def limit(limit: Int): FindPublisher[Document] =
        originalPublisher.limit(limit)
      override def skip(skip: Int): FindPublisher[Document] =
        originalPublisher.skip(skip)
      override def maxTime(
          maxTime: Long,
          timeUnit: TimeUnit
      ): FindPublisher[Document] = originalPublisher.maxTime(maxTime, timeUnit)
      override def maxAwaitTime(
          maxAwaitTime: Long,
          timeUnit: TimeUnit
      ): FindPublisher[Document] =
        originalPublisher.maxAwaitTime(maxAwaitTime, timeUnit)
      override def projection(projection: Bson): FindPublisher[Document] =
        originalPublisher.projection(projection)
      override def sort(sort: Bson): FindPublisher[Document] =
        originalPublisher.sort(sort)
      override def noCursorTimeout(
          noCursorTimeout: Boolean
      ): FindPublisher[Document] =
        originalPublisher.noCursorTimeout(noCursorTimeout)
      override def oplogReplay(oplogReplay: Boolean): FindPublisher[Document] =
        originalPublisher.oplogReplay(oplogReplay)
      override def partial(partial: Boolean): FindPublisher[Document] =
        originalPublisher.partial(partial)
      override def cursorType(cursorType: CursorType): FindPublisher[Document] =
        originalPublisher.cursorType(cursorType)
      override def collation(collation: Collation): FindPublisher[Document] =
        originalPublisher.collation(collation)
      override def comment(comment: String): FindPublisher[Document] =
        originalPublisher.comment(comment)
      override def hint(hint: Bson): FindPublisher[Document] =
        originalPublisher.hint(hint)
      override def hintString(hint: String): FindPublisher[Document] =
        originalPublisher.hintString(hint)
      override def max(max: Bson): FindPublisher[Document] =
        originalPublisher.max(max)
      override def min(min: Bson): FindPublisher[Document] =
        originalPublisher.min(min)
      override def returnKey(returnKey: Boolean): FindPublisher[Document] =
        originalPublisher.returnKey(returnKey)
      override def showRecordId(
          showRecordId: Boolean
      ): FindPublisher[Document] = originalPublisher.showRecordId(showRecordId)
      override def batchSize(batchSize: Int): FindPublisher[Document] =
        originalPublisher.batchSize(batchSize)
      override def allowDiskUse(
          allowDiskUse: java.lang.Boolean
      ): FindPublisher[Document] = originalPublisher.allowDiskUse(allowDiskUse)
      override def explain(): Publisher[Document] = originalPublisher.explain()
      override def explain(verbosity: ExplainVerbosity): Publisher[Document] =
        originalPublisher.explain(verbosity)
      override def explain[E](explainResultClass: Class[E]): Publisher[E] =
        originalPublisher.explain(explainResultClass)
      override def explain[E](
          explainResultClass: Class[E],
          verbosity: ExplainVerbosity
      ): Publisher[E] = originalPublisher.explain(explainResultClass, verbosity)
      override def subscribe(s: Subscriber[_ >: Document]): Unit = {
        originalPublisher.subscribe(new Subscriber[Document] {
          override def onSubscribe(sub: Subscription): Unit = {
            val subscription = new Subscription {
              override def request(n: Long): Unit = {
                println(s"request $n")
                sub.request(n)
              }
              override def cancel(): Unit = sub.cancel()
            }
            s.onSubscribe(subscription)
          }
          override def onNext(t: Document): Unit = s.onNext(t)
          override def onError(t: Throwable): Unit = s.onError(t)
          override def onComplete(): Unit = s.onComplete()
        })
      }
    }

    findPublisher
  }
}
