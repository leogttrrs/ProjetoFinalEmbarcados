plugins {
    kotlin("jvm")
    application
}

val ktorVersion = "3.1.1"

application {
    mainClass.set("br.com.projetofinal.gateway.ApplicationKt")
}

dependencies {
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:1.5.16")
}
