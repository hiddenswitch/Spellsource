package com.spellsource.tasks

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SourceTask
import org.gradle.process.ExecSpec

abstract class GoTask extends SourceTask {
  @Input
  abstract ListProperty<String> getGoCommandLineArgs()

  @Internal
  abstract RegularFileProperty getWorkingDir()

  @Input
  abstract MapProperty<String, Object> getEnvironment()

  GoTask() {
    super()
    workingDir.set(project.layout.projectDirectory.asFile)
  }

  @Override
  public Task configure(Closure closure) {
    super.configure(closure);

    def goPath = "${project.buildDir.absolutePath}/go"
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
    doLast {
      project.exec { ExecSpec spec ->
        spec.environment("GOPATH", goPath)
        spec.environment("GOFLAGS", "-modcacherw")
        spec.workingDir(wd)
        spec.commandLine([goBinary] + goCommandLineArgs.get())
        spec.environment.putAll(env.get())
      }
    }


    doFirst {
      project.mkdir(goPath)
    }
  }
}
