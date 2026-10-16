plugins {
    // AGP 8.10.1 : premiere lignee supportant compileSdk 36, exigee par Play
    // depuis le durcissement du niveau d'API cible. Reclame Gradle 8.11+,
    // la CI utilise 8.14.2.
    id("com.android.application") version "8.10.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
