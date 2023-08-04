package com.spellsource.tasks

import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

class PlatformArchTuple {
  String platform
  String dockerArch
  String javaArch
    TaskProvider<Task> daemonTask
  Map<String, String> buildArgs = [:]
  String suffix
  String windowsBase
  List<String> tags = []
}
