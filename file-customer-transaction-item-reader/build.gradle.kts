dependencies {
    runtimeOnly("com.h2database:h2")

    implementation("org.springframework:spring-oxm")

    // xml
    implementation("javax.xml.bind:jaxb-api:2.3.0")
    implementation("org.glassfish.jaxb:jaxb-runtime")

    // json
    implementation("com.fasterxml.jackson.core:jackson-databind")
}