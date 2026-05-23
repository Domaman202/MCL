plugins {
    id("java")
}

group = "ru.DmN.mcl"
version = "1.3.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("io.github.domaman202:MCL:1.10.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withSourcesJar()
    withJavadocJar()
}