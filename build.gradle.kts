import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

version = "1.2.0"

tasks.named<ShadowJar>("shadowJar") {
    enableAutoRelocation = false
}
