//         id("org.jetbrains.kotlin.js") version "1.4-M1"
plugins {
    id("org.jetbrains.kotlin.js") version "1.3.72"
}

group = "fm.force"
version = "1.0-SNAPSHOT"


val kotlinVersion: String by extra
val reactVersion: String by extra
val extensionsVersion: String by extra
val reactRouterDomVersion: String by extra
val reactReduxVersion: String by extra
val reduxVersion: String by extra
val styledVersion: String by extra
val ktorClientVersion: String by extra

repositories {
    mavenCentral()
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
    maven { url = uri("https://kotlin.bintray.com/kotlin-js-wrappers/") }
    maven { url = uri("https://dl.bintray.com/kotlin/kotlinx.html/") }
    maven { url = uri("https://dl.bintray.com/cfraser/muirwik") }
}

kotlin {
    target {
//        produceExecutable()
        browser {
            @UseExperimental(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDceDsl::class)
            dceTask {
                // dceOptions
                keep("ktor-ktor-io.\$\$importsForInline\$\$.ktor-ktor-io.io.ktor.utils.io")
            }
            compilations.all {
                kotlinOptions {
                    friendModulesDisabled = false
                    metaInfo = true
                    sourceMap = true
                    sourceMapEmbedSources = "always"
                    moduleKind = "commonjs"
                    main = "call"
                }
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    sourceSets {
        dependencies {
            implementation(kotlin("stdlib-js"))
            implementation(npm("core-js", "3.6.4"))

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.3")
            implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.106-kotlin-1.3.72")

        }
    }
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.3")

}

