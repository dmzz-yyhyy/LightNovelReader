import java.net.URI

version = "0.0.1-SNAPSHOT"

plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(libs.dom4j)
    implementation(libs.jaxen)
}

tasks.test {
    useJUnitPlatform()
}

java {
    withSourcesJar()
    withJavadocJar()
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["java"])
                groupId = "io.nightfish.potatoepub"
                artifactId = "epub"
                version = project.version.toString()
                pom {
                    name.set("potatoepub")
                    description.set("A kotlin library to prase and gen epub")
                    licenses {
                        license {
                            name.set("Apache-2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                }
            }
        }

        repositories {
            maven {
                name = "reposilite"
                url = URI("https://maven.curiousers.org/release")
                credentials {
                    username = System.getenv("REPO_USER")
                    password = System.getenv("REPO_PASS")
                }
            }
        }
    }
}
