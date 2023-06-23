# Coveralls Jacoco Gradle Plugin
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) ![Deployment Status](https://github.com/nbaztec/coveralls-jacoco-gradle-plugin/workflows/check/badge.svg) [![Coverage Status](https://coveralls.io/repos/github/nbaztec/coveralls-jacoco-gradle-plugin/badge.svg?branch=main)](https://coveralls.io/github/nbaztec/coveralls-jacoco-gradle-plugin?branch=main)

A jacoco test coverage reporter gradle plugin for [coveralls.io](https://coveralls.io).

The plugin supports non-root packages in line with the recommended [Kotlin directory structure](https://kotlinlang.org/docs/reference/coding-conventions.html#directory-structure) 
which was missing in many other plugins for the Kotlin ecosystem. 

The plugin automatically detects the root package, if it conforms to Kotlin guidelines and has a `.kt` file on the root level.

## Usage

[Gradle Plugin page](https://plugins.gradle.org/plugin/com.github.nbaztec.coveralls-jacoco)

Apply the plugin with the ID: `com.github.nbaztec.coveralls-jacoco`. 

```kotlin
// build.gradle.kts

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
}

plugins {
    jacoco
    id("com.github.nbaztec.coveralls-jacoco") version "1.2.15"
}
```

This will add a gradle task `coverallsJacoco` that can be used to publish coverage report via 
```bash
$ ./gradlew test jacocoTestReport coverallsJacoco
```

Set the value of `COVERALLS_REPO_TOKEN` from the project page on coveralls.io

Additionally, the following coveralls parameters may be specified via environment variables:
* `COVERALLS_PARALLEL` (`true`/`false`)
* `COVERALLS_FLAG_NAME`

## Options
```kotlin
// build.gradle.kts

coverallsJacoco {
    reportPath = "" // default: "build/reports/jacoco/test/jacocoTestReport.xml"

    reportSourceSets += sourceSets.foo.allJava.srcDirs + sourceSets.bar.allJava.srcDirs // default: main
    apiEndpoint = "" // default: https://coveralls.io/api/v1/jobs 
    
    dryRun = false // default: false
    coverallsRequest = File("build/req.json") // default: null
}
```

* `reportPath: String` - location of the jacoco xml report.
* `reportSourceSets: Iterable<File>` - a list of directories where to find the source code in.
* `apiEndpoint: String` - coveralls api endpoint for posting jobs.
* `dryRun: Boolean` - executes the task without posting to coveralls. Useful for debugging.
* `coverallsRequest: File` - writes the coveralls request payload to a file. Useful for debugging.

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

## Multi-Project Support - Pure Kotlin/Java
It is recommended to use the [JaCoCo Aggregation plugin](https://docs.gradle.org/current/userguide/jacoco_report_aggregation_plugin.html) to consolidate
the test reports. Please refer to the [sample](https://docs.gradle.org/current/samples/sample_jvm_multi_project_with_code_coverage_standalone.html) on the website, on how to configure the plugin, 
and specify the output file in the `coverallsJacoco` config.

```kotlin
coverallsJacoco {
    reportPath = "build/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml"
}
```

The `jacoco-report-aggregation` plugin must be configured in the same project as this plugin, regardless whether it is
configured in the root project or a standalone utility subproject.

## (Not Recommended) Multi-Project Support - Pure Kotlin/Java
To consolidate multiple JaCoCo coverage reports, the following code can be used to add a new task `codeCoverageReport`
```kotlin
tasks.register<JacocoReport>("codeCoverageReport") {
    val jacocoReportTask = this

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

    coverallsJacoco.dependsOn(jacocoReportTask)
}
```

## (Not Recommended) Multi-Project Support - Android

To consolidate multiple JaCoCo coverage reports on Android multi-project configurations, the following code can be used to add a new task `jacocoFullReport`

### Groovy DSL `build.gradle`
```groovy
// ignore any subproject, if required `subprojects.findAll{ it.name != 'customSubProject' }`
def coveredProjects = subprojects

// configure() method takes a list as an argument and applies the configuration to the projects in this list.
configure(coveredProjects) { p ->
    p.evaluate()

    // Here we apply jacoco plugin to every project
    apply plugin: 'jacoco'
    // Set Jacoco version
    jacoco {
        toolVersion = "0.8.5"
    }

    // Here we create the task to generate Jacoco report
    // It depends to unit test task we don't have to manually running unit test before the task
    task jacocoReport(type: JacocoReport, dependsOn: 'test') {

        // Define what type of report we should generate
        // If we don't want to process the data further, html should be enough
        reports {
            xml.enabled = true
            html.enabled = true
        }

        // Setup the .class, source, and execution directories
        final fileFilter = ['**/R.class', '**/R$*.class', '**/BuildConfig.*', '**/Manifest*.*', 'android/**/*.*']

        sourceDirectories.setFrom files(["${p.projectDir}/src/main/java"])
        classDirectories.setFrom files([
            fileTree(dir: "${p.buildDir}/classes", excludes: fileFilter),
            fileTree(dir: "${p.buildDir}/intermediates/javac/debug", excludes: fileFilter),
            fileTree(dir: "${p.buildDir}/tmp/kotlin-classes/debug", excludes: fileFilter),
        ])
        executionData.setFrom fileTree(dir: p.buildDir, includes: [
                'jacoco/*.exec', 'outputs/code-coverage/connected/*coverage.ec'
        ])
    }
}

apply plugin: 'jacoco'
apply plugin: 'com.github.nbaztec.coveralls-jacoco'

task jacocoFullReport(type: JacocoReport, group: 'Coverage reports') {
    def projects = coveredProjects

    // Here we depend on the jacocoReport task that we created before
    dependsOn(projects.jacocoReport)

    final source = files(projects.jacocoReport.sourceDirectories)

    additionalSourceDirs.setFrom source
    sourceDirectories.setFrom source

    classDirectories.setFrom files(projects.jacocoReport.classDirectories)
    executionData.setFrom files(projects.jacocoReport.executionData)

    reports {
        html {
            enabled true
            destination file("$buildDir/reports/jacoco/html")
        }
        xml {
            enabled true
            destination file("$buildDir/reports/jacoco/jacocoFullReport.xml")
        }
    }

    doFirst {
        executionData.setFrom files(executionData.findAll { it.exists() })
    }

    coverallsJacoco {
        reportPath = "$buildDir/reports/jacoco/jacocoFullReport.xml"
        reportSourceSets =  projects.jacocoReport.sourceDirectories.collect{ it.getFiles() }.flatten()
    }

    tasks.coverallsJacoco.dependsOn(it)
}
```

### Kotlin DSL `build.gradle.kts`
```kotlin

// ignore any subproject, if required `subprojects.findAll{ it.name != 'customSubProject' }`
val coveredProjects = subprojects

// configure() method takes a list as an argument and applies the configuration to the projects in this list.
configure(coveredProjects) {
    val p = (this as org.gradle.api.internal.project.DefaultProject)
    p.evaluate()

    // Here we apply jacoco plugin to every project
    apply(jacoco)

    // Set Jacoco version
    jacoco {
        toolVersion = "0.8.5"
    }

    // Here we create the task to generate Jacoco report
    // It depends to unit test task we don't have to manually running unit test before the task
    p.task("jacocoReport", JacocoReport::class) {

        // Define what type of report we should generate
        // If we don't want to process the data further, html should be enough
        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }

        // Setup the .class, source, and execution directories
        val fileTreeConfig: (ConfigurableFileTree) -> Unit = {
            it.exclude("**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*", "android/**/*.*")
        }

        sourceDirectories.setFrom(files("${p.projectDir}/src/main/java"))
        classDirectories.setFrom(listOf(
            fileTree("${p.buildDir}/classes", fileTreeConfig),
            fileTree("${p.buildDir}/intermediates/javac/debug", fileTreeConfig),
            fileTree("${p.buildDir}/tmp/kotlin-classes/debug", fileTreeConfig)
        ))
        executionData.setFrom(fileTree(p.buildDir) {
            include("jacoco/*.exec", "outputs/code-coverage/connected/*coverage.ec")
        })
    }.dependsOn("test")
}

plugins {
    jacoco
    id("com.github.nbaztec.coveralls-jacoco") version "1.2.4"
}

tasks {
    register("jacocoFullReport", JacocoReport::class) {
        val jacocoReportTask = this

        group = "Coverage reports"
        val projects = coveredProjects

        // Here we depend on the jacocoReport task that we created before
        val subTasks = projects.map { it.task<JacocoReport>("jacocoReport") }
        dependsOn(subTasks)

        val subSourceDirs = subTasks.map { files(it.sourceDirectories) }
        additionalSourceDirs.setFrom(subSourceDirs)
        sourceDirectories.setFrom(subSourceDirs)

        classDirectories.setFrom(subTasks.map { files(it.classDirectories) })
        executionData.setFrom(subTasks.map { files(it.executionData) })

        reports {
            html.isEnabled = true
            html.destination = file("$buildDir/reports/jacoco/html")

            xml.isEnabled = true
            xml.destination = file("$buildDir/reports/jacoco/jacocoFullReport.xml")
        }

        doFirst {
            executionData.setFrom(files(executionData.filter { it.exists() }))
        }

        coverallsJacoco {
            dependsOn(jacocoReportTask)

            reportPath = "$buildDir/reports/jacoco/jacocoFullReport.xml"
            reportSourceSets = subSourceDirs.flatMap { it.files }
        }
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
* Gitlab CI
* Bitrise


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
      with:
        ref: ${{ github.event.pull_request.head.sha }}
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
      with:
        ref: ${{ github.event.pull_request.head.sha }}
    - name: test and publish coverage
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
      run: ./gradlew test jacocoTestReport coverallsJacoco
```
### Buildkite

See Buildkite environment variables [documentation](https://buildkite.com/docs/pipelines/environment-variables#defining-your-own)

### Gitlab CI

See Gitlab CI predefined variables [documentation](https://docs.gitlab.com/ee/ci/variables/predefined_variables.html)

### Bitrise CI

See Bitrise CI predefined variables [documentation](https://devcenter.bitrise.io/builds/available-environment-variables/)

### Other CI

For [other CIs](https://docs.coveralls.io/supported-ci-services#insert-your-ci-here), the following default environment variables are supported:

```
CI_NAME
CI_BUILD_NUMBER
CI_BUILD_URL
CI_BRANCH
CI_PULL_REQUEST
```