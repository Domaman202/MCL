plugins {
    id("java")
    id("com.gradleup.nmcp.aggregation").version("1.2.1")
    `maven-publish`
    `java-library`
    signing
    kotlin("jvm")
}

group = "io.github.domaman202"
version = "1.6.1"

dependencies {
    implementation("org.apache.logging.log4j:log4j-api:2.0")
    implementation("org.apache.commons:commons-lang3:3.20.0")
    implementation("com.google.code.gson:gson:2.2.4")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("CommandLine Arguments Library")
                description.set("Library for processing program startup arguments in console")
                url.set("https://github.com/Domaman202/CmdArgsKt")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("DomamaN202")
                        name.set("DomamaN202")
                        email.set("vip.domaman@mail.ru")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/Domaman202/CmdArgsKt.git")
                    developerConnection.set("scm:git:ssh://github.com/Domaman202/CmdArgsKt.git")
                    url.set("https://github.com/Domaman202/CmdArgsKt")
                }
            }
        }
    }

    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

nmcpAggregation {
    centralPortal {
        username = System.getenv("MAVEN_USERNAME")
        password = System.getenv("MAVEN_PASSWORD")
        publicationName = "$group:$name:$version"
        publishingType = "USER_MANAGED"
        publishingType = "AUTOMATIC"
    }

    publishAllProjectsProbablyBreakingProjectIsolation()
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
}
repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(8)
}