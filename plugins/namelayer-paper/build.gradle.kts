plugins {
    id("io.papermc.paperweight.userdev")
}

version = "3.0.6-F"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    compileOnly(project(":plugins:civmodcore-paper"))
}
