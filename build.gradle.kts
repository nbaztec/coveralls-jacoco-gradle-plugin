group = "com.github.nbaztec"
version = "1.0.4"

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
    testImplementation("junit:junit:4.13")
    testImplementation( "org.junit.jupiter:junit-jupiter-api:5.6.2")
    testImplementation("io.mockk:mockk:1.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

tasks {
    test {
        useJUnitPlatform()
    }
    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }
    }
}

plugins {
    jacoco
    `java-gradle-plugin`
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    id("com.gradle.plugin-publish") version "0.12.0"
    id("com.github.nbaztec.coveralls-jacoco-kotlin") version "1.0.3"
}

coverallsJacoco {
    rootPackage = "org.gradle.plugin.coveralls.jacoco"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "coveralls-jacoco"

            from(components["java"])
        }

    }
}

gradlePlugin {
    plugins {
        create("coverallsJacocoPlugin") {
            id = "com.github.nbaztec.coveralls-jacoco"
            implementationClass = "org.gradle.plugin.coveralls.jacoco.CoverallsJacocoPlugin"
        }
    }
}

pluginBundle {
    (plugins) {
        "coverallsJacocoPlugin" {
            website = "http://github.com/nbaztec/coveralls-jacoco-gradle-plugin/"
            vcsUrl = "https://github.com/nbaztec/coveralls-jacoco-gradle-plugin.git"
            description = "Send jacoco coverage data to coveralls.io"
            tags = listOf("coverage", "coveralls")
            displayName = "Coveralls Jacoco Plugin"
        }
    }
}
