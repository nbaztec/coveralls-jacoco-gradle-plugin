repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.dom4j", "dom4j", "2.1.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")
}

plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    id("com.gradle.plugin-publish") version "0.12.0"
}

group = "com.github.nbaztec"
version = "1.0.0"

gradlePlugin {
    plugins {
        create("coverallsJacocoKotlinPlugin") {
            id = "com.github.nbaztec.coveralls-jacoco-kotlin"
            implementationClass = "org.gradle.plugin.coveralls.jacoco.CoverallsJacocoPlugin"
        }
    }
}
pluginBundle {
    (plugins) {
        "coverallsJacocoKotlinPlugin" {
            website = "http://github.com/nbaztec/coveralls-jacoco-kotlin-gradle-plugin/"
            vcsUrl = "https://github.com/nbaztec/coveralls-jacoco-kotlin-gradle-plugin.git"
            description = "Send jacoco coverage data to coveralls.io"
            tags = listOf("coverage", "coveralls")
            displayName = "Coveralls Jacoco Kotlin Plugin"
        }
    }
}
