package mypackage

interface SimpleTask<T, U> {
    fun run(simpleTaskPayload: Any): U
}
