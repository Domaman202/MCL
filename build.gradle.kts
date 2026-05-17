plugins {
    id("java")
}

group = "ru.DmN.mca"
version = "1.1.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("io.github.domaman202:MCA:1.6.1")
}

tasks.test {
    useJUnitPlatform()
}