package com.chrisluttazi.hasher

import org.apache.pekko.actor.ActorRef
import org.apache.pekko.http.scaladsl.marshalling.Marshal
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json._

class HashRoutesSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with ScalatestRouteTest
    with HashRoutes {
  override val hashActor: ActorRef =
    system.actorOf(HashActor.props, "hashRegistry")

  lazy val routes: Route = hashRoutes
  "HashRoutes" should {
    "be able to hash strings (POST /hash)" in {
      val hashRequest = HashRequest("")
      val hashRequestEntity = Marshal(hashRequest).to[MessageEntity].futureValue
      val request = Post("/hash").withEntity(hashRequestEntity)
      request ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        // JSON object member order is not significant (and changed between
        // Scala 2.12 and 2.13 spray-json output); compare parsed values.
        entityAs[String].parseJson should ===("""{"original":"","hashed":""}""".parseJson)
      }
    }
  }
}
