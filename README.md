# Coveralls Jacoco Gradle Plugin
[![Coverage Status](https://coveralls.io/repos/github/nbaztec/coveralls-jacoco-gradle-plugin/badge.svg?branch=master)](https://coveralls.io/github/nbaztec/coveralls-jacoco-gradle-plugin?branch=master)

A jacoco test coverage reporter gradle plugin for [coveralls.io](https://coveralls.io).

The plugin supports non-root packages in line with the recommended [Kotlin directory structure](https://kotlinlang.org/docs/reference/coding-conventions.html#directory-structure) 
which was missing in many other plugins for the Kotlin ecosystem. 

## Usage

[Gradle Plugin page](https://plugins.gradle.org/plugin/com.github.nbaztec.coveralls-jacoco)

Apply the plugin with the ID: `com.github.nbaztec.coveralls-jacoco`
This will add a gradle task `coverallsJacoco` that can be used to publish coverage report via `./gradlew test coverallsJacoco`

## Options
```kotlin
// build.gradle.kts

coverallsJacoco {
    reportPath = "" // default: "build/reports/jacoco/test/jacocoTestReport.xml"
    rootPackage = "com.github.nbaztec.foo" // optional, leave out if project has a normal java styled directory structure  
    reportSourceSets = [ sourceSets.foo, sourceSets.bar ] // optional, default: main
    apiEndpoint = "" // optional, default: https://coveralls.io/api/v1/jobs 
}
```