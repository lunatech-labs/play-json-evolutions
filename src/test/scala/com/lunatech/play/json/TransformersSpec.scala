import com.lunatech.play.json.Transformers
import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, JsString, Json, __}

class TransformerSpec extends Specification {

  "The 'add' transformer" should {
    "add a fixed field with a string key to an object" in {
      val transformer = Transformers.add("key2", JsString("val2"))
      Json.obj("key1" -> "val1").transform(transformer).get must_== Json.obj("key1" -> "val1", "key2" -> "val2")
    }

    "add a fixed field at a path to an object" in {
      val transformer = Transformers.add(__ \ "key2", JsString("val2"))
      Json.obj("key1" -> "val1").transform(transformer).get must_== Json.obj("key1" -> "val1", "key2" -> "val2")
    }

    "add a fixed field at a deep path to an object" in {
      val transformer = Transformers.add(__ \ "key1" \ "key11", JsString("val1"))
      Json.obj().transform(transformer).get must_== Json.obj("key1" -> Json.obj("key11" -> "val1"))
    }
  }

  "The 'addAll' transformer" should {
    "add multiple fixed fields with a string key to an object" in {
      val transformer = Transformers.addAll("key2" -> "val2", "key3" -> "val3")
      Json.obj("key1" -> "val1").transform(transformer).get must_== Json.obj("key1" -> "val1", "key2" -> "val2", "key3" -> "val3")
    }
  }

  "The 'remove' transformer" should {
    "remove a field with string key from an object" in {
      val transformer = Transformers.remove("key2")
      Json.obj("key1" -> "val1", "key2" -> "val2").transform(transformer).get must_== Json.obj("key1" -> "val1")
    }

    "remove a field at a path from an object" in {
      val transformer = Transformers.remove(__ \ "key2")
      Json.obj("key1" -> "val1", "key2" -> "val2").transform(transformer).get must_== Json.obj("key1" -> "val1")
    }

    "remove a field at a deep path from an object" in {
      val transformer = Transformers.remove(__ \ "key2" \ "key22")
      Json.obj("key1" -> "val1", "key2" -> Json.obj("key22" -> "val2")).transform(transformer).get must_== Json.obj("key1" -> "val1", "key2" -> Json.obj())
    }
  }

  "The 'removeAll' transformer" should {
    "remove multipled fields with string keys from an object" in {
      val transformer = Transformers.removeAll("key2", "key3")
      Json.obj("key1" -> "val1", "key2" -> "val2", "key3" -> "val3").transform(transformer).get must_== Json.obj("key1" -> "val1")
    }
  }

  "The 'copy' transformer" should {
    "copy a field at a string key to a different key in an object" in {
      val transformer = Transformers.copy("key1", "key2")
      Json.obj("key1" -> "val1").transform(transformer).get must_== Json.obj("key1" -> "val1", "key2" -> "val1")
    }

    "copy a field at a path to a different path in an object" in {
      val transformer = Transformers.copy(__ \ "key1", __ \ "key2")
      Json.obj("key1" -> "val1").transform(transformer).get must_== Json.obj("key1" -> "val1", "key2" -> "val1")
    }

    "copy a field at a path to a deep path in an object" in {
      val transformer = Transformers.copy(__ \ "key1", __ \ "key2" \ "key22")
      Json.obj("key1" -> "val1").transform(transformer).get must_== Json.obj("key1" -> "val1", "key2" -> Json.obj("key22" -> "val1"))
    }
  }

  "The 'rename' transformer" should {
    "rename a field at a string key to a different key in an object" in {
      val transformer = Transformers.rename("key1", "key2")
      Json.obj("key1" -> "val1").transform(transformer).get must_== Json.obj("key2" -> "val1")
    }

    "rename a field at a path to a different path in an object" in {
      val transformer = Transformers.rename(__ \ "key1", __ \ "key2")
      Json.obj("key1" -> "val1").transform(transformer).get must_== Json.obj("key2" -> "val1")
    }

    "rename a field at a path to a different deep path in an object" in {
      val transformer = Transformers.rename(__ \ "key1", __ \ "key2" \ "key22")
      Json.obj("key1" -> "val1").transform(transformer).get must_== Json.obj("key2" -> Json.obj("key22" -> "val1"))
    }
  }

  "The 'all' transformer" should {
    "reduce a set of transformers by applying them in sequence" in {
      val t1 = Transformers.add("key2", JsString("val2"))
      val t2 = Transformers.add("key3", JsString("val3"))

      val total = Transformers.all(t1, t2)
      Json.obj("key1" -> "val1").transform(total).get must_== Json.obj("key1" -> "val1", "key2" -> "val2", "key3" -> "val3")
    }
  }

  "The 'manual' transformer" should {
    "transform using a JsValue => JsValue function" in {
      val transformer = Transformers.manual {
        case o: JsObject => o + ("key2" -> JsString("val2"))
        case other => sys.error("Expected a JsObject")
      }

      Json.obj("key1" -> "val1").transform(transformer).get must_== Json.obj("key1" -> "val1", "key2" -> "val2")
    }
  }

  "The 'using' transformer" should {
    "transform with a Reads constructed using the old value" in {
      val transformer = Transformers.using {
        case o: JsObject =>
          val value: String = (o \ "key1").as[String]
          Transformers.add("key2", JsString("foo_" + value))
        case other => sys.error("Expected a JsObject")
      }

      Json.obj("key1" -> "val1").transform(transformer).get must_== Json.obj("key1" -> "val1", "key2" -> "foo_val1")
    }
  }

}