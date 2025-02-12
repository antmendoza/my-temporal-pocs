package io.temporal.samples.kotlin

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerCronTaskResult(listOf: List<String>) {
    public val team: List<String> = listOf

    //create empty constructor
    constructor() : this(listOf())

}
