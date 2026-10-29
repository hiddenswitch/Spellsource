import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.model.ObjectFactory

import javax.inject.Inject


class UnityTask extends AbstractExecTask<UnityTask> {

    @Input
    String flags = ''

    @Input
    String outputDir = ''

    @Inject
    ObjectFactory getObjectFactory() {}

    @Inject
    org.gradle.process.internal.ExecActionFactory getExecActionFactory() {}

    @Internal
    Provider<BuildService<BuildServiceParameters>> unity3d

    private final File projectDirFile
    private final File rootProjectDirFile
    private final File buildDirFile

    UnityTask() {
        super(UnityTask.class)
        this.projectDirFile = project.projectDir
        this.rootProjectDirFile = project.rootProject.projectDir
        this.buildDirFile = project.layout.buildDirectory.asFile.get()
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
        outputs.dir("${buildDirFile}/${outputDir}")
        return super.configure(closure)
    }

    @TaskAction
    protected void exec() {
        workingDir "${projectDirFile}/src/unity/"
        def isWindows = Os.isFamily(Os.FAMILY_WINDOWS)
        def commandLineArgs = ['./deploy.sh', flags]
        if (isWindows) {
            commandLineArgs = ["${rootProjectDirFile}/bin/bash.exe"] + commandLineArgs
        }
        commandLine commandLineArgs
        super.exec()
    }
}