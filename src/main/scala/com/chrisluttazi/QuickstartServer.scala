package com.chrisluttazi

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

object QuickstartServer extends App with HashRoutes {

  implicit val system: ActorSystem = ActorSystem("hashHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val hashActor: ActorRef = system.actorOf(HashActor.props, "hashActor")

  lazy val routes: Route = hashRoutes
  Http().bindAndHandle(routes, "localhost", 8080)
  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
}
