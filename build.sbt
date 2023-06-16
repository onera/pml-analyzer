//Definition of the managed dependencies
val sourceCode = "com.lihaoyi" %% "sourcecode" % "0.3.0"
val scalaz = "org.scalaz" %% "scalaz-core" % "7.3.7"
val scala_xml = "org.scala-lang.modules" %% "scala-xml" % "2.1.0"
val scopt = "com.github.scopt" %% "scopt" % "4.1.0"
val scalactic = "org.scalactic" %% "scalactic" % "3.2.15"
val scalatest = "org.scalatest" %% "scalatest" % "3.2.15" % "test"
val scalaplus = "org.scalatestplus" %% "scalacheck-1-15" % "3.2.11.0" % "test"

lazy val modelCode = taskKey[Seq[(String,File)]]("files to be embedded in docker")

lazy val dockerProxySetting = (
  for {
    httpProxy <- sys.env.get("http_proxy")
    httpsProxy <- sys.env.get("https_proxy")
  } yield {
    Seq( docker / dockerBuildArguments := Map(
      "http_proxy" -> httpProxy,
      "https_proxy" -> httpsProxy))
  }) getOrElse Seq.empty

lazy val dockerSettings = Seq(
  docker / imageNames := Seq(
    ImageName(
      namespace = Some(organization.value),
      repository = name.value,
      tag = Some("v" + version.value)
    )
  ),
  modelCode := Seq(
    "src/main/scala/pml/examples/simpleKeystone" -> (Compile / scalaSource).value / "pml" / "examples" / "simpleKeystone",
    "src/main/scala/pml/examples/simpleT1042" -> (Compile / scalaSource).value / "pml" / "examples" / "simpleT1042",
    "src/main/scala/views/interference/examples/simpleKeystone" -> (Compile / scalaSource).value / "views" / "interference" / "examples" / "simpleKeystone",
    "src/main/scala/views/interference/examples/simpleT1042" -> (Compile / scalaSource).value / "views" / "interference" / "examples" / "simpleT1042",
  ),
  docker / dockerfile := {
    // The assembly task generates a fat JAR file
    val artifact: File = assembly.value
    val generatedDoc = (Compile / doc).value
    val artifactTargetPath = s"/home/user/code/lib/${artifact.name}"
    val base = (Compile / baseDirectory).value
    val binlib = base / "binlib"
    new Dockerfile {
      from("openjdk:8")
      customInstruction("RUN", "apt-get update && apt-get --fix-missing update && apt-get install -y graphviz gnupg libgmp3-dev make")
      env("SBT_VERSION", sbtVersion.value)
      customInstruction("RUN", "mkdir /working/ && cd /working/ && curl -L -o sbt-$SBT_VERSION.deb https://repo.scala-sbt.org/scalasbt/debian/sbt-$SBT_VERSION.deb && dpkg -i sbt-$SBT_VERSION.deb && rm sbt-$SBT_VERSION.deb && apt-get update && apt-get install sbt && cd && rm -r /working/")
      customInstruction("RUN", "groupadd -r user && useradd --no-log-init -r -g user user")
      customInstruction("RUN", "mkdir -p /home/user/code")
      customInstruction("RUN", "mkdir -p /home/user/code/lib")
      customInstruction("RUN", "mkdir -p /home/user/code/src/main/scala/pml")
      customInstruction("RUN", "mkdir -p /home/user/code/src/main/scala/views/interference")
      workDir("/home/user/code")
      for((to,from) <- modelCode.value)
        copy(from, to)
      copy((Compile / doc / target).value, "doc")
      copy(binlib, "binlib")
      copy(artifact, artifactTargetPath)
      copy(Seq(base / "AUTHORS.txt", base / "lesser.txt", base / "minimalBuildSBT.txt", base / "LICENCE.txt", base / "Makefile"), "./")
      customInstruction("RUN", "mv minimalBuildSBT.txt build.sbt")
      env("LD_LIBRARY_PATH" -> "/home/user/code/binlib:${LD_LIBRARY_PATH}")
      customInstruction("RUN", "chown -R user /home/user && chgrp -R user /home/user")
      user("user")
      customInstruction("RUN", "sbt \"compile\" clean")
      entryPoint("/bin/bash")
    }
  }
) ++ dockerProxySetting

lazy val docSetting =
  Compile / doc / scalacOptions ++= Seq(
    "-groups",
    "-siteroot", "doc",
    "-doc-root-content", "doc/_assets/text/rootContent.txt",
    "-skip-by-regex:pml.expertises,views.dependability.*,pml.examples,views.interference.examples,pml.model.relations,views.interference.model.relations",
    "-project-logo", "doc/_assets/images/phylog_logo.gif"
  )

lazy val assemblySettings = Seq(
  assembly / assemblyJarName := s"PMLAnalyzer_${version.value}.jar",
  assembly / assemblyMergeStrategy := {
    case PathList(ps@_*) if ps.contains("patterns") => MergeStrategy.discard
    case PathList(ps@_*) if ps.contains("examples") => MergeStrategy.discard
    case x =>
      (ThisBuild / assemblyMergeStrategy).value(x)
  })

//Definition of the common settings for the projects (ie the scala version, compilation options and library resolvers)
lazy val commonSettings = Seq(
  organization := "onera",
  version := "1.0.0",
  scalaVersion := "3.2.2",
  sbtVersion := "1.8.2",
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature"),
  resolvers ++= Resolver.sonatypeOssRepos("releases"),
  resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
  libraryDependencies ++= Seq(
    scalaz,
    scala_xml,
    sourceCode,
    scalatest,
    scalactic,
    scalaplus
  ),
  docSetting
) ++ dockerSettings ++ assemblySettings

// The service project is the main project containing all the sources for
// PML modelling and analysis
lazy val PMLAnalyzer = (project in file("."))
  .enablePlugins(DockerPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "pml_analyzer")

