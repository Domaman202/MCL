plugins {
    id("java")
}

group = "ru.DmN.mca"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("io.github.domaman202:MCA:1.2.0")
}

tasks.test {
    useJUnitPlatform()
}