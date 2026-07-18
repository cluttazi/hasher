package com.chrisluttazi.hasher

import org.apache.pekko.actor.{ActorRef, ActorSystem}
import org.apache.pekko.event.{Logging, LoggingAdapter}
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.pattern.ask
import org.apache.pekko.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

import com.chrisluttazi.hasher.HashActor._

trait HashRoutes extends JsonSupport {
  implicit def system: ActorSystem

  lazy val log: LoggingAdapter = Logging(system, classOf[HashRoutes])

  def hashActor: ActorRef

  // usually we'd obtain the timeout from the system's configuration
  implicit lazy val timeout: Timeout = Timeout(5.seconds)

  lazy val hashRoutes: Route =
    pathPrefix("hash") {
      pathEnd {
        post {
          entity(as[HashRequest]) { hashRequest =>
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
