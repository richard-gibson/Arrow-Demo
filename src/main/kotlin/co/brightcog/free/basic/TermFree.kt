package co.brightcog.free.basic

import arrow.*
import arrow.core.*
import arrow.data.*
import arrow.free.*
import arrow.free.instances.*
import arrow.syntax.option.*
import arrow.typeclasses.*

@higherkind sealed class Terminal<out T> : TerminalKind<T> {
    object ReadLine : Terminal<String>()
    data class WriteLine(val inp: String) : Terminal<Unit>()
    companion object : FreeMonadInstance<TerminalHK> {
        fun readline(): TerminalIO<String> = Free.liftF(ReadLine)
        fun writeline(inp: String): TerminalIO<Unit> = Free.liftF(WriteLine(inp))
        fun <M, A>run(program: TerminalIO<A>, interpreter: FunctionK<TerminalHK, M>, m: Monad<M>) =
                program.foldMap(interpreter, m)
    }
}

typealias TerminalIO<T> = Free<TerminalHK, T>

val prog: TerminalIO<String> = Terminal.binding {
    val r1 = Terminal.readline().bind()
    val r2 = Terminal.readline().bind()
    Terminal.writeline("::: $r1 :::").bind()
    Terminal.writeline("::: $r2 :::").bind()
    yields("$r1  ::::  $r2")
}.ev()


internal fun <A> Option<Nel<A>>.pop(): Tuple2<Option<Nel<A>>, Option<A>> =
        this.fold({ None toT None }, { Nel.fromList(it.tail) toT it.head.some() })

internal fun <A> Option<Nel<A>>.push(s: A): Tuple2<Option<Nel<A>>, Unit> =
        this.fold({ Nel.of(s).some() toT Unit }, { Nel(s, it.all).some() toT Unit })

data class Mock(val inp: Option<Nel<String>> = None, val out: Option<Nel<String>> = None)


typealias MockState<A> = State<Mock, A>

fun termToState(): FunctionK<TerminalHK, StateKindPartial<Mock>> = object : FunctionK<TerminalHK, StateKindPartial<Mock>> {
    override fun <A> invoke(fa: TerminalKind<A>): MockState<A> {
        val op = fa.ev()
        @Suppress("UNCHECKED_CAST")
        return when (op) {
            is Terminal.ReadLine ->
                State<Mock, String> {
                    val (ins, s) = it.inp.pop()
                    it.copy(inp = ins) toT s.getOrElse { "" }
                }
            is Terminal.WriteLine ->
                State.modify<IdHK, Mock> { it.copy(out = it.out.push(op.inp).a) }
        } as MockState<A>
    }
}

fun termToEval(): FunctionK<TerminalHK, EvalHK> = object : FunctionK<TerminalHK, EvalHK> {
    override fun <A> invoke(fa: TerminalKind<A>): Eval<A> {
        val op = fa.ev()
        @Suppress("UNCHECKED_CAST")
        return when (op) {
            is Terminal.ReadLine -> Eval.later { readLine() }
            is Terminal.WriteLine -> Eval.later { println("[Eval Result] ${op.inp}") }
        } as Eval<A>
    }
}


fun main(s: Array<String>) {

    val mockState = Mock(inp = Nel.fromList(listOf("hello ", "world", "foo", "bar")))
    val mockRun = Terminal.run(prog, termToState(), State.monad()).ev()
    val mockRunRes1 = mockRun.run(mockState).a
    val mockRunRes2 = mockRun.run(mockRunRes1).a
    println(mockRunRes1)
    println(mockRunRes2)

    val evalRun = Terminal.run(prog, termToEval(), Eval.monad()).ev()
    println("Running Eval program: please enter 2 values on separate lines")
    println(evalRun.value())


}