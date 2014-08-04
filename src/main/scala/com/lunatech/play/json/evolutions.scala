package com.lunatech.play.json

import play.api.libs.json.{ Format, JsNumber, JsObject, JsResult, JsSuccess, JsValue, Reads }
import play.api.libs.json.Reads.OptionReads

object evolutions {

  type Transformation = Reads[_ <: JsObject]
  type Evolution = (Int, Transformation)

  implicit class RichFormat[T](format: Format[T]) {

    def error() = sys.error("Can only apply evolutions to `JsObject` instances")

    def withEvolutions(evolutions: Evolution*): Format[T] =
      if (evolutions.isEmpty) format
      else new Format[T] {
        val utilities = new EvolutionUtilities(evolutions)

        override def writes(o: T) = format.writes(o) match {
          case obj: JsObject => utilities writeLatestVersionTo obj
          case _ => error()
        }

        override def reads(json: JsValue) = json match {
          case obj: JsObject => utilities upgrade obj flatMap format.reads
          case _ => error()
        }
      }
  }

  class EvolutionUtilities(evolutions: Seq[Evolution]) {
    private val sortedEvolutions = evolutions.sortBy { case (version, _) => version }
    private val (latestVersion, _) = sortedEvolutions.lastOption.getOrElse(0 -> null)

    def writeLatestVersionTo(o: JsObject) =
      o + ("_version" -> JsNumber(latestVersion))

    def upgrade(json: JsObject) =
      for {
        jsonVersion <- (json \ "_version").validate[Option[Int]]
        transformations <- applicableTransformations(jsonVersion)
        upgraded <- applyTransformations(json, transformations)
      } yield upgraded

    private def applicableTransformations(jsonVersion: Option[Int]): JsResult[Seq[Transformation]] = {
      val documentVersion = jsonVersion getOrElse 0
      val applicableTransformations = sortedEvolutions collect {
        case (version, evolution) if version > documentVersion => evolution
      }
      JsSuccess(applicableTransformations)
    }

    private def applyTransformations(json: JsValue, transformations: Seq[Transformation]): JsResult[JsValue] =
      transformations.foldLeft[JsResult[JsValue]](JsSuccess(json)) {
        case (json, transform) => json flatMap { _.transform(transform) }
      }
  }
}