package co.brightcog.demo.opt

import arrow.core.*
import arrow.data.*
import arrow.syntax.option.*
import arrow.typeclasses.*
import co.brightcog.demo.*


fun userByName(name: String): Option<User> =
        users.firstOrNull { it.name == name }.toOption()

fun manifestsContainingUser(user: User): Option<Nel<FlightManafest>> =
        Nel.fromList(flightManafests.filter { fm -> fm.passengers.contains(user.id) })

fun flightById(flightNo: Int): Option<Flight> =
        flights.firstOrNull { it.flightNo == flightNo }.toOption()


fun userFlights(name: String): Option<Nel<Flight>> =
        try {
            Option.monad().binding {
                val user = userByName(name).bind()
                val manifests = manifestsContainingUser(user).bind()
                val flights = manifests.traverse({ flightById(it.flightNo) }, Option.applicative()).bind()
                yields(flights)
            }.ev()

        } catch (e: Exception) {
            println("Something went wrong ${e.message}")
            throw e
        }


fun main(s: Array<String>) {

    println(userFlights("bob"))
    println(userFlights("brian"))
}