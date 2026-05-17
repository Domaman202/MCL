plugins {
    id("java")
}

group = "ru.DmN.mca"
version = "1.1.1"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("io.github.domaman202:MCA:1.8.0")
}

tasks.test {
    useJUnitPlatform()
}