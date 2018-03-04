package com.luttazi

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.delete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path

import scala.concurrent.Future
import com.luttazi.HashActor._
import akka.pattern.ask
import akka.util.Timeout

trait HashRoutes extends JsonSupport {
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[HashRoutes])

  def hashActor: ActorRef

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  lazy val hashRoutes: Route =
    pathPrefix("hash") {
      pathEnd {
        post {
          entity(as[HashRequest]) { hashRequest: HashRequest =>
            val hashCreated: Future[ActionPerformed] =
              (hashActor ? Hash(hashRequest.body)).mapTo[ActionPerformed]
            onSuccess(hashCreated) { performed =>
              log.info("Created hash [{}]: {}", hashRequest.body, performed)
              complete((StatusCodes.Created, performed))
            }
          }
        }
      }
    }
}
