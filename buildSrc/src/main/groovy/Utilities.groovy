import org.gradle.api.tasks.Exec

class Utilities {
    static def dotEnv(Exec task, File f) {
        if (!f.exists()) {
            return
        }
        f.readLines().each() {
            def (key, value) = it.tokenize('=')
            task.environment(key, value)
        }
    }

    static def gitIgnore(File f) {
        if (!f.exists()) {
            return [] as List<String>
        }
        def ignores = []
        f.eachLine { line ->
            //ignore comments and empty lines
            if (!line.startsWith('#') && !line.isEmpty()) {
                ignores.add(line)
            }
        }
        return ignores
    }
}
