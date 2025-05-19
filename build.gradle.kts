plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "club.shengsheng"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.2.0.202503040940-r")
    implementation("io.netty:netty-all:4.2.0.Final")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.google.guava:guava:33.4.8-jre")
    implementation("org.kohsuke:github-api:2.0-rc.3")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.57")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
