package com.spellsource.utils

import javax.inject.Inject
import org.gradle.process.ExecOperations

abstract class ProcessOperations {
  @Inject
  abstract ExecOperations getExecOperations()
}
