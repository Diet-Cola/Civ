plugins {
    id("io.papermc.paperweight.userdev")
}

version = "1.5.0-SNAPSHOT-F"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:combattagplus-paper"))

    compileOnly(libs.vault.api) {
        exclude(group = "org.bukkit")
    }
}
