dependencies {
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0")

    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("org.flywaydb:flyway-mysql")

    implementation("org.springframework:spring-oxm")
    implementation("com.thoughtworks.xstream:xstream:1.4.20")
}