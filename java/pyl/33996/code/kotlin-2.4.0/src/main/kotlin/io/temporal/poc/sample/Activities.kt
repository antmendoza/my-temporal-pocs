package io.temporal.poc.sample

import io.temporal.activity.ActivityInterface


class GreetingActivitiesImpl : GreetingActivities {
    override fun composeGreeting(name: String): String = "Notified $name"
}

@ActivityInterface
interface GreetingActivities {
    fun composeGreeting(name: String): String
}
