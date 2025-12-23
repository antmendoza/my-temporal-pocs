plugins {
    application
}

repositories {
    mavenCentral()
}

configurations.all {
    resolutionStrategy {
        dependencySubstitution.all {
            val requested = requested as ModuleComponentSelector
            if (requested.group == "com.google.guava" && requested.module == "guava") {
                // gradle doesn't build without this hack
                val ver = requested.version.replace("-android", "-jre")
                useTarget("${requested.group}:${requested.module}:$ver")
            }
        }
    }
}

dependencies {
    val ver = object {
        val logback = "1.2.12"
        val temporal = "1.22.3"
        val log4j = "2.17.2"
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("io.temporal:temporal-testing:${ver.temporal}")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.assertj:assertj-core:3.22.0")

    testImplementation("org.apache.logging.log4j:log4j-api:${ver.log4j}")
    testImplementation("org.apache.logging.log4j:log4j-to-slf4j:${ver.log4j}")
    testImplementation("ch.qos.logback:logback-classic:${ver.logback}")
    testImplementation("ch.qos.logback:logback-core:${ver.logback}")

    implementation("io.temporal:temporal-sdk:${ver.temporal}")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass = "org.example.App"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
