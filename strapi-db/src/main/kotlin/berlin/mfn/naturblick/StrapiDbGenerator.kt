package berlin.mfn.naturblick

import org.gradle.api.*

class StrapiDbGenerator : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register(
            "syncReleaseDb",
            SyncDb::class.java
        )
        project.tasks.register(
            "syncDebugDb",
            SyncDb::class.java
        )
        project.tasks.register(
            "syncReleaseMedia",
            SyncMedia::class.java
        )
        project.tasks.register(
            "syncDebugMedia",
            SyncMedia::class.java
        )
    }
}
