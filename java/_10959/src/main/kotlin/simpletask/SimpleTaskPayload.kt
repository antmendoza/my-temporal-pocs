package simpletask

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.squareup.wire.WireTypeAdapterFactory
import io.temporal.common.converter.DataConverter
import io.temporal.common.converter.DefaultDataConverter
import io.temporal.common.converter.GsonJsonPayloadConverter

@JsonIgnoreProperties(ignoreUnknown = true)
class SimpleTaskPayload<T> (
  @JsonProperty("payload") private var payload: T,
  @JsonProperty("activityName") private val activityName: String? = "dynamicActivity",
) {
  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@modelClass")
  fun getPayload(): T {
    return this.payload
  }

  fun getActivityName(): String {
    if (activityName != null) {
      return activityName
    }
    return "dynamicActivity"
  }


  data class Builder<T>(
    var payload: T? = null,
    var activityName: String = "dynamicActivity",
  ) {

    /**
     * Set the payload which will be delivered to the simpletask
     * execution. This method is required to be set; if not set
     * an exception will be thrown
     *
     * @param payload
     */
    fun withTaskPayload(payload: T) = apply { this.payload = payload }


    /**
     * Setting this field will override the default activity name set in the
     * workflow history.
     *
     * @param activityName Defaults to "dynamicActivity"
     */
    fun withActivityName(activityName: String) = apply { this.activityName = activityName }

    fun build(): SimpleTaskPayload<T> {
      if (payload == null) {
        throw Exception("Task payload cannot be null")
      }

      return SimpleTaskPayload(
        payload = payload!!,
        activityName = activityName,
      )
    }
  }
}

fun createWireGsonJsonDataConverter(): DataConverter {
  return DefaultDataConverter.newDefaultInstance()
    .withPayloadConverterOverrides(
      GsonJsonPayloadConverter { it.registerTypeAdapterFactory(WireTypeAdapterFactory()) },
    )
}