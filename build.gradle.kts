plugins {
    id("java")
    id("application")
}

group = "br.com.peer"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("br.com.peer.Peer")
}

// Add task to run the demo
tasks.register<JavaExec>("runDemo") {
    group = "application"
    description = "Run the P2P chat demonstration"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("br.com.peer.PeerChatDemo")
}

tasks.test {
    useJUnitPlatform()
}