# Coveralls Jacoco Kotlin Gradle Plugin
A jacoco test coverage reporter gradle plugin for Kotlin and [coveralls.io](https://coveralls.io).

The plugin supports non-root packages in line with the recommended [Kotlin directory structure](https://kotlinlang.org/docs/reference/coding-conventions.html#directory-structure) 
which was missing in many other plugins for the Kotlin ecosystem. 

## Usage
Apply the plugin with the ID: `com.github.nbaztec.coveralls-jacoco-kotlin`

This will add a gradle task `coverallsJacoco` that can be used to publish coverage report via `./gradlew test coverallsJacoco`

## Options
```kotlin
coverallsJacoco {
    rootPackage = 'com.github.nbaztec.foo' // optional, leave out if project has java directory structure  
    reportPath = 'build/reports/jacoco/test/jacocoTestReport.xml'
}
```