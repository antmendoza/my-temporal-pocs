package io.temporal.poc.sample

import io.temporal.activity.ActivityInterface

@ActivityInterface
interface GreetingActivities {
    fun composeGreeting(name: String): String
}

class GreetingActivitiesImpl : GreetingActivities {
    override fun composeGreeting(name: String): String {
        Thread.sleep(2000)
        return "Hello, $name"
    }
}