plugins {
    id("signing")
    `maven-publish`
    java
}

repositories {
    mavenLocal()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven(uri("https://repo.viaversion.com"))
    maven(uri("https://maven.plugily.xyz/releases"))
    maven(uri("https://maven.plugily.xyz/snapshots"))
    maven(uri("https://jitpack.io"))
    maven(uri("https://repo.maven.apache.org/maven2/"))
}

dependencies {
    implementation("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    implementation("com.viaversion:viaversion-api:4.0.0")
    implementation("com.github.ProtocolSupport:ProtocolSupport:master")
}


group = "me.tigerhix.lib"
version = "1.4.4"
description = "scoreboard"
java.sourceCompatibility = JavaVersion.VERSION_1_8


tasks {
    processResources {
        filesMatching("**/plugin.yml") {
            expand(project.properties)
        }
    }

    javadoc {
        options.encoding = "UTF-8"
    }

}

publishing {
    repositories {
        maven {
            name = "Releases"
            url = uri("https://maven.plugily.xyz/releases")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
        maven {
            name = "Snapshots"
            url = uri("https://maven.plugily.xyz/snapshots")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}