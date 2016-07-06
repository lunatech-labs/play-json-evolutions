package com.lunatech.play.json

import play.api.libs.json._

object evolutions {

  type Transform = Reads[_ <: JsObject]
  type Evolution = (Int, Transform)

  implicit class RichFormat[T](format: OFormat[T]) {

    def withEvolutions(evolutions: Evolution*): OFormat[T] =
      if (evolutions.isEmpty) format
      else new OFormat[T] {
        val newest = evolutions.map { case (i, _) => i }.max

        override def writes(o: T) = writeVersion(format.writes(o), newest)
        override def reads(json: JsValue) = upgrade(json, evolutions) flatMap { format.reads }
      }

    private def writeVersion(json: JsValue, version: Int) = json match {
      case o: JsObject => o + ("_version" -> JsNumber(version))
      case other => sys.error("Oh noes!") // TODO, better error
    }

    private def upgrade(json: JsValue, evolutions: Seq[Evolution]): JsResult[JsValue] =
      for {
        jsonVersion <- (json \ "_version").validateOpt[Int]
        transforms <- applicableTransforms(evolutions, jsonVersion)
        upgraded <- applyTransforms(json, transforms)
      } yield upgraded

    private def applicableTransforms(evolutions: Seq[Evolution], jsonVersion: Option[Int]): JsResult[Seq[Transform]] = {
      val documentVersion = jsonVersion getOrElse 0
      JsSuccess(evolutions collect { case (nr, evolution) if nr > documentVersion => evolution })
    }

    private def applyTransforms(json: JsValue, transforms: Seq[Transform]): JsResult[JsValue] =
      transforms.foldLeft[JsResult[JsValue]](JsSuccess(json)) {
        case (json, transform) => json flatMap { _.transform(transform) }
      }

  }

}