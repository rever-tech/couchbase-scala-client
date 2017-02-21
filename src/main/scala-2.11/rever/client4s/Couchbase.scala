package rever.client4s

import com.couchbase.client.java.query.core.N1qlQueryExecutor
import com.couchbase.client.java.query.{AsyncN1qlQueryResult, N1qlQueryResult}
import com.couchbase.client.java.search.result.impl.DefaultSearchQueryResult
import com.couchbase.client.java.search.result.{AsyncSearchQueryResult, SearchQueryResult}
import com.couchbase.client.java.view._
import rx.lang.scala.JavaConversions.toScalaObservable
import rx.Observable
import rx.functions.Func1

import scala.concurrent.{Future, Promise}
import scala.util.control.NonFatal

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

    def toFuture[R](f: T => R): Future[R] = {
      val tmpPromise = Promise[R]
      toScalaObservable(observable).subscribe(
        t => try {
          tmpPromise.success(f(t))
        } catch {
          case NonFatal(e) => tmpPromise.failure(e)
        },
        ex => tmpPromise.failure(ex),
        () => {
          if (!tmpPromise.isCompleted)
            tmpPromise.success(null.asInstanceOf[R])
        }
      )
      tmpPromise.future
    }
  }

  implicit class AsyncN1qlResultImplicits(asyncQueryResult: Observable[AsyncN1qlQueryResult]) {
    def toFuture(): Future[N1qlQueryResult] = {
      asyncQueryResult.flatMap[N1qlQueryResult](N1qlQueryExecutor.ASYNC_RESULT_TO_SYNC).toFuture()
    }
  }

  implicit class AsyncSearchResultImplicits(asyncSearchResult: Observable[AsyncSearchQueryResult]) {
    def toFuture(): Future[SearchQueryResult] = {
      asyncSearchResult.flatMap[SearchQueryResult](DefaultSearchQueryResult.FROM_ASYNC).toFuture()
    }
  }

  implicit class AsyncViewResultImplicits(asyncViewResult: Observable[AsyncViewResult]) {
    /**
      *
      * @return Future of DefaultViewResult with null `env` and `bucket`
      */
    def toFuture(): Future[ViewResult] = asyncViewResult.map[ViewResult](new Func1[AsyncViewResult, ViewResult]() {
      def call(asyncViewResult: AsyncViewResult): ViewResult = {
        new DefaultViewResult(null, null, asyncViewResult.rows,
          asyncViewResult.totalRows, asyncViewResult.success, asyncViewResult.error, asyncViewResult.debug)
      }
    }).toFuture()
  }

  implicit class AsyncSpatialViewResultImplicits(asyncSpatialViewResult: Observable[AsyncSpatialViewResult]) {
    /**
      *
      * @return Future of SpatialViewResult with null `env` and `bucket`
      */
    def toFuture(): Future[SpatialViewResult] = asyncSpatialViewResult.map[SpatialViewResult](new Func1[AsyncSpatialViewResult, SpatialViewResult]() {
      def call(asyncSpatialViewResult: AsyncSpatialViewResult) = new DefaultSpatialViewResult(null, null, asyncSpatialViewResult.rows, asyncSpatialViewResult.success, asyncSpatialViewResult.error, asyncSpatialViewResult.debug)
    }).toFuture()
  }
}
