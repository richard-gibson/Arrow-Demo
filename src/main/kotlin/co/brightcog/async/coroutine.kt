package co.brightcog.async

import kotlinx.coroutines.experimental.*
import arrow.effects.*

import arrow.core.*
import arrow.data.*
import arrow.syntax.either.*
import arrow.syntax.foldable.sequence_
import arrow.syntax.option.*
import arrow.typeclasses.*

data class Street(val number: Int, val name: String)
data class Address(val city: String, val street: Street)
data class User(val id: Int, val name: String, val email: String, val address: Address, val phone: Option<Int> = none())
data class Flight(val flightNo: Int, val destination: String, val departure: String)
data class FlightManafest(val flightNo: Int, val passengers: List<Int>)

val DKWA = DeferredKW.applicative()

val flights = listOf(
        Flight(10567, "Belfast", "London"),
        Flight(10023, "Dublin", "Paris"),
        Flight(10011, "Berlin", "Rome")
).k()

val users = listOf(
        User(id = 1, name = "bob",
                email = "bob@bob.com",
                address = Address(city = "Belfast",
                        street = Street(1, "Antrim Rd")),
                phone = 12345678.some()),
        User(id = 2, name = "Jane",
                email = "Jane@jane.com",
                address = Address(city = "Belfast",
                        street = Street(5, "Antrim Rd")))
).k()

val flightManafests = listOf(
        FlightManafest(10567, listOf(1, 2)),
        FlightManafest(10023, listOf(1))
).k()


sealed class Failure
data class NotFound(val msg: String) : Failure()
data class FlightSystemException(val exception: Throwable) : Failure()

fun userByName(name: String): DeferredKW<Either<Failure, User>> =
        async{users.firstOrNull { it.name == name }.toOption().toEither { NotFound("user id for $name") }}.k()

fun manifestsContainingUser(user: User): DeferredKW<Either<Failure, Nel<FlightManafest>>> =
        async{ Nel.fromList(flightManafests
                .filter { fm -> fm.passengers.contains(user.id) })
                .toEither { NotFound("manifest for $user") }}.k()

fun flightById(flightNo: Int): DeferredKW<Either<Failure, Flight>> {
    val exec: () -> Option<Flight> = { flights.firstOrNull { it.flightNo == flightNo }.toOption() }
//    val exec: () -> Option<Flight> = { throw RuntimeException("KABOOM!!") }
    return async{safeSystemOp(exec, { it.toEither { NotFound("FlightNo: $flightNo") } })}.k()
}


fun flightsFromManifests(manafests: Nel<FlightManafest>): DeferredKW<Either<Failure, Nel<Flight>>> =
        manafests.traverse({ flightById(it.flightNo) }, DKWA).ev()
                 .map { it.traverse(::identity, Either.applicative()).ev() }

fun <A, B> safeSystemOp(fa: () -> A, fb: (A) -> Either<Failure, B>): Either<Failure, B> =
        Try(fa).fold({ FlightSystemException(it).left() }, fb)

fun userFlights(name: String): DeferredKW<Either<Failure, Nel<Flight>>> =
        EitherT.monadError<DeferredKWHK, Failure>().binding {
            val user = EitherT(userByName(name)).bind()
            val manifests = EitherT(manifestsContainingUser(user)).bind()
            val flights = EitherT(flightsFromManifests(manifests)).bind()
            yields(flights)
        }.ev().value.ev()




fun main(args: Array<String>) = runBlocking {
    println(userFlights("bob").deferred.await())


    println("Start")
    val jobs = List(100_000) {
        launch {
            delay(1000)
            print(".")
        }
    }

    jobs.forEach { it.join() }
    println("Stop")
}

