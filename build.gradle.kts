plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.2.0"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(
            providers.gradleProperty("platformType"),
            providers.gradleProperty("platformVersion"),
        )
        // LSP4IJ — bridge between any LSP server and IntelliJ-based IDEs
        plugins("com.redhat.devtools.lsp4ij:${providers.gradleProperty("lsp4ijVersion").get()}")

        pluginVerifier()
        zipSigner()
    }
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")
        ideaVersion {
            sinceBuild = "243"
        }
    }
    signing {
        // Set CERTIFICATE_CHAIN, PRIVATE_KEY, PRIVATE_KEY_PASSWORD env vars to sign
    }
    publishing {
        // Set PUBLISH_TOKEN env var for JetBrains Marketplace
    }
}
