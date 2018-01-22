package co.brightcog.validation

import arrow.core.*
import arrow.data.*

inline fun <A, B> Either<A, B>.valid(pred: (B) -> Boolean, onFail: () -> A): Either<A, B> = when (this) {
    is Either.Left<A, B> -> this
    is Either.Right<A, B> -> if (pred(this.b)) this else Either.Left(onFail())
}

inline fun <A, B> Either<A, B>.toValidatedNel(): ValidatedNel<A, B> =
        this.fold({ Invalid(Nel.of(it)) }, { Valid(it) })


inline fun <A, B> Either<A, B>.toValidated(): Validated<A, B> =
        this.fold({ Invalid(it) }, { Valid(it) })