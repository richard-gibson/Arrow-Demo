package co.brightcog.optics

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import arrow.data.ListK
import arrow.data.each
import arrow.data.index
import arrow.data.k
import arrow.optics.Lens
import arrow.optics.dsl.index
import arrow.optics.optics

@optics data class Street(val number: Int, val name: String) { companion object {} }
@optics data class Address(val city: String, val street: Street) { companion object {} }
@optics data class Company(val name: String, val address: Address) { companion object {} }
@optics data class Employee(val name: String, val company: Option<Company>) { companion object {} }
@optics data class Employees(val employees: ListK<Employee>){ companion object {} }

fun main(s: Array<String>) {

    val john = Employee("John Doe",
            Company("Arrow", Address("Functional city", Street(23, "lambda street"))).some())
    val jane = Employee("Jane Doe",
            Company("Comp2", Address("Another city", Street(123, "jane's street"))).some())

    val bill = Employee("bill Doe", none())


    println(john)

    println(Employee.company.address.street.name.modify(jane,  String::toUpperCase))
    println(Employee.company.address.street.name.modify(bill,  String::toUpperCase))


    println(Employee.company.address.street.modify(john){Street(it.number+1, it.name.toUpperCase())})

    val employees = Employees(listOf(john, jane).k())



    println(ListK.each<Employee>().run {
        Employees.employees.every.company.address.street.name.modify(employees, String::capitalize)
    })

    println(ListK.index<Employee>().run {
        Employees.employees[3].company.address.street.name.modify(employees, String::capitalize)
    })

    println(ListK.index<Employee>().run {
        Employees.employees[0].company.address.street.name.getOption(employees)
    })


    println(ListK.index<Employee>().run {
        Employees.employees[50].company.address.street.name.getOption(employees)
    })




}