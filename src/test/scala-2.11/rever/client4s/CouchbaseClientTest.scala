package rever.client4s

import java.util.UUID

import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.{Bucket, CouchbaseCluster}
import org.scalatest.{BeforeAndAfter, FunSuite}
import Couchbase._
import scala.concurrent.duration._

import scala.concurrent.Await

/**
  * Created by tiennt4 on 20/02/2017.
  */
class CouchbaseClientTest extends FunSuite with BeforeAndAfter {

  var cluster: CouchbaseCluster = _
  var bucket: Bucket = _
  private implicit val ec = scala.concurrent.ExecutionContext.global

  private val testIdentity = UUID.randomUUID().toString

  private val doc1 =
    s"""{
       |  "id": "$testIdentity-1",
       |  "value": "${testIdentity}_1"
       |}""".stripMargin
  private val doc2 =
    s"""{
       |  "id": "$testIdentity-2",
       |  "value": "${testIdentity}_2"
       |}""".stripMargin
  private val doc3 =
    s"""{
       |  "id": "$testIdentity-3",
       |  "value": "${testIdentity}_2"
       |}""".stripMargin

  before {
    cluster = CouchbaseCluster.create("172.16.100.1")
    bucket = cluster.openBucket("userprofile", "1234")
    bucket.upsert(JsonDocument.create(s"$testIdentity-1", JsonObject.fromJson(doc1)))
    bucket.upsert(JsonDocument.create(s"$testIdentity-2", JsonObject.fromJson(doc2)))
    bucket.upsert(JsonDocument.create(s"$testIdentity-3", JsonObject.fromJson(doc3)))
  }

  test("[ASYNC] insert/get/delete document should success") {
    val json = JsonObject.empty().put("id", "1")
      .put("key", "value")
    val doc = JsonDocument.create(testIdentity,json)
    val insertResp = Await.result(bucket.async().insert(doc).toFuture(), 1 seconds)
    assert(insertResp.id().equals(testIdentity))
    val get1Resp = Await.result(bucket.async().get(doc.id()).toFuture(), 1 seconds)
    assert(get1Resp.id().equals(testIdentity))
    // Assert response's content equals to indexed's content
    assert(get1Resp.content().equals(json))
    val removeResp = Await.result(bucket.async().remove(doc.id()).toFuture(), 1 seconds)
    assert(removeResp.id().equals(testIdentity))
    assert(removeResp.content() == null)
    val get2Resp = Await.result(bucket.async().get(testIdentity).toFuture(), 1 seconds)
    assert(get2Resp == null)
  }

  after {
    bucket.remove(s"$testIdentity-1")
    bucket.remove(s"$testIdentity-2")
    bucket.remove(s"$testIdentity-3")
    bucket.close()
    cluster.disconnect()
  }

}
