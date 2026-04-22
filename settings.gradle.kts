pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "sparrowdesk-mobile-sdk"
include(":sparrowdesk-sdk")
include(":sample:androidApp")
include(":sample-ecommerce:androidApp")
