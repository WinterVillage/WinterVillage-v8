plugins {
    `java-library`

    // shadow
    id("io.github.goooler.shadow") version "8.1.7"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(22)
}

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    implementation(project(":common"))

    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")

    // velocity
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    // google guice
    compileOnly("com.google.inject:guice:7.0.0")

    // cloudnet
    compileOnly("eu.cloudnetservice.cloudnet:bridge:4.0.0-RC10")
    compileOnly("eu.cloudnetservice.cloudnet:platform-inject-api:4.0.0-RC10")
    compileOnly("eu.cloudnetservice.cloudnet:wrapper-jvm:4.0.0-RC10")
}

tasks {
    compileJava {
        options.release = 22
        options.encoding = Charsets.UTF_8.name()
    }

    shadowJar {
        archiveBaseName.set("wintervillage-restart")
        archiveClassifier.set("")

        relocate("eu.cloudnetservice.cloudnet", "eu.cloudnetservice.cloudnet")
    }
}

tasks.register("generateTemplates") { }