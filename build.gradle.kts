plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    application
}

group = "pl.kliniewski"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("pl.kliniewski.stock.calculator.MainKt")
}

tasks.withType<Jar>() {
    manifest {
        attributes["Main-Class"] = application.mainClass.orNull
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile)) {
            exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/versions/**")
        }
    }
}