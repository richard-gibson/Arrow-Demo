package co.brightcog.demo.eith

import arrow.core.*
import arrow.data.Nel
import arrow.instances.ForEither
import arrow.typeclasses.binding
import co.brightcog.demo.*


sealed class Failure
data class NotFound(val msg: String) : Failure()
data class FlightSystemException(val exception: Throwable) : Failure()

fun userByName(name: String): Either<Failure, User> =
        users.firstOrNull { it.name == name }.toOption().toEither { NotFound("user id for $name") }

fun manifestsContainingUser(user: User): Either<Failure, Nel<FlightManafest>> =
        Nel.fromList(flightManafests
                .filter { fm -> fm.passengers.contains(user.id) })
                .toEither { NotFound("manifest for $user") }

fun flightById(flightNo: Int): Either<Failure, Flight> {
    val exec: () -> Option<Flight> = { flights.firstOrNull { it.flightNo == flightNo }.toOption() }
//    val exec: () -> Option<Flight> = { throw RuntimeException("KABOOM!!") }
    return safeSystemOp(exec, { it.toEither { NotFound("FlightNo: $flightNo") } })
}

fun <A, B> safeSystemOp(fa: () -> A, fb: (A) -> Either<Failure, B>): Either<Failure, B> =
        Try(fa).fold({ FlightSystemException(it).left() }, fb)

fun flightsFromManifests(manifests: Nel<FlightManafest>): Either<Failure, Nel<Flight>> =
        manifests.traverse(Either.applicative(), {flightById(it.flightNo) }).fix()

fun userFlights(name: String): Either<Failure, Nel<Flight>> =
    ForEither<Failure>() extensions {
      binding {
        val user = userByName(name).bind()
        val manifests = manifestsContainingUser(user).bind()
        flightsFromManifests(manifests).bind()
      }.fix()
    }

fun userFlightsFM(name: String): Either<Failure, Nel<Flight>> =
    userByName(name).flatMap { user ->
      manifestsContainingUser(user).flatMap { manifests ->
        manifests.traverse(Either.applicative(), { flightById(it.flightNo) }).fix()
      }
    }

fun main(s: Array<String>) {

    println(userFlights("bob"))
    println(userFlights("brian"))
}