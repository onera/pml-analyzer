# PML analyzer

The PML analyzer is an open source API providing a simple DSL to build
a description of the architecture of your chip based on the PHYLOG Model Language (PML).
From this representation a set of safety and interference model templates can be generated to perfom safety and
interference analyses of your platform.

The only dependencies of the PML analyzer are:
+ The Java Runtime Environment version 8 [JRE 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) or newer.
+ The Simple Build Tool [SBT](https://www.scala-sbt.org/)

## Configurations

### build.sbt

To get pml analyzer simply add the following line to your build.sbt file:
```scala
libraryDependencies += "io.github.onera" % "pml_analyzer" % "1.0.0"
```

## Installing dependencies

### Java 8

You need a working installation of the Java Runtime Environment
version 8 (either OpenJDK or Oracle will do).  Installation procedures
may vary depending on your system (Windows, OSX, Linux), please follow
the official guidelines for your system.

### SBT

The compilation of a PML model can be easily performed 
with [SBT](https://www.scala-sbt.org/). Installation procedures may vary depending on your system (Windows, OSX, Linux), 
please follow the official guidelines for your system.

### Monosat

The [Monosat](https://github.com/sambayless/monosat) enables PML Analyzer to perform interference analysis.
You should add the monosat.jar library to your class path (or put it in the lib folder) and ensure that the library (.so for Linux,
.dylib for Mac, .dll for Windows) is accessible from the java library path. If not update it by running sbt or the executable with the VM option:
```shell
# to run SBT with a given library path
java -jar -Djava.library.path=yourPath sbt-launch.jar 

# to run a JAR
java -jar -Djava.library.path=yourPath youJar.jar 
```

## Using the PML analyzer

### Overview

There is no installation procedure for the PML analyzer itself, simply
create your own model by importing the ``pml.model`` package containing the basic constructors
of the PML language. 

The possible operation that can be performed on a PML model (such as linking
or unlinking entities) are provided in the ``pml.operators``  package.

Exporters to [yuml](https://yuml.me/diagram) and [graphviz](http://www.graphviz.org/) are provided in the ``pml.exporters`` package.  
        
The compilation of a PML model can be easily perform with [SBT](https://www.scala-sbt.org/) 
+ First launch sbt in the project repository (this operation may take some time)
```sbtshell
 sbt 
```
+ Once sbt is ready to receive command launch the compilation and execution of your PML model 
```sbtshell
 runMain pathToYourModel
 ```

### Editing a PML Model

The PML analyzer is based on a platform description provided in a Scala embedded Domain Specific Language 
called PML. Therefore, PML analyzer can be seen as an API to easily build your model and to carry out automatic analyses.

Any IDE can be used to edit PML models, we can recommend [Intellij IDEA](https://www.jetbrains.com) that provides support plugins for Scala and SBT. 

Various benchmark systems for platform modeling are provided 
in the ``pml.examples`` package. These benchmarks can be used as a starting point to
your modeling activity.

#### Getting started with Intellij

To edit PML model with Intellij please follow the installation steps given by [JetBrain](https://www.jetbrains.com).
The installation can be made for any platform and does not require any administrator privilege.
Once the Intellij is installed please download the Scala and SBT Executor plugins.

#### Creating a project with Intellij

The build specifications and project structure are provided with the PML source code.
So to create a project you simply have to select "Open project" on the starting menu of Intellij and indicate the directory containing PML (where the file ``build.sbt`` is).

The tool should then configure automatically your project. 
Please add all the library in ``lib`` as project libraries by right-clicking on them and select ``Add as library`` 

The last step is to indicate the Java version of the project, to do so please go to ``File/Project Structure/Project/Project`` SDK and select ``Java 1.8``

You are now able to build, run and debug your models with Intellij

#### Troubleshooting

**Connection error while loading project or running build** If your platform uses a proxy  
please indicate the connection credentials in ``File/Settings/Appearance & Behaviour/System Settings/HTTP Proxy``

**No monosat library in path** If you want to use the integrated interference computation please indicate the path to the
dynamic library of monosat by editing your run configuration and adding to VM options ``Djava.library.path=yourPath``

### Examples

#### Argumentation patterns

The justification patterns considered for the CAST32-A are provided in the ``views.patterns`` package. 
These patterns  can be used as a starting point to start your argumentation activity.

To compile and run the PHYLOG patterns example please enter the following commands:
```sbtshell
 sbt runMain views.patterns.examples.PhylogPatterns
```

#### Modelling
Various benchmark systems for platform modeling are provided 
in the ``pml.examples`` package. These benchmarks can be used as a starting point to
your modeling activity.

To compile and run the Keystone example please enter the following commands:
```sbtshell
 sbt runMain pml.examples.keystone.SimpleKeystoneExport
```

#### Analysis
For each view (interference, patterns and dependability) examples are provided in the dedicated ``views.X.examples``.
These benchmarks can be used as a starting point to
your analysis activity. For instance, we can carry out the interference analysis of the Keystone platform with
 ```sbtshell
 # example of a PML model where an IDP interference model is generated
 sbt runMain views.interference.examples.SimpleKeystoneInterferenceGeneration
 ```

If the tool is running on a Unix System you can use the Makefile to compile the DOT and LaTeX generated file:
```shell
# compile the DOT files
make pml
 
# compile the LaTeX Argumentation Patterns
make patterns

# transform PDF to PNG
make png
``` 

### Packaging

All projects can be packaged into a single FATJAR containing all non-native dependencies.
The available projects can be obtained by running:
```sbtshell
 sbt projects
```

To export a project as a FATJAR, simply select the project to export with the previous command and then run:
```sbtshell
 sbt assembly
```
The resulting FATJAR will be produced in projectName/target/scala3.2.2

If your system contains a docker engine, you can build a docker image by running the following command:
```sbtshell
 sbt docker
```

### External tools

The PML modeling does not rely on any external dependency. Nevertheless, it is possible
to connect some backend analysis tools to directly perform analyses out of your PML model:
  * for interference analysis: [IDP](https://dtai.cs.kuleuven.be/software/idp/try) or [Monosat](https://github.com/sambayless/monosat)
  * for the safety analysis: [CeciliaOCAS]() or [OpenAltarica](https://www.openaltarica.fr/docs-downloads/) 

