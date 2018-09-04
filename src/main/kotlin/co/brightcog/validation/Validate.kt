package co.brightcog.validation

import arrow.core.Either
import arrow.core.fix
import arrow.core.right
import arrow.data.ListK
import arrow.data.Nel
import arrow.data.fix
import arrow.data.semigroup
import arrow.instances.ForEither
import arrow.instances.ForValidated
import arrow.typeclasses.binding

sealed class Failure {
    data class EmptyString(val fieldName: String) : Failure()
    data class InvalidCity(val fieldName: String) : Failure()
    data class ValueOutOfRange(val i: Int) : Failure()
}

val zip = listOf("00111", "001122", "000333")
val cities = listOf("Dublin", "London", "Madrid")

data class Employee(val name: String, val zipCode: String, val city: String, val salary: Int)

fun nonBlank(fieldName: String, data: String): Either<Failure, String> =
        data.right().valid({ it.isNotEmpty() }) { Failure.EmptyString("$fieldName cannot be blank") }

fun inRange(lower: Int, upper: Int, data: Int): Either<Failure, Int> =
        data.right().valid({ it in lower..upper }) { Failure.ValueOutOfRange(data) }

fun validZip(data: String): Either<Failure, String> =
        data.right().valid({ it in zip }) { Failure.InvalidCity(data) }

fun validCities(data: String): Either<Failure, String> =
        data.right().valid({ it in cities }) { Failure.InvalidCity(data) }


fun empEitherFromMonad(name: String, zipCode: String, city: String, salary: Int): Either<Failure, Employee> =
    ForEither<Failure>() extensions {
      binding {
        val n = nonBlank("name", name).bind()
        val z = validZip(zipCode).bind()
        val c = validCities(city).bind()
        val s = inRange(10, 20, salary).bind()
        Employee(n, z, c, s)
      }.fix()
    }

fun empEitherFromApp(name: String, zipCode: String, city: String, salary: Int): Either<Failure, Employee> =
    ForEither<Failure>() extensions {
      map(nonBlank("name", name),
          validZip(zipCode),
          validCities(city),
          inRange(10, 20, salary)) { (n, z, c, s) ->
        Employee(n, z, c, s)
      }.fix()
    }


fun empValidatedFromApp(name: String, zipCode: String, city: String, salary: Int): Either<Nel<Failure>, Employee> =
    ForValidated<Nel<Failure>>(Nel.semigroup()) extensions {
      map(
          nonBlank("name", name).toValidatedNel(),
          validZip(zipCode).toValidatedNel(),
          validCities(city).toValidatedNel(),
          inRange(10, 20, salary).toValidatedNel()) { (n, z, c, s) ->
        Employee(n, z, c, s)
      }.fix().toEither()
    }


fun main(a: Array<String>) {

    println(empEitherFromApp("foo", "00111", "Dublin", 15))
    println(empEitherFromApp("", "00", "Washington", 17500))
    println(empEitherFromApp("foo", "00", "Washington", 17500))
    println()

    println()
    println(empEitherFromMonad("foo", "00111", "Dublin", 15))
    println(empEitherFromMonad("", "00", "Washington", 17500))
    println(empEitherFromMonad("foo", "00", "Washington", 17500))
    println()

    println()
    println(empValidatedFromApp("foo", "00111", "Dublin", 15))
    println(empValidatedFromApp("", "00", "Washington", 17500))
    println(empValidatedFromApp("foo", "00", "Washington", 17500))
}
