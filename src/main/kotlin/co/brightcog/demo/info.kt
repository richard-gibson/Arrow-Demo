package co.brightcog.demo

import arrow.core.*
import arrow.data.*
import arrow.syntax.option.*

data class Street(val number: Int, val name: String)
data class Address(val city: String, val street: Street)
data class User(val id: Int, val name: String, val email: String, val address: Address, val phone: Option<Int> = none())
data class Flight(val flightNo: Int, val destination: String, val departure: String)
data class FlightManafest(val flightNo: Int, val passengers: List<Int>)


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