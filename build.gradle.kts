import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.20"
}
dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("script-runtime"))
    implementation("com.github.holgerbrandl", "kscript-annotations", "1.2")

    implementation("com.github.h0tk3y.betterParse", "better-parse", "0.4.0-1.4-M2")
}
repositories {
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/hotkeytlt/maven")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}
