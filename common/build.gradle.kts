plugins {
    `java-library`
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
    // paper
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")

    // velocity
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    // async mongodb
    implementation("org.mongodb:mongodb-driver-reactivestreams:5.1.0")

    // google guice
    compileOnly("com.google.inject:guice:7.0.0")

    // luckperms
    compileOnly("net.luckperms:api:5.4")

    // protocollib
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
}

tasks {
    compileJava {
        options.release = 22
        options.encoding = Charsets.UTF_8.name()
    }
}