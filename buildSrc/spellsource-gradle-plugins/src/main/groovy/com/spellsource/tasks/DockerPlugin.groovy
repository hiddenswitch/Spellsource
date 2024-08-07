package com.spellsource.tasks

import com.bmuschko.gradle.docker.DockerExtension
import com.bmuschko.gradle.docker.DockerRemoteApiPlugin
import com.bmuschko.gradle.docker.tasks.container.*
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import com.github.dockerjava.core.dockerfile.Dockerfile
import com.github.dockerjava.core.dockerfile.DockerfileStatement
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ecr.EcrClient

class DockerPlugin implements Plugin<Project> {


  @Override
  void apply(Project project) {
    project.plugins.apply(DockerRemoteApiPlugin.class)
    var useWindowsDockerDaemon = project.tasks.register('useWindowsDockerDaemon')
    var useLinuxDockerDaemon = project.tasks.register('useLinuxDockerDaemon')
    // todo: make this work in other projects
    def awsAccountId = project.properties['awsAccountId'] as String
    def awsRegion = project.properties['awsRegion'] as String

    project.afterEvaluate {
      project.extensions.configure(DockerExtension, {
        it.registryCredentials {
          // requires the user to have sourced spellsource-cluster/src/source-me.sh or to have valid credentials
          try (def ecrClient = EcrClient.builder().region(Region.of(awsRegion)).build()) {
            def token = ecrClient.getAuthorizationToken({})
            def authData = token.authorizationData().get(0).authorizationToken()
            def decodedPassword = new String(authData.decodeBase64()).split(':')[1]
            // todo: we should really retrieve this dynamically some how...
            url.set("https://${awsAccountId}.dkr.ecr.${awsRegion}.amazonaws.com")
            username.set('AWS')
            password.set(decodedPassword)
          } catch (Throwable ignored) {

          }
        }
      })

      def version = System.getenv("PROJECT_VERSION") ?: project.version

      if (version != null && version != "unspecified") {
        project.tasks.findAll { it instanceof DockerPushImage || it instanceof DockerBuildImage }.forEach({
          it.configure { task ->
            Set<String> images = task.getImages().get()
            def imagesWithVersion = images
                    .findAll { image -> image.contains("latest") }
                    .collect { image -> image.replace("latest", version.toString()) }
            imagesWithVersion.addAll(images)
            task.getImages().set(imagesWithVersion)
          }
        })
      }

    }

    useWindowsDockerDaemon.configure(it -> {
      onlyIf {
        if (!Os.isFamily(Os.FAMILY_WINDOWS)) {
          return false
        }
        def versionOutput = new ByteArrayOutputStream()
        project.exec {
          commandLine("docker", "version", "-f", "{{.Server.Os}}")
          standardOutput(versionOutput)
        }
        return !versionOutput.toString().trim().equalsIgnoreCase("windows")
      }
      doLast {
        if (!Os.isFamily(Os.FAMILY_WINDOWS)) {
          throw new GradleException('Cannot build or use Windows containers on non-Windows platforms, so dependencies on the Windows docker daemon cannot be used on your current platform')
        }
        if (project.file("C:\\Program Files\\Docker\\Docker\\DockerCli.exe").exists()) {
          project.exec {
            commandLine "C:\\Program Files\\Docker\\Docker\\DockerCli.exe", "-SwitchWindowsEngine"
          }
        }
      }
    })
    useLinuxDockerDaemon.configure({
      onlyIf {
        if (!Os.isFamily(Os.FAMILY_WINDOWS)) {
          return false
        }
        def versionOutput = new ByteArrayOutputStream()
        project.exec {
          commandLine("docker", "version", "-f", "{{.Server.Os}}")
          standardOutput(versionOutput)
        }
        return !versionOutput.toString().trim().equalsIgnoreCase("linux")
      }
      doLast {
        project.exec {
          commandLine "C:\\Program Files\\Docker\\Docker\\DockerCli.exe", "-SwitchLinuxEngine"
        }
      }
    })

    def thisOsArch = System.properties['os.arch'] as String

    // enumerate the docker files
    ((project.fileTree('docker') + project.fileTree('src')).matching {
      include "**/*Dockerfile"
    } + project.files("Dockerfile"))
            .each { dockerFileFile ->
              if (!dockerFileFile.exists()) {
                return
              }
              def parsedDockerfile = new Dockerfile(dockerFileFile, dockerFileFile.parentFile)
              def parseResult = parsedDockerfile.parse()
              def windowsBaseImages = [
                      'mcr.microsoft.com/windows',
                      'spellsource/unityruntimewindows',
                      '270666084746.dkr.ecr.us-west-2.amazonaws.com/unityruntimewindows',
                      'unityci/editor:windows'
              ]

              def isWindowsImage = parsedDockerfile.statements.any { stmt ->
                if (stmt instanceof DockerfileStatement.OtherLine) {
                  def ol = (DockerfileStatement.OtherLine) stmt
                  return windowsBaseImages.any { ol.statement.contains "FROM $it" } || ol.statement.contains("ARG WINRELEASE=") || ol.statement.contains("ARG WINBASE")
                }
                return false
              }

              def platformArchTuples = new ArrayList<PlatformArchTuple>()
              if (isWindowsImage) {
                def winRelease = "WINRELEASE"
                def envPrefix = "GRADLE_DOCKER_ARG_"
                def tags = ["latest"]

                def buildArgPattern = ~/(?m)^ARG\s+(\w+)=(.*)$/
                def matcher = buildArgPattern.matcher(dockerFileFile.text)
                Map<String, String> args = new HashMap<String, String>()
                if (matcher.find()) {
                  args = matcher.results().toArray().collectEntries { [it.group(1).toString(), it.group(2).toString()] }
                }

                def customTagPattern = ~/(?m)^\s*#\s*tag\s*=\s*(.*)$/
                matcher = customTagPattern.matcher(dockerFileFile.text)

                args.keySet().each { k ->
                  def envName = envPrefix + k;
                  if (System.getenv().containsKey(envName)) {
                    def value = System.getenv().get(envName)
                    args.put(k, value)
                    project.logger.info("Setting gradle docker arg ${k} to ${value}")
                  }
                }

                if (matcher.find()) {
                  var tag = matcher.group(1)
                  args.each { k, v ->
                    tag = tag.replaceAll('\\$\\{' + k + '\\}', v)
                  }
                  tags.add(tag)
                }

                platformArchTuples.addAll(Docker.windowsVersions.collect { windowsBase ->
                  def buildArgs = new HashMap<String, String>(args)
                  buildArgs.put(winRelease, windowsBase)

                  return new PlatformArchTuple(
                          dockerArch: "amd64",
                          javaArch: "x86_64",
                          platform: "windows",
                          daemonTask: useWindowsDockerDaemon,
                          buildArgs: buildArgs,
                          suffix: "windows_$windowsBase",
                          windowsBase: windowsBase,
                          tags: tags.collect { "$it-$windowsBase" }
                  )
                })
              } else {
                platformArchTuples.addAll([["amd64", "x86_64"], ["arm64", "aarch64"]].collectMany { archTuple ->
                  def dockerArch = archTuple[0]
                  def javaArch = archTuple[1]

                  return ["linux"].collect { platform ->

                    def tags = ["latest-$dockerArch".toString()]
                    if (dockerArch == 'amd64') {
                      tags.add("latest")
                    }
                    return new PlatformArchTuple(
                            dockerArch: dockerArch,
                            javaArch: javaArch,
                            platform: platform,
                            daemonTask: useLinuxDockerDaemon,
                            suffix: "${platform}_${dockerArch}",
                            tags: tags
                    )
                  }
                } as List<PlatformArchTuple>)
              }

              def nameComponents = dockerFileFile.name.split("\\.")
              def variant = ""
              if (nameComponents.length > 1) {
                variant = "-" + nameComponents[0..-2].join('-')
              }
              def isRootDockerfile = dockerFileFile.parentFile.equals(project.projectDir)
              def rootProjectName = project.rootProject.name
              def imageName = isRootDockerfile && dockerFileFile.parentFile.name.contains(rootProjectName) ? "${dockerFileFile.parentFile.name.replace(rootProjectName + "-", "")}$variant" : "${dockerFileFile.parentFile.name}$variant"
              def repositoryName = isRootDockerfile ? rootProjectName.toLowerCase() : "${dockerFileFile.parentFile.parentFile.name.toLowerCase()}"
              def localTag = "$repositoryName/$imageName:latest"
              def thisContainerName = "${repositoryName}_${imageName}"
              TaskProvider<DockerBuildImage> thisArchImage = null;
              def remoteDestination = "ghcr.io/hiddenswitch/spellsource/${imageName}"
              def prepareContext = project.tasks.register("prepareContext_${repositoryName}_${imageName}") {
                group = 'docker'
              }

              def createImages = platformArchTuples.collect { platformArchTuple ->
                def arch = platformArchTuple.dockerArch
                def osArch = platformArchTuple.javaArch
                def dockerPlatform = "$platformArchTuple.platform/$arch"
                def correctLocalArch = thisOsArch.contains(osArch) || thisOsArch.contains(arch)
                def ecrImages = platformArchTuple.tags.collect { tag ->
                  return "$remoteDestination:$tag".toString()
                }

                def createCraneImageTask = project.tasks.register("createAndPushImageCrane_${repositoryName}_${imageName}_${platformArchTuple.suffix}") { task ->
                  group = 'docker'
                  dependsOn prepareContext
                  inputs.dir(project.relativeProjectPath(dockerFileFile.parent))


                  def reversed = parsedDockerfile.statements.asList()
                  Collections.reverse(reversed)
                  def lastFrom = reversed.find {
                    it instanceof DockerfileStatement.OtherLine && it.statement.startsWith("FROM")
                  }.collect {
                    def line = (DockerfileStatement.OtherLine) it
                    def regexp = ~/\$\w+/

                    def baseImage = line.statement.split("\\s+")[1]
                    return baseImage.replaceAll(regexp, platformArchTuple.windowsBase ?: "")
                  }.first()

                  doLast {
                    def layerPath = "$task.temporaryDir/img.tar"
                    def target = project.file(layerPath)
                    target.createNewFile()
                    try (def tar = parseResult.buildDockerFolderTar()) {
                      try (def targetOutputStream = new FileOutputStream(target)) {
                        tar.transferTo(targetOutputStream)
                      }
                    }

                    ecrImages.forEach { tag ->
                      project.exec {
                        commandLine "crane", "append", "--platform=${platformArchTuple.platform}/${platformArchTuple.dockerArch}", "-f", layerPath, "-t", tag, "-b", lastFrom
                      }
                    }
                  }
                }

                def createImageTask = project.tasks.register("createImage_${repositoryName}_${imageName}_${platformArchTuple.suffix}", DockerBuildImage) {
                  group = 'docker'
                  dependsOn prepareContext, platformArchTuple.daemonTask
                  onlyIf { correctLocalArch || Boolean.parseBoolean(project.findProperty("docker.builder") as String ?: "false") }
                  inputDir.set(project.file(dockerFileFile.parent))
                  dockerFile.set(dockerFileFile)
                  quiet.set(false)
                  platform.set(dockerPlatform)
                  buildArgs.putAll(platformArchTuple.buildArgs)
                  if (arch == 'amd64') {
                    images.addAll(ecrImages)
                  }
                  if (correctLocalArch) {
                    images.add(localTag)
                  }
                }

                if (correctLocalArch) {
                  thisArchImage = createImageTask
                }

                if (arch == 'amd64') {
                  def pushImageTask = project.tasks.register("pushImage_${repositoryName}_${imageName}_${platformArchTuple.suffix}", DockerPushImage) {
                    onlyIf { arch == 'amd64' }
                    group = 'docker'
                    dependsOn createImageTask
                    // todo: we really want this to be smarter
                    if (arch == 'amd64') {
                      images.set(ecrImages)
                    }

                  }
                }


                return createImageTask
              }

              def createAndPushManifestCrane = project.tasks.register("createAndPushManifestCrane_${repositoryName}_${imageName}") {
                dependsOn(project.tasks.collect { it.name.startsWith("createAndPushImageCrane_${repositoryName}_${imageName}") })
                def ecrImages = platformArchTuples.collectMany {
                  it.tags.collect { tag ->
                    return "$remoteDestination:$tag".toString()
                  }
                }

                def latestTag = "$remoteDestination:latest"
                def commands = ["docker", "manifest", "create", latestTag] + ecrImages.collectMany { ["--amend", it] }

                doLast {
                  project.exec {
                    commandLine(commands)
                  }
                  project.exec {
                    commandLine "docker", "manifest", "push", latestTag
                  }
                }
              }

              def createImageKaniko = project.tasks.register("createImageKaniko_${repositoryName}_${imageName}") {
                group = 'docker'
                dependsOn prepareContext
                def kanikoBin = "/kaniko/executor"
                onlyIf {
                  try {
                    return project.file(kanikoBin).exists()
                  } catch (Throwable ignored) {
                    return false
                  }
                }
                doFirst {
                  project.file('/kaniko/.docker/config.json').text = "{ \"credsStore\": \"ecr-login\" }"
                }
                doLast {
                  project.exec {
                    commandLine kanikoBin, "--dockerfile=${dockerFileFile.absolutePath}", "--context=${project.file(dockerFileFile.parent).absolutePath}", "--destination=${remoteDestination}"
                  }
                }
              }

              def stopContainerTask1 = project.tasks.register("stopContainer1_${repositoryName}_${imageName}", DockerStopContainer) {
                group = 'docker'
                containerId.set(thisContainerName)
                onError({})
              }

              // work around circular dependency
              def stopContainerTask = project.tasks.register("stopContainer_${repositoryName}_${imageName}", DockerStopContainer) {
                group = 'docker'
                containerId.set(thisContainerName)
                onError({})
              }

              def removeExistingContainerTask = project.tasks.register("removeExistingContainer_${repositoryName}_${imageName}", DockerRemoveContainer) {
                group = 'docker'
                dependsOn stopContainerTask1
                containerId.set(thisContainerName)
                onError({})
              }

              def createContainerTask = project.tasks.register("createContainer_${repositoryName}_${imageName}", DockerCreateContainer) {
                group = 'docker'
                if (thisArchImage != null) {
                  dependsOn thisArchImage
                }
                dependsOn removeExistingContainerTask
                imageId.set(localTag)
                containerName.set(thisContainerName)
                // hostConfig.autoRemove.set(true)
              }

              def startContainerTask = project.tasks.register("startContainer_${repositoryName}_${imageName}", DockerStartContainer) {
                group = 'docker'
                dependsOn createContainerTask, useLinuxDockerDaemon
                targetContainerId createContainerTask.get().getContainerId()
                doLast {
                  sleep(1000)
                }
              }

              def waitContainerTask = project.tasks.register("waitContainer_${repositoryName}_${imageName}", DockerWaitContainer) {
                group = 'docker'
                dependsOn createContainerTask, useLinuxDockerDaemon
                targetContainerId createContainerTask.get().getContainerId()
              }
            }
  }
}
