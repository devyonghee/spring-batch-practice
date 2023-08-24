dependencies {
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("org.springframework:spring-oxm")
    implementation("javax.xml.bind:jaxb-api:2.3.0")
    implementation("org.glassfish.jaxb:jaxb-runtime")

    implementation("com.fasterxml.jackson.core:jackson-databind")

    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("org.flywaydb:flyway-mysql")
}