package berlin.mfn.naturblick

import berlin.mfn.naturblick.strapi.KtorApi
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class SyncDb : DefaultTask() {
    init {
        description = "Downloads some strapi data and creates a SQL db file"
    }

    @OutputFile
    val speciesDbSql: RegularFileProperty = project.objects.fileProperty().convention {
        File(project.buildDir, "assets/strapi-db.sqlite3")
    }

    @TaskAction
    fun doAction() {
        val dbFile = speciesDbSql.get().asFile
        dbFile.delete()

        runBlocking {
            val service = KtorApi.service("https://naturblick.museumfuernaturkunde.berlin/")
            try {
                dbFile.writeBytes(service.getFile().bytes())
            } catch(e: Error) {
                logger.error("Error during call to => ktor <= best you consider to look there as well", e)
            }
        }
    }
}

