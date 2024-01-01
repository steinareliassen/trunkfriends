import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.osprey"
version = "0.3.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing")
    implementation("commons-io:commons-io:2.14.0")
}

compose.desktop {
    application {
        mainClass = "org.osprey.trunkfriends.ui.TrunkUIKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Trunkfriends"
            packageVersion = "0.4.0"
            windows {
                menuGroup = "Trunkfriends"
                upgradeUuid = "cc9631f7-88bf-4520-86f8-242f0615caf7"
                iconFile.set(project.file("icon.ico"))
                modules("java.net.http")
            }
            linux {
                iconFile.set(project.file("icon.png"))
                debMaintainer = "steinar.eliassen@gmail.com"
                appRelease = "1"
            }
            macOS {
                iconFile.set(project.file("icon.png"))
            }
        }
    }
}

tasks.withType<Test> {
	useJUnitPlatform()
}
