package com.spellsource.tasks

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceTask
import org.gradle.api.file.FileTree
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec
import javax.inject.Inject

@CacheableTask
abstract class GoTask extends SourceTask {
  @Input
  abstract ListProperty<String> getGoCommandLineArgs()

  @Internal
  abstract RegularFileProperty getWorkingDir()

  @Input
  abstract MapProperty<String, Object> getEnvironment()

  @Inject
  abstract ExecOperations getExecOperations()

  @Override
  @PathSensitive(PathSensitivity.RELATIVE)
  FileTree getSource() {
    return super.getSource()
  }

  GoTask() {
    super()
    workingDir.set(project.layout.projectDirectory.asFile)
  }

  @Override
  public Task configure(Closure closure) {
    super.configure(closure);

    def goPath = "${project.layout.buildDirectory.asFile.get().absolutePath}/go"
    def goPathDir = new File(goPath)
    def goBinary = "C:\\Program Files\\Go\\bin\\go";
    if (Os.isFamily(Os.FAMILY_MAC)) {
      if (project.file("/opt/homebrew/bin/go").exists()) {
        goBinary = "/opt/homebrew/bin/go"
      } else {
        goBinary = "/usr/local/bin/go"
      }
    } else if (Os.isFamily(Os.FAMILY_UNIX)) {
      goBinary = project.file("/usr/local/go/bin/go").exists() ? "/usr/local/go/bin/go" : "/usr/bin/go"
    }

    def wd = this.workingDir
    def env = this.environment
    def execOps = this.execOperations
    def cmdArgs = this.goCommandLineArgs
    doLast {
      execOps.exec { ExecSpec spec ->
        spec.environment("GOPATH", goPath)
        spec.environment("GOFLAGS", "-modcacherw")
        spec.workingDir(wd.get().asFile)
        spec.commandLine([goBinary] + cmdArgs.get())
        spec.environment.putAll(env.get())
      }
    }


    doFirst {
      goPathDir.mkdirs()
    }
  }
}
