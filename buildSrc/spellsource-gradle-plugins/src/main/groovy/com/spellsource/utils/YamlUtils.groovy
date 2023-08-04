package com.spellsource.utils


import org.yaml.snakeyaml.Yaml

class YamlUtils {
  static def readYaml(String path) {
    return readYaml(new File(path))
  }
  static def readYaml(File file) {
    try (def inputStream = file.newInputStream()) {
      return new Yaml().load(inputStream)
    }
  }
}
