package co.brightcog.demo
import arrow.effects.*


import kotlinx.coroutines.experimental.*
import arrow.core.*
import arrow.data.*
import arrow.syntax.either.*
import arrow.syntax.option.*
import arrow.typeclasses.*


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
        manafests.traverse({ flightById(it.flightNo) }, DeferredKW.applicative()).ev()
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

}