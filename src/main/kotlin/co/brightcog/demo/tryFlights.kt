package co.brightcog.demo.tr

import arrow.core.*
import arrow.data.Nel
import arrow.instances.extensions
import arrow.typeclasses.Monad
import arrow.typeclasses.binding
import co.brightcog.demo.*

data class EmptyReponseException(val msg: String) : Exception(msg)

fun <T> Option<T>.toTry(ifEmpty: () -> Throwable): Try<T> =
     this.fold ({ arrow.core.Failure(ifEmpty())} , {arrow.core.Success(it)})


fun userByName(name: String): Try<User> =
        users.firstOrNull { it.name == name }.toOption().toTry { EmptyReponseException("no user id for $name") }


fun manifestsContainingUser(user: User): Try<Nel<FlightManafest>> =
        Nel.fromList(flightManafests
                .filter { fm -> fm.passengers.contains(user.id) })
                .toTry { EmptyReponseException("manifest for $user") }

fun flightById(flightNo: Int): Try<Flight> =
    flights.firstOrNull { it.flightNo == flightNo }.toOption()
            .toTry {   EmptyReponseException("no flights for $flightNo") }

fun flightsFromManifests(manifests: Nel<FlightManafest>): Try<Nel<Flight>> =
        manifests.traverse(Try.applicative(), {flightById(it.flightNo) }).fix()

fun userFlights(name: String): Try<Nel<Flight>> =
    ForTry extensions {
      binding {
        val user = userByName(name).bind()
        val manifests = manifestsContainingUser(user).bind()
        flightsFromManifests(manifests).bind()
      }.fix()
    }

fun userFlightsFM(name: String): Try<Nel<Flight>> =
    userByName(name).flatMap { user ->
      manifestsContainingUser(user).flatMap { manifests ->
        manifests.traverse(Try.applicative(), { flightById(it.flightNo) }).fix()
      }
    }

fun main(s: Array<String>) {

    println(userFlights("bob"))
    println(userFlights("brian"))
}