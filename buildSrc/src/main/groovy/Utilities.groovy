import org.gradle.api.tasks.Exec
import org.gradle.process.ExecSpec

import java.nio.file.Files
import java.nio.file.Path

class Utilities {
    public static Properties propertiesOrEmpty(String path) {
        if (!Files.exists(Path.of(path))) {
            return new Properties();
        }

        def props = new Properties();
        new File(path).withInputStream { props.load(it) }
        return props
    }

    public static Map<String, String> env(String path) {
        if (!Files.exists(Path.of(path))) {
            return Collections.emptyMap()
        }
        def f = new File(path);
        def map = new HashMap<String, String>();
        f.readLines().each() {
            if (it.stripLeading().startsWith('#')) {
                return
            }
            def (key, value) = it.tokenize('=')
            map.put(key, value)
        }
        return map;
    }

    static def dotEnv(ExecSpec task, File f) {
        if (!f.exists()) {
            return
        }
        f.readLines().each() {
            if (it.stripLeading().startsWith('#')) {
                return
            }
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
