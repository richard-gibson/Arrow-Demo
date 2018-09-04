package co.brightcog.demo.nl

import co.brightcog.demo.*

fun userByName(name: String): User? =
        users.filter { it.name == name }.firstOrNull()

fun manifestsContainingUser(user: User): List<FlightManafest>? =
        flightManafests.filter { fm -> fm.passengers.contains(user.id) }

fun flightById(flightNo: Int): Flight? =
        flights.filter { it.flightNo == flightNo }.firstOrNull()


fun userFlights(name: String): List<Flight>? {
  try {
    return userByName(name)?.let {
      manifestsContainingUser(it)?.mapNotNull { flightById(it.flightNo) }
    }
  } catch (e: Exception) {
    println("Something went wrong ${e.message}")
    throw e
  }
}


fun main(s: Array<String>) {

    println(userFlights("bob"))

    println(userFlights("brian"))
}