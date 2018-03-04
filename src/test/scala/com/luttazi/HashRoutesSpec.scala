package com.luttazi

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class HashRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with HashRoutes {
  override val hashRegistryActor: ActorRef =
    system.actorOf(HashActor.props, "hashRegistry")

  lazy val routes = hashRoutes
  "HashRoutes" should {
    "be able to hash strings (POST /hash)" in {
      val hashRequest = HashRequest("")
      val hashRequestEntity = Marshal(hashRequest).to[MessageEntity].futureValue
      val request = Post("/hash").withEntity(hashRequestEntity)
      request ~> routes ~> check {
        status should ===(StatusCodes.Success)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"original":"","hashed":""}""")
      }
    }
  }
}
