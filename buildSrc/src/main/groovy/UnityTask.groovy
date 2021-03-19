import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class UnityTask extends AbstractExecTask<UnityTask> {

    @Input
    String flags = ''

    @Input
    String outputDir = ''

    Provider<BuildService<BuildServiceParameters>> unity3d

    UnityTask() {
        super(UnityTask.class)
    }

    @Override
    Task configure(Closure closure) {
        dependsOn += ':spellsource-protos:protosChanged'
        unity3d = project.gradle.sharedServices.registerIfAbsent("unity3d", BuildService) {
            maxParallelUsages.set(1)
        }
        usesService(unity3d)
        inputs.dir("src/unity/Assets/")
        inputs.files("src/unity/ProjectSettings/EditorBuildSettings.asset")
        inputs.dir("src/unity/Packages/")
        outputs.dir("${project.buildDir}/${outputDir}")
        return super.configure(closure)
    }

    @TaskAction
    protected void exec() {
        workingDir "${project.projectDir}/src/unity/"
        commandLine './deploy.sh', flags
        super.exec()
    }
}