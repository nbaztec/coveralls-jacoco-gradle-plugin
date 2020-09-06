# Coveralls Jacoco Gradle Plugin
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) [![Coverage Status](https://coveralls.io/repos/github/nbaztec/coveralls-jacoco-gradle-plugin/badge.svg?branch=main)](https://coveralls.io/github/nbaztec/coveralls-jacoco-gradle-plugin?branch=main)

A jacoco test coverage reporter gradle plugin for [coveralls.io](https://coveralls.io).

The plugin supports non-root packages in line with the recommended [Kotlin directory structure](https://kotlinlang.org/docs/reference/coding-conventions.html#directory-structure) 
which was missing in many other plugins for the Kotlin ecosystem. 

The plugin automatically detects the root package, if it conforms to Kotlin guidelines and has a `.kt` file on the root level.

## Usage

[Gradle Plugin page](https://plugins.gradle.org/plugin/com.github.nbaztec.coveralls-jacoco)

Add the `google()` repository. The plugin relies on it to detect android projects.
Apply the plugin with the ID: `com.github.nbaztec.coveralls-jacoco`. 

```kotlin
// build.gradle.kts

buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}

plugins {
    jacoco
    id("com.github.nbaztec.coveralls-jacoco")
}
```

This will add a gradle task `coverallsJacoco` that can be used to publish coverage report via 
```bash
$ ./gradlew test jacocoTestReport coverallsJacoco
```

Set the value of `COVERALLS_REPO_TOKEN` from the project page on coveralls.io

## Options
```kotlin
// build.gradle.kts

coverallsJacoco {
    reportPath = "" // default: "build/reports/jacoco/test/jacocoTestReport.xml"
    reportSourceSets += sourceSets.foo.allJava.srcDirs + sourceSets.bar.allJava.srcDirs // optional, default: main
    apiEndpoint = "" // optional, default: https://coveralls.io/api/v1/jobs 
}
```

## Excluding Files
Please refer to the official JaCoCo documentation to exclude files from the report. An example configuration is as follows:
```
jacocoTestReport {
    afterEvaluate {
        classDirectories = files(classDirectories.files.collect {
            fileTree(dir: it, exclude: "com/foo/**")
        })
    }
}
```

## Multi-Project Support
To consolidate multiple JaCoCo coverage reports, the following code can be used to add a new task `codeCoverageReport`
```kotlin
tasks.register<JacocoReport>("codeCoverageReport") {
    // If a subproject applies the 'jacoco' plugin, add the result it to the report
    subprojects {
        val subproject = this
        subproject.plugins.withType<JacocoPlugin>().configureEach {
            subproject.tasks.matching({ it.extensions.findByType<JacocoTaskExtension>() != null }).configureEach {
                val testTask = this
                sourceSets(subproject.sourceSets.main.get())
                executionData(testTask)
            }

            // To automatically run `test` every time `./gradlew codeCoverageReport` is called,
            // you may want to set up a task dependency between them as shown below.
            // Note that this requires the `test` tasks to be resolved eagerly (see `forEach`) which
            // may have a negative effect on the configuration time of your build.
            subproject.tasks.matching({ it.extensions.findByType<JacocoTaskExtension>() != null }).forEach {
                rootProject.tasks["codeCoverageReport"].dependsOn(it)
            }
        }
    }


    // enable the different report types (html, xml, csv)
    reports {
        // xml is usually used to integrate code coverage with
        // other tools like SonarQube, Coveralls or Codecov
        xml.isEnabled = true

        // HTML reports can be used to see code coverage
        // without any external tools
        html.isEnabled = true
    }
}
```

## CI Usage
The plugin can be used with the following CI providers:

* Travis-CI & Travis-Pro
* CircleCI
* Github Actions
* Jenkins
* Codeship
* Buildkite


### Travis
Set the `COVERALLS_REPO_TOKEN` via [Environment Variables](https://docs.travis-ci.com/user/environment-variables/) or [Encryption Keys](https://docs.travis-ci.com/user/encryption-keys/) and set up a job as follows:

```yaml
language: java
script: ./gradlew test jacocoTestReport coverallsJacoco
```

### CircleCI
Set the `COVERALLS_REPO_TOKEN` via CircleCI's [Environment Variables](https://circleci.com/docs/2.0/env-vars/) and set up a job as follows:
```yaml
jobs:
  check:
    docker:
      - image: amazoncorretto:11
    steps:
      - run: ./gradlew test jacocoTestReport coverallsJacoco
```

### Github Actions
Set the `COVERALLS_REPO_TOKEN` via Github's [Environment Variables](https://docs.github.com/en/actions/configuring-and-managing-workflows/using-environment-variables) or [Secrets](https://docs.github.com/en/actions/configuring-and-managing-workflows/creating-and-storing-encrypted-secrets) and set up a job as follows:
```yaml
jobs:
  tests:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    - name: test and publish coverage
      env:
        COVERALLS_REPO_TOKEN: ${{ secrets.COVERALLS_REPO_TOKEN }}
      run: ./gradlew test jacocoTestReport coverallsJacoco
```

For running on publicly forked PRs, the plugin uses a (undocumented) API and uses `GITHUB_TOKEN` to identify the repo instead, as follows:
```yaml
jobs:
  tests:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    - name: test and publish coverage
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
      run: ./gradlew test jacocoTestReport coverallsJacoco
```
### Buildkite

See [buildkite environment variables documentation](https://buildkite.com/docs/pipelines/environment-variables#defining-your-own)
