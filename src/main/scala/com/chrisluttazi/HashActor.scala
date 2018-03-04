package com.chrisluttazi

import akka.actor.{ Actor, ActorLogging, Props }

final case class HashRequest(body: String)

object HashActor {

  final case class ActionPerformed(original: String, hashed: String)

  final case class Hash(body: String)

  def props: Props = Props[HashActor]
}

class HashActor extends Actor with ActorLogging {

  import HashActor._

  def receive: Receive = {
    case Hash(string) =>
      sender() ! ActionPerformed(s"$string", s"$string")
  }
}
