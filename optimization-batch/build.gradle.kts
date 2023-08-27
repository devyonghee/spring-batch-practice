dependencies {
    implementation("org.springframework.batch:spring-batch-integration")
    implementation("org.springframework.integration:spring-integration-amqp")
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    // DeployerPartition
    implementation("org.springframework.cloud:spring-cloud-starter-task")
    implementation("org.springframework.cloud:spring-cloud-deployer-local:2.8.3")

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("org.flywaydb:flyway-mysql")
}