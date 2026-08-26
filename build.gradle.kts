plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "flowviz.resources"
    generateResClass = always
}

kotlin {
    wasmJs {
        moduleName = "flowviz"
        browser {
            commonWebpackConfig {
                outputFileName = "flowviz.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val wasmJsMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
