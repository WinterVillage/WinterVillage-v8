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

    // paper
    maven("https://repo.papermc.io/repository/maven-public/")

    // protocollib
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    implementation(project(":common"))

    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")

    // async mongodb
    compileOnly("org.mongodb:mongodb-driver-reactivestreams:5.1.0")

    // google guice
    implementation("com.google.inject:guice:7.0.0")

    // luckperms
    compileOnly("net.luckperms:api:5.4")

    // protocollib
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")

    // triumphgui
    implementation("dev.triumphteam:triumph-gui:3.1.10")

    // custom block data
    implementation("com.jeff-media:custom-block-data:2.2.3")
}

tasks {
    compileJava {
        options.release = 22
        options.encoding = Charsets.UTF_8.name()
    }

    shadowJar {
        archiveBaseName.set("wintervillage")
        archiveClassifier.set("")

        relocate("dev.triumphteam.gui", "de.wintervillage.main.gui")
        relocate("com.jeff_media.customblockdata", "de.wintervillage.main.customblockdata")

        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
    }
}