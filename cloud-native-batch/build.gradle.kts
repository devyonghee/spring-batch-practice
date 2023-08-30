dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-task")
    implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE")
    implementation("org.springframework.cloud:spring-cloud-starter-config")

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0")

    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("org.flywaydb:flyway-mysql")
}