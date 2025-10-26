import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.gradleup.shadow") version "9.2.2"
    id("net.kyori.blossom") version "2.2.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.3" // IntelliJ + Blossom integration
}

group = "com.gmail.tracebachi"
version = "2.1.1"

repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url = "https://nexus.velocitypowered.com/repository/maven-public/")
}

dependencies {
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.3")
    implementation("com.zaxxer:HikariCP:2.6.3")
    compileOnly("net.md-5:bungeecord-api:1.21-R0.3")
    compileOnly("org.spigotmc:spigot-api:1.13.2-R0.1-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

sourceSets {
    main {
        blossom {
            resources {
                property("version", project.version.toString())
                property("description", project.description ?: "")
            }
            javaSources {
                property("version", project.version.toString())
                property("description", project.description ?: "")
            }
        }
    }
}

tasks.getByName<ShadowJar>("shadowJar") {
    // relocate("com.zaxxer", "com.gmail.tracebachi.DbShare.lib.com.zaxxer")
    // relocate("org.slf4j", "com.gmail.tracebachi.DbShare.lib.org.slf4j")

    // MariaDB Connector/J
    relocate("com.github", "com.gmail.tracebachi.DbShare.lib.com.github")
    relocate("com.google.errorprone", "com.gmail.tracebachi.DbShare.lib.com.google.errorprone")
    relocate("com.sun", "com.gmail.tracebachi.DbShare.lib.com.sun")
    relocate("org.apache", "com.gmail.tracebachi.DbShare.lib.org.apache")
    relocate("org.checkerframework", "com.gmail.tracebachi.DbShare.lib.org.checkerframework")
    relocate("org.mariadb", "com.gmail.tracebachi.DbShare.lib.org.mariadb")
    relocate("waffle", "com.gmail.tracebachi.DbShare.lib.waffle")
}
