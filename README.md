JSON Evolutions
===============

This is a small library adding _evolutions_ to Play's `JsValue` json classes. It allows you to easily upgrade old json documents to the most recent json format that your application uses.

It consists of two parts: _Transformers_, so simplify construction of the most basic transformers, and a pimp for Play's `Format` trait to make them work with evolutions.

Usage
=====

Add the following dependency to your application's `Build.scala` or `build.sbt`:

    "com.lunatech" %% "play-json-evolutions" % "0.1.2"

This requires one of the following resolvers:

    // For release versions
    "Lunatech public releases" at "http://artifactory.lunatech.com/artifactory/releases-public"
    
    // For SNAPSHOT versions
    "Lunatech public snapshots" at "http://artifactory.lunatech.com/artifactory/snapshots-public"

Versions
========

Version 0.1.0 is built for Play 2.1.x
Version 0.1.2 is built for Play 2.3.x, both Scala 2.10 and 2.11

Suggested use cases
===================

MongoDB
=======
If you're using MongoDB and your document model evolves, you can use JSON evolutions to 'upgrade' your old JSON, between fetching it from Mongo and deserializing it in your Play app. When you save
this document again, it will be in the newest version. This means that you don't need downtime to upgrade your entire DB at once.

Event sourcing
==============
If you're doing event sourcing with JSON serialized events and your event model evolves, you can use JSON evolutions to upgrade old events during replay.


Simple usage
============

Suppose that you have a `User` class, that only has a field `name`, and an accompanying `Format[User]`:

    // Old version of the User class
    case class User(name: String)

    object User {
      val userFormat = Json.format[User]
    }

If you use Play's macros to generate JSON formats, they're represented in JSON as `{ 'name' : 'Erik' }`. Now, you want to add a field 'role', because you've added authorization support to your app. So your new class is:

    // New version of the User class
    case class User(name: String, role: String)

    object User {
      val userFormat = Json.format[User]
    }

If you now try to deserialize the JSON of an existing user, it will fail because the `role` field is missing. JSON Evolutions allow you to upgrade the old user json to new JSON, so that it can then be deserialized. In this case, we want to add a field `'role': 'member'` to the JSON:

    // New version of the User class
    case class User(name: String, role: String)

    object User {
      val userFormat = Json.format[User].withEvolutions(
        1 -> Transformers.add("role", JsString("member")))
    }

Now, the userFormat will write the following JSON, when serializing `User("Erik", "member")`:

    { "name" : "Erik", "role" : "member", "_version" : 1 }

When reading, it will read the `_version` field in the JSON (and assume version 0 if there's no version yet), and apply all evolutions with a higher number before deserializing.

Transformers
============

In play, a JSON transformer is a `Reads[_ <: JsValue]`. This might seem odd at first, but a `Reads` lets you validate JSON, and in this case, it produces a new `JsValue`, which means it's effectively transforming JSON into some other JSON, while validating the input.

The functions that Play offers to construct these `Reads` are not always obvious or easy to use though. This library provides a couple of transformers that make the really simple tasks really simple. For harder tasks, you still need to manually create `Reads` instances. The starting point for this is probably the methods on the `JsPath.json` object.

Examples
========

    TODO

Contributors
============

Created by @eamelink, idea by @EECOLOR.


License
=======

Copyright 2014 Lunatech (http://www.lunatech.com).

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
