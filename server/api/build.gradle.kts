import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.graalvm.buildtools.native")
}

dependencies {
    implementation(project(":module:core"))
    testImplementation(project(":module:core"))

    // Inject spring beans on runtime
    implementation(project(":module:persistence"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.oauth2.resource.server)

    // Remove Jakarta Persistence API related warnings
    implementation(libs.jakarta.persistence.api)
}

tasks.getByName<BootJar>("bootJar") {
    enabled = true
}

tasks.getByName<Jar>("jar") {
    enabled = true
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("realworld")
            mainClass.set("io.zhc1.realworld.RealWorldApplication")
            buildArgs.add("--verbose")
            buildArgs.add("-H:+ReportExceptionStackTraces")
            buildArgs.add("--no-fallback")
            buildArgs.add("-H:+AddAllCharsets")
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(17))
            })
        }
    }
}

tasks.named("nativeCompile") {
    dependsOn("bootJar")
}
