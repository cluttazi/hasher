package com.chrisluttazi.hasher

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import org.apache.pekko.actor.{ ActorRef, ActorSystem }
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.server.Route

object QuickstartServer extends App with HashRoutes {

  implicit val system: ActorSystem = ActorSystem("hashHttpServer")

  val hashActor: ActorRef = system.actorOf(HashActor.props, "hashActor")

  lazy val routes: Route = hashRoutes
  Http().newServerAt("localhost", 8080).bind(routes)
  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
}
