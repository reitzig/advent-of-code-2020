import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.20"
}
dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("script-runtime"))
    implementation("com.github.holgerbrandl", "kscript-annotations", "1.2")
}
repositories {
    mavenCentral()
    jcenter()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}
