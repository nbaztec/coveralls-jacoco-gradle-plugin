group = "com.github.nbaztec"
version = "1.0.1"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.dom4j", "dom4j", "2.1.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.8.1.202007141445-r")
    implementation("org.apache.httpcomponents:httpmime:4.5.2")
    implementation("com.google.code.gson:gson:2.8.5")
}

plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    id("com.gradle.plugin-publish") version "0.12.0"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "coveralls-jacoco-kotlin"

            from(components["java"])
        }

    }
}

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
