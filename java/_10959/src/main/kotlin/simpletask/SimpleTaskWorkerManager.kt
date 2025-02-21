package simpletask

import io.temporal.worker.WorkerFactory

class SimpleTaskWorkerManager(private val workerFactory: WorkerFactory) {
  fun startup() {
    workerFactory.start()
  }

  fun shutdown() {
    workerFactory.shutdown()
  }
}