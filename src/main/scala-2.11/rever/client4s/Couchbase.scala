package rever.client4s

import rx.lang.scala.JavaConversions.toScalaObservable
import rx.Observable

import scala.concurrent.{Future, Promise}

/**
  * Created by tiennt4 on 20/02/2017.
  */
object Couchbase {

  implicit class ObservableImplicits[+T](observable: Observable[T]) {
    private[this] val promise = Promise[T]

    private[client4s] def getObj = this

    def toFuture(): Future[T] = {
      toScalaObservable(observable).subscribe(
        t => promise.success(t),
        ex => promise.failure(ex),
        () => {
          if (!promise.isCompleted)
            promise.success(null.asInstanceOf[T])
        }
      )
      promise.future
    }
  }

}
