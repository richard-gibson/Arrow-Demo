package co.brightcog.demo.opt

import arrow.core.*
import arrow.data.Nel
import arrow.instances.extensions
import arrow.typeclasses.binding
import co.brightcog.demo.*


fun userByName(name: String): Option<User> =
        users.firstOrNull { it.name == name }.toOption()

fun manifestsContainingUser(user: User): Option<Nel<FlightManafest>> =
        Nel.fromList(flightManafests.filter { fm -> fm.passengers.contains(user.id) })

fun flightById(flightNo: Int): Option<Flight> =
        flights.firstOrNull { it.flightNo == flightNo }.toOption()


fun userFlights(name: String): Option<Nel<Flight>> =
          ForOption extensions {
            binding {
              val user = userByName(name).bind()
              val manifests = manifestsContainingUser(user).bind()
              manifests.traverse(Option.applicative(), { flightById(it.flightNo) }).bind()
            }.fix()
          }


fun main(s: Array<String>) {

    println(userFlights("bob"))
    println(userFlights("brian"))
}