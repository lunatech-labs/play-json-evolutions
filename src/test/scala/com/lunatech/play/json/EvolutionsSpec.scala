package controllers

import org.specs2.mutable.Specification
import com.lunatech.play.json.evolutions.RichFormat
import play.api.libs.functional.syntax.toInvariantFunctorOps
import play.api.libs.json.{ Json, Reads }
import com.lunatech.play.json.Transformers
import play.api.libs.json.JsString
import play.api.libs.json.JsNumber

class EvolutionsSpec extends Specification {

  case class User(name: String, age: Int, role: String)
  val userFormat = Json.format[User]
  val user = User("Erik", 29, "member")

  "A formatter with evolutions" should {

    "write the same value as the original formatter if no evolutions have been defined" in {
      val f = userFormat.withEvolutions()

      f.writes(user) must_== userFormat.writes(user)
    }

    "write the highest version number if evolutions have been defined" in {
      val someReads = Reads.JsObjectReads

      val f1 = userFormat.withEvolutions(1 -> someReads)
      (f1.writes(user) \ "_version").as[Int] must_== 1


      val f2 = userFormat.withEvolutions(1 -> someReads, 2 -> someReads)
      (f2.writes(user) \ "_version").as[Int] must_== 2


      val f3 = userFormat.withEvolutions(3 -> someReads, 4 -> someReads)
      (f3.writes(user) \ "_version").as[Int] must_== 4
    }

    "read a written class when no evolutions are defined" in {
      implicit val f = userFormat.withEvolutions()
      val json = Json.toJson(user)
      json.as[User] must_== user
    }

    "read a written class when evolutions are defined" in {
      implicit val f = userFormat.withEvolutions(1 -> Transformers.addAll("key" -> "value"))
      val json = Json.toJson(user)
      json.as[User] must_== user
    }

    "upgrade old json before deserializing" in {
      implicit val f = userFormat.withEvolutions(
        1 -> Transformers.addAll("age" -> JsNumber(29)),
        2 -> Transformers.addAll("role" -> "member")
      )

      val oldJson = Json.obj("name" -> "Erik")
      oldJson.as[User] must_== user
    }

  }

}
