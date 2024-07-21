import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
    `java-library`

    // shadow
    id("io.github.goooler.shadow") version "8.1.7"

    // paper userdev
    id("io.papermc.paperweight.userdev") version "1.7.1"

    // plugin.yml based on gradle config
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

dependencies {
    paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")

    // async mongodb
    implementation("org.mongodb:mongodb-driver-reactivestreams:5.1.0")

    // google guice
    implementation("com.google.inject:guice:7.0.0")

    // luckperms
    compileOnly("net.luckperms:api:5.4")
}

tasks {
    compileJava {
        options.release = 21
        options.encoding = Charsets.UTF_8.name()
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    shadowJar {
        archiveBaseName.set("wintervillage")
        archiveClassifier.set("")
    }
}

tasks.withType<JavaExec> {
    jvmArgs = listOf("-Dfile.encoding=UTF-8")
}

bukkitPluginYaml{
    name = "WinterVillage-Main"
    main = "de.wintervillage.main.WinterVillage"
    load = BukkitPluginYaml.PluginLoadOrder.STARTUP
    authors.addAll("Eagler", "Voldechse")
    apiVersion = "1.21"
    version = "${project.version}"
    loadBefore.addAll("LuckPerms")
}