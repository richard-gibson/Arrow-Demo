package co.brightcog.validation

import arrow.core.*
import arrow.core.Either.Left
import arrow.core.Either.Right
import arrow.data.*

inline fun <A, B> Either<A, B>.valid(pred: (B) -> Boolean, onFail: () -> A): Either<A, B> = when (this) {
    is Left<A> -> this
    is Right<B> -> if (pred(this.b)) this else Either.Left(onFail())
}

inline fun <A, B> Either<A, B>.toValidatedNel(): ValidatedNel<A, B> =
        this.fold({ Invalid(Nel.of(it)) }, { Valid(it) })


inline fun <A, B> Either<A, B>.toValidated(): Validated<A, B> =
        this.fold({ Invalid(it) }, { Valid(it) })