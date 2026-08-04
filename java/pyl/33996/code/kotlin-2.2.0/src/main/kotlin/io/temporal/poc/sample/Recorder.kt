package io.temporal.poc.sample

import io.temporal.testing.TestWorkflowEnvironment
import java.io.File

object Recorder {
    @JvmStatic
    fun main(args: Array<String>) {
        val out = File(if (args.isNotEmpty()) args[0] else "history-kotlin-2.2.0.json")


        val worker = SampleWorker
        worker.main(arrayOf())


        val historyAsJson = SampleStarter.execute()

        out.writeText(historyAsJson)
        println("Wrote history to ${out.absolutePath}")


        worker.shutdown()


    }
}
