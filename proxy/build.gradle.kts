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

    // velocity
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    // async mongodb
    implementation("org.mongodb:mongodb-driver-reactivestreams:5.1.0")

    // google guice
    compileOnly("com.google.inject:guice:7.0.0")

    // luckperms
    compileOnly("net.luckperms:api:5.4")
}

tasks {
    compileJava {
        options.release = 22
        options.encoding = Charsets.UTF_8.name()
    }

    shadowJar {
        archiveBaseName.set("wintervillage")
        archiveClassifier.set("")
    }
}

tasks.register("generateTemplates") { }