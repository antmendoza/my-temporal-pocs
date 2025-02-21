package simpletask

import io.micrometer.core.instrument.MeterRegistry
import io.temporal.common.converter.DataConverter
import io.temporal.common.converter.GlobalDataConverter
import io.temporal.serviceclient.ServiceStubsOptions
import io.temporal.serviceclient.WorkflowServiceStubsOptions
import io.temporal.worker.WorkerOptions

class SimpleTaskTemporalConfig @JvmOverloads constructor(
  val temporalNamespace: String,
  val taskQueue: String,
  val temporalTargetEndpoint: String,
  val dataConverter: DataConverter = GlobalDataConverter.get(),
  val serviceStubOptions: ServiceStubsOptions = WorkflowServiceStubsOptions.getDefaultInstance(),
  val workerOptions: WorkerOptions = WorkerOptions.getDefaultInstance(),
)