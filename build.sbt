import sbt.Tests
import java.io.FileWriter
import java.io.PrintWriter

//Definition of the managed dependencies
val sourceCode = "com.lihaoyi" %% "sourcecode" % "0.3.0"
val scalaz = "org.scalaz" %% "scalaz-core" % "7.3.7"
val scala_xml = "org.scala-lang.modules" %% "scala-xml" % "2.1.0"
val scopt = "com.github.scopt" %% "scopt" % "4.1.0"
val scalactic = "org.scalactic" %% "scalactic" % "3.2.15"
val scalatest = "org.scalatest" %% "scalatest" % "3.2.15" % "test"
val scalaplus = "org.scalatestplus" %% "scalacheck-1-15" % "3.2.11.0" % "test"
val parallel = "org.scala-lang.modules" %% "scala-parallel-collections" % "1.1.0"
val choco = "org.choco-solver" % "choco-solver" % "5.0.0-beta.1"
val javaBdd = "com.github.com-github-javabdd" % "com.github.javabdd" % "10.0.0"
val fastParse = "com.lihaoyi" %% "fastparse" % "3.1.1"

lazy val writeMinimalBuildSBT = taskKey[File]("Write minimal build.sbt for Docker usage")

writeMinimalBuildSBT := {
  val fileName = new File("minimalBuildSBT.txt")
  val content =
    s"""val scalatest = "org.scalatest" %% "scalatest" % "3.2.15" % "test"
       |val scalaplus = "org.scalatestplus" %% "scalacheck-1-15" % "3.2.11.0" % "test"
       |
       |lazy val root = (project in file("."))
       |  .settings(
       |    name := "myProject",
       |    version := "1.0.0",
       |    scalaVersion := "${scalaVersion.value}",
       |    sbtVersion := "${sbtVersion.value}",
       |    scalacOptions := Seq("-unchecked", "-deprecation", "-feature"),
       |    resolvers += Resolver.sonatypeCentralSnapshots,
       |    libraryDependencies ++= Seq(
       |        scalatest,
       |        scalaplus
       |      )
       |  )
       |
       |// Fork a new JVM on every sbt run task
       |// Fixes an issue with the classloader complaining that the Monosat library is
       |// "already loaded in another classloader" on successive runs of the analysis
       |// in the same sbt instance.
       |fork := true""".stripMargin
  val writer = new PrintWriter(new FileWriter(fileName))
  writer.println(content)
  writer.close()
  println(s"[INFO] File $fileName written successfully.")
  fileName
}

lazy val modelCode =
  taskKey[Seq[(File, String)]]("files to be embedded in docker")

modelCode := Seq(
    (examples / Compile / scalaSource).value / "generic" -> "src/main/scala/generic",
    (examples / Compile / scalaSource).value / "keystone" -> "src/main/scala/keystone",
    (examples / Compile / scalaSource).value / "riscv" -> "src/main/scala/riscv",
    (examples / Compile / scalaSource).value / "mySys" -> "src/main/scala/mySys",
    (PMLAnalyzer / Compile / baseDirectory).value / "src" / "test" -> "src/test"
  )


lazy val dockerProxySetting = (for {
  httpProxy <- sys.env.get("http_proxy")
  httpsProxy <- sys.env.get("https_proxy")
} yield {
  Seq(
    docker / dockerBuildArguments := Map(
      "http_proxy" -> httpProxy,
      "https_proxy" -> httpsProxy
    )
  )
}) getOrElse Seq.empty

