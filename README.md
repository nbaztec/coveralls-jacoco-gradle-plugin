# Coveralls Jacoco Kotlin Gradle Plugin
A jacoco test coverage reporter gradle plugin for Kotlin and [coveralls.io](https://coveralls.io).

The plugin supports non-root packages in line with the recommended [Kotlin directory structure](https://kotlinlang.org/docs/reference/coding-conventions.html#directory-structure) 
which was missing in many other plugins for the Kotlin ecosystem. 

## Usage

[Gradle Plugin page](https://plugins.gradle.org/plugin/com.github.nbaztec.coveralls-jacoco-kotlin)

Apply the plugin with the ID: `com.github.nbaztec.coveralls-jacoco-kotlin`
This will add a gradle task `coverallsJacoco` that can be used to publish coverage report via `./gradlew test coverallsJacoco`

## Options
```kotlin
coverallsJacoco {
    reportPath = 'build/reports/jacoco/test/jacocoTestReport.xml'
    rootPackage = 'com.github.nbaztec.foo' // optional, leave out if project has java directory structure  
    additionalSourceSets = [ sourceSets.foo, sourceSets.bar ] // optional, sourceSet.main is always included
    apiEndpoint = "https://coveralls.io/api/v1/jobs" // optional
}
```