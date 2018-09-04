package co.brightcog.free.basic
/*
import co.brightcog.free.User
import arrow.*
import arrow.core.*
import arrow.data.*
import arrow.free.*
import arrow.free.instances.*
import arrow.syntax.option.*
import arrow.typeclasses.*
import java.util.*

@higherkind sealed class UserRepositoryAlg<T> : UserRepositoryAlgKind<T> {
    data class FindUser(val id: UUID) : UserRepositoryAlg<Option<User>>()
    data class UpdateUser(val u: User) : UserRepositoryAlg<Unit>()
    companion object : FreeMonadInstance<UserRepositoryAlgHK> {
        fun findUser(id: UUID): UserRepository<Option<User>> = Free.liftF(UserRepositoryAlg.FindUser(id))
        fun updateUser(u: User): UserRepository<Unit> = Free.liftF(UserRepositoryAlg.UpdateUser(u))
        fun <M, A>run(p: UserRepository<A>, interpreter: FunctionK<UserRepositoryAlgHK, M>, m: Monad<M>) =
                p.foldMap(interpreter, m)
    }
}

typealias UserRepository<T> = Free<UserRepositoryAlgHK, T>

val idInterpreter: FunctionK<UserRepositoryAlgHK, IdHK> = object : FunctionK<UserRepositoryAlgHK, IdHK> {
    override fun <A> invoke(fa: UserRepositoryAlgKind<A>): Id<A> {
        val op = fa.ev()
        @Suppress("UNCHECKED_CAST")
        return when (op) {
//            is UserRepositoryAlg.FindUser -> Id.pure(Some(User(id = op.id, email = "e@mail.com", loyaltyPoints = 0)))
            is UserRepositoryAlg.FindUser -> Id.pure(None)
            is UserRepositoryAlg.UpdateUser -> Id.pure(println("user updated ${op.u}"))
        } as Id<A>
    }
}


fun addPointsProgram(userId: UUID, pointsToAdd: Int): UserRepository<Either<String, Unit>> = UserRepositoryAlg.binding {
    val updatedUser = UserRepositoryAlg.findUser(userId).bind()
            .map { it.copy(loyaltyPoints = pointsToAdd + it.loyaltyPoints) }
    val res = updatedUser
            .map { UserRepositoryAlg.updateUser(it).map { Right(Unit) } }
            .getOrElse { Free.pure<UserRepositoryAlgHK, Either<String, Unit>>(Left("User not found")) }
            .bind()
    yields(res)
}.ev()


val result =
        UserRepositoryAlg.run(addPointsProgram(UUID.randomUUID(), 10), idInterpreter, Id.monad()).ev()

fun main(s: Array<String>) {
   val i = result
    println(i)
}
    */