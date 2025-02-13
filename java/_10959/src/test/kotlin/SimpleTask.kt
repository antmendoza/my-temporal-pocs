package kotlin

interface SimpleTask<T, U> {
    fun taskExecutionLogic(simpleTaskPayload: SimpleTaskPayload<T>): U

}