lazy val dockerSettings = Seq(
  docker / imageNames := Seq(
    ImageName(
      namespace = Some(organization.value),
      repository = name.value,
      tag = Some("v" + version.value.split("\\+").head)
    )
  ),
  docker / dockerfile := {
    // The assembly task generates a fat JAR file
    val artifact: File = assembly.value
    val generatedDoc = (Compile / doc).value
    val minimalBuildSBT = writeMinimalBuildSBT.value
    val artifactTargetPath = s"/home/user/code/lib/${artifact.name}"
    val base = (Compile / baseDirectory).value
    new Dockerfile {
      from("ubuntu:latest")
      customInstruction("RUN", "apt-get update && apt-get --fix-missing update && apt-get install -y curl openjdk-17-jdk git graphviz gnupg libgmp3-dev make cmake build-essential zlib1g-dev snapd")
      customInstruction("RUN", "snap install minizinc --classic")
      env("SBT_VERSION", sbtVersion.value)
      customInstruction(
        "RUN",
        "mkdir /working/ && cd /working/ && curl -L -o sbt-$SBT_VERSION.deb https://repo.scala-sbt.org/scalasbt/debian/sbt-$SBT_VERSION.deb && dpkg -i sbt-$SBT_VERSION.deb && rm sbt-$SBT_VERSION.deb && apt-get update && apt-get install sbt && cd && rm -r /working/"
      )
      customInstruction(
        "RUN",
        "groupadd -r user && useradd --no-log-init -r -g user user"
      )
      customInstruction("RUN", "mkdir -p /home/user/code")
      customInstruction("RUN", "mkdir -p /home/user/code/lib")
      customInstruction("RUN", "mkdir -p /home/user/code/binlib")
      customInstruction("RUN", "mkdir -p /home/user/code/src/main/scala")
      customInstruction("RUN", "mkdir -p /home/user/code/src/test")
      workDir("/home/user")
      customInstruction("RUN", "git clone https://github.com/sambayless/monosat.git")
      workDir("/home/user/monosat")
      customInstruction("RUN", "cmake -DJAVA=ON .")
      customInstruction("RUN", "make")
      customInstruction("RUN", "cp libmonosat.so /home/user/code/binlib")
      workDir("/home/user/code")
      for ((from, to) <- modelCode.value)
        copy(from, to)
      copy((Compile / doc / target).value, "doc")
      copy(artifact, artifactTargetPath)
      copy(Seq(base / "AUTHORS.txt", base / "lesser.txt", base / "minimalBuildSBT.txt", base / "LICENSE", base / "Makefile"), "./")
      customInstruction("RUN", s"mv ${minimalBuildSBT.name} build.sbt")
      env("LD_LIBRARY_PATH" -> "/home/user/code/binlib:${LD_LIBRARY_PATH}")
      customInstruction(
        "RUN",
        "chown -R user /home/user && chgrp -R user /home/user"
      )
      user("user")
      customInstruction("RUN", "sbt \"compile\" clean ")
      entryPoint("/bin/bash")
    }
  }
) ++ dockerProxySetting

lazy val docSetting =
  Compile / doc / scalacOptions ++= Seq(
    "-groups",
    "-siteroot",
    "doc",
    "-doc-root-content",
    "doc/_assets/text/rootContent.txt",
    "-skip-by-regex:onera.pmlanalyzer.views.dependability.*",
    "-project-logo",
    "doc/_assets/images/phylog_logo.gif"
  )

lazy val assemblySettings = Seq(
  assembly / assemblyJarName := s"PMLAnalyzer_${version.value}.jar",
  assembly / assemblyMergeStrategy := {
    case PathList(ps@_*) if ps.contains("patterns") || ps.contains("examples") => MergeStrategy.discard
    case x =>
      (ThisBuild / assemblyMergeStrategy).value(x)
  }
)

lazy val compileSettings = Seq(
  scalaVersion := "3.3.5",
  sbtVersion := "1.11.2",
  scalafixOnCompile := true,
  semanticdbEnabled := true,
  scalafmtOnCompile := true,
  scalafixDependencies += "io.github.dedis" %% "scapegoat-scalafix" % "1.1.4",
  semanticdbVersion := scalafixSemanticdb.revision,
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-Werror")
)

lazy val testSettings = Seq(
  Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-n", "UnitTests", "-n", "FastTests")
)

lazy val dependencySettings = Seq(
  resolvers += Resolver.sonatypeCentralSnapshots,
  libraryDependencies ++= Seq(
    scalaz,
    scala_xml,
    sourceCode,
    scalatest,
    scalactic,
    scalaplus,
    parallel,
    choco,
    javaBdd,
    fastParse
  )
)

//Definition of the common settings for the projects (ie the scala version, compilation options and library resolvers)
lazy val publishSettings = Seq(
  organization := "io.github.onera",
  homepage := Some(url("https://github.com/onera/pml-analyzer")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/onera/pml-analyzer"),
      "git@github.com:onera/pml-analyzer.git"
    )
  ),
  developers := List(
    Developer(
      "kevin-delmas",
      "kevin-delmas",
      "kevin.delmas@onera.fr",
      url("https://www.onera.fr/en/staff/kevin-delmas")
    )
  ),
  licenses += (
    "LGPL-2.1",
    url("https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html")
  ),
  versionScheme := Some("early-semver")
)

lazy val examples = (project in file("examples"))
  .dependsOn(PMLAnalyzer)
  .settings(
    compileSettings,
    name := "examples",
    publish / skip := true
  )


// The service project is the main project containing all the sources for
// PML modelling and analysis
lazy val PMLAnalyzer = (project in file("."))
  .enablePlugins(DockerPlugin)
  .settings(
    compileSettings,
    dependencySettings,
    docSetting,
    publishSettings,
    dockerSettings,
    assemblySettings,
    testSettings,
    name := "pml_analyzer",
    addCommandAlias("testPerf", "; set Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, \"-n\", \"PerfTests\", \"-l\", \"UnitTests\", \"-l\", \"FastTests\") ; test")
  )

// Fork a new JVM on every sbt run task
// Fixes an issue with the classloader complaining that the Monosat library is
// "already loaded in another classloader" on successive runs of the analysis
// in the same sbt instance.
fork := true
