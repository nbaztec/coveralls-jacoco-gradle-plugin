import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.github.nbaztec"
version = "1.2.12"

buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        languageVersion = "1.4"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    google()
    mavenCentral()
    jcenter()
}

val testAndroid: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

val testAndroidImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

val testAndroidRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}

dependencies {
    implementation("org.jetbrains.kotlin", "kotlin-gradle-plugin", Versions.kotlin)
    implementation("org.jetbrains.kotlin", "kotlin-reflect", Versions.kotlin)
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", Versions.kotlinxCoroutines)
    implementation("org.dom4j", "dom4j", Versions.dom4j)
    implementation("org.eclipse.jgit", "org.eclipse.jgit", Versions.jgit)
    implementation("org.apache.httpcomponents", "httpmime", Versions.httpMime)
    implementation("com.google.code.gson", "gson", Versions.gson)
    compileOnly("com.android.tools.build", "gradle", Versions.androidBuildTools)

    testImplementation("junit", "junit", Versions.junit)
    testImplementation("org.junit.jupiter", "junit-jupiter-api", Versions.jupiter)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", Versions.jupiter)
    testImplementation("io.mockk", "mockk", "1.10.5")

    testAndroidImplementation("com.android.tools.build", "gradle", Versions.androidBuildTools)
    testAndroidImplementation("junit", "junit", Versions.junit)
    testAndroidImplementation("org.junit.jupiter", "junit-jupiter-api", Versions.jupiter)
    testAndroidRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", Versions.jupiter)
    testAndroidImplementation("io.mockk", "mockk", Versions.mockk)
}

tasks {
    test {
        useJUnitPlatform()
    }

    val testAndroid by registering(Test::class) {
        useJUnitPlatform()
        description = "Runs android source set tests."
        group = "verification"
        testClassesDirs = testAndroid.output.classesDirs
        classpath = testAndroid.runtimeClasspath
        shouldRunAfter(test)
    }

    check {
        dependsOn(testAndroid)
    }

    jacocoTestReport {
        dependsOn(test, testAndroid)
        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }
        executionData(File(buildDir, "jacoco/testAndroid.exec"))
    }

    create("setupPublishSecrets") {
        doLast {
            val key = System.getenv("GRADLE_PUBLISH_KEY")
            val secret = System.getenv("GRADLE_PUBLISH_SECRET")

            check(key != null) { "GRADLE_PUBLISH_KEY is required" }
            check(secret != null) { "GRADLE_PUBLISH_SECRET is required" }

            System.setProperty("gradle.publish.key", key)
            System.setProperty("gradle.publish.secret", secret)
        }
    }
}

plugins {
    idea
    jacoco
    `java-gradle-plugin`
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version Versions.kotlin
    id("com.gradle.plugin-publish") version Versions.gradlePublishPlugin
    id("com.github.nbaztec.coveralls-jacoco") version Versions.gradleCoverallsJacocoPlugin
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

idea {
    module {
        testSourceDirs = testSourceDirs.apply {
            addAll(testAndroid.allJava.srcDirs)
        }
    }
}
