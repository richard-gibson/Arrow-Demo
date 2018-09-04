package co.brightcog.demo

import arrow.core.*
import arrow.data.EitherT
import arrow.data.Nel
import arrow.data.fix
import arrow.data.sequence
import arrow.effects.*
import arrow.instances.ForEitherT
import arrow.typeclasses.binding
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking


sealed class Failure
data class NotFound(val msg: String) : Failure()
data class FlightSystemException(val exception: Throwable) : Failure()


fun userByName(name: String): DeferredK<Either<Failure, User>> =
        async{users.firstOrNull { it.name == name }.toOption().toEither { NotFound("user id for $name") }}.k()

fun manifestsContainingUser(user: User): DeferredK<Either<Failure, Nel<FlightManafest>>> =
        async{ Nel.fromList(flightManafests
                .filter { fm -> fm.passengers.contains(user.id) })
                .toEither { NotFound("manifest for $user") }}.k()

fun flightById(flightNo: Int): DeferredK<Either<Failure, Flight>> {
    val exec: () -> Option<Flight> = { flights.firstOrNull { it.flightNo == flightNo }.toOption() }
//    val exec: () -> Option<Flight> = { throw RuntimeException("KABOOM!!") }
    return async{safeSystemOp(exec) { it.toEither { NotFound("FlightNo: $flightNo") } } }.k()
}

fun flightsFromManifests(manafests: Nel<FlightManafest>): DeferredK<Either<Failure, Nel<Flight>>> =
        manafests.traverse(DeferredK.applicative(), { flightById(it.flightNo) }).fix()
                .map { it.sequence(Either.applicative()).fix() }

fun <A, B> safeSystemOp(fa: () -> A, fb: (A) -> Either<Failure, B>): Either<Failure, B> =
        Try(fa).fold({ FlightSystemException(it).left() }, fb)

fun userFlights(name: String): DeferredK<Either<Failure, Nel<Flight>>> =
    ForEitherT<ForDeferredK, Failure>(DeferredK.monad()) extensions {
      binding {
        val user = EitherT(userByName(name)).bind()
        val manifests = EitherT(manifestsContainingUser(user)).bind()
        EitherT(flightsFromManifests(manifests)).bind()
      }.fix().value.fix()
    }



fun main(args: Array<String>) = runBlocking {
    println(userFlights("bob").deferred.await())

}