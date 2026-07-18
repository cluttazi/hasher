package com.chrisluttazi.hasher

import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.chrisluttazi.hasher.HashActor.ActionPerformed
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport {

  import DefaultJsonProtocol._

  implicit val hashJsonFormat: RootJsonFormat[HashRequest] =
    jsonFormat(HashRequest.apply, "body")

  implicit val actionPerformedJsonFormat: RootJsonFormat[ActionPerformed] =
    jsonFormat(ActionPerformed.apply, "original", "hashed")
}
