package com.luttazi

import com.luttazi.HashActor.ActionPerformed

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {

  import DefaultJsonProtocol._

  implicit val hashJsonFormat = jsonFormat1(HashRequest)

  implicit val actionPerformedJsonFormat = jsonFormat2(ActionPerformed)
}
