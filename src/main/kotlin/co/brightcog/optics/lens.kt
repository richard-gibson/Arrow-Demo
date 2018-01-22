package co.brightcog.optics


import arrow.*
import arrow.optics.*
import arrow.core.*
import arrow.syntax.option.some

@lenses data class Street(val number: Int, val name: String)
@lenses data class Address(val city: String, val street: Street, val postcode: Option<String>)
@lenses data class Company(val name: String, val address: Address)
@lenses data class Employee(val name: String, val company: Company)


fun main(s: Array<String>) {

    val employee = Employee("John Doe", Company("Arrow", Address("Functional city", Street(23, "lambda street"), "BT5 1LL".some())))
    println(employee)

    val employeeAddress: Lens<Employee, Address> = employeeCompany() compose companyAddress()
    val employeeStreetName: Lens<Employee, String> = employeeAddress compose addressStreet() compose streetName()
    val employeePostCode = employeeAddress compose addressPostcode()

    println(employeeStreetName.modify(employee, String::toUpperCase))
    println(employeePostCode.modify(employee, {it.map { s -> s.toLowerCase() }}))



}