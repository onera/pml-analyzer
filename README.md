<div align="center">
        <picture>
                <img src="doc/_assets/images/phylog2.png" alt="Library Banner">
        </picture>
</div>
<br>

<!-- Badge section -->
<div align="center">
   <a href="https://github.com/onera/pml-analyzer/blob/master/README.md">
        <img alt="License LGPL" src="https://img.shields.io/badge/scala-3.2.2+-red"></a>
   <a href="https://github.com/onera/pml-analyzer/actions/workflows/scala-test.yml">
        <img alt="Tests" src="https://github.com/onera/pml-analyzer/actions/workflows/scala-test.yml/badge.svg"></a>
   <a href="https://github.com/onera/pml-analyzer/LICENSE">
        <img alt="License LGPL" src="https://img.shields.io/badge/License-LGPLv2.1-efefef"></a>
</div>
<br>

# PML analyzer

The PML analyzer is an open source API providing a simple DSL to build
a description of the architecture of your chip based on the PHYLOG Model Language (PML).
From this representation a set of safety and interference model templates can be generated to perfom safety and
interference analyses of your platform.
You can find a detailed documentation of PML Analyzer [here](https://onera.github.io/pml-analyzer-docs/)

The only dependencies of the PML analyzer are:
+ The Java Runtime Environment version 8 [JRE 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) or newer.
+ The Simple Build Tool [SBT](https://www.scala-sbt.org/)

## Configurations

### build.sbt

To get the version X.Y.Z of pml analyzer simply add the following line to your build.sbt file:
```scala
libraryDependencies += "io.github.onera" % "pml_analyzer" % "X.Y.Z"
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
### Cecilia

It is possible to export PML models as [CeciliaOCAS](https://satodev.com/nos-produits/cecilia-workshop/) or [OpenAltarica](https://www.openaltarica.fr/docs-downloads/) models and perform automatic analyses or simulation out of these models.

### IDP

It is possible to use [IDP](https://dtai.cs.kuleuven.be/software/idp/try) as an alternative to MONOSAT to perform the interference analyses out of your PML model.

## Using the PML analyzer

### Overview

There is no installation procedure for the PML analyzer itself, simply
create your own model by importing the ``pml.model`` package containing the basic constructors
of the PML language. 

The possible operation that can be performed on a PML model (such as linking
or unlinking entities) are provided in the ``pml.operators``  package.

Exporters to [yuml](https://yuml.me/diagram) and [graphviz](http://www.graphviz.org/) are provided in the ``pml.exporters`` package.  
        
The compilation of a PML model can be easily perform with [SBT](https://www.scala-sbt.org/) 
Simply run the following command in the project repository (this operation may take some time)
```shell
 sbt runMain pathToYourModel
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
Once the Intellij IDE is installed please download the Scala and SBT Executor plugins.
More information are available on the  [Intellij Scala 3 support guide](https://dotty.epfl.ch/3.0.0/docs/usage/ide-support.html)
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

The justification patterns considered for the AMC20-193 (former CAST32-A) are provided in the ``views.patterns`` package. 
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
 sbt runMain pml.examples.simpleKeystone.SimpleKeystoneExport
```

#### Analysis
For each view (interference, patterns and dependability) examples are provided in the dedicated ``views.X.examples``.
These benchmarks can be used as a starting point to
your analysis activity. For instance, we can carry out the interference analysis of the Keystone platform with
 ```sbtshell
 # example of a PML model where a MONOSAT based interference identification is performed
 sbt runMain views.interference.examples.simpleKeystone.SimpleKeystoneInterferenceGeneration
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

### Package your model

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

### Use docker image of PML Analyzer

The simplest way to use PML Analyzer is to download a pre-configured Docker image to run interference analyses.
The only dependency is the [Docker execution engine](https://docs.docker.com/get-docker/).

#### Getting started with Docker

Docker enables you to create from this image a container where all dependencies have been already resolved.

##### Container for a simple execution of PML examples

The following commands create a simple container that can be used to run the examples provided in PML Analyzer.

```shell
# load directly the archive containing the image of the preconfigured PML Analyzer environment
# note that this step is not needed if you use sbt docker command that already load the image among the available
# docker images
docker load < [IMAGE_NAME].tar.gz

# The image has been configured to run as a non-root user
# To share content between the container and the host, it is mandatory to create
# the directories upfront with the write rights
mkdir [PATH_TO_SHARED_FOLDER]/[SHARED_FOLDER_NAME]
chmod -R a+w [PATH_TO_ANALYSIS_RESULT]/[SHARED_FOLDER_NAME]

# In particular, it is interesting to share a folder to store the interference analysis results [PATH_TO_ANALYSIS_RESULT]
# and to share a folder to store the export results [PATH_TO_EXPORT_RESULT]

# To run a 'one-shot' container (removed after exit) from an image with shared directories for the results.
# Note that directory sharing is not mandatory, one can just run the container without the -v options
docker run -it --rm \
-v [PATH_TO_ANALYSIS_RESULT]:/home/user/code/analysis \
-v [PATH_TO_EXPORT_RESULT]:/home/user/code/export \
[IMAGE_NAME]
```

##### Container for platform modelling

You can create a container that also share some source files describing your own platform [MY_PLATFORM]. Let us consider
that the source files
describing you platform are located in [PLATFORM_MODEL_PATH] and the interference specifications are located in
[PLATFORM_INTERFERENCE_SPECIFICATION_PATH]. You can add these files as source code of the project by sharing them with
the
container as follows:

```shell
# In case the user wants to create its own platform [MY_PLATFORM],
# Create share a folder to store the models [PATH_TO_MODEL]
# Create share a folder to store the specification [PATH_TO_INTERFERENCE_SPECIFICATION]
docker run -it --rm \
-v [PATH_TO_MODEL]:/home/user/code/src/main/scala/onera/pmlanalyzer/pml/examples/[MY_PLATFORM] \
-v [PATH_TO_INTERFERENCE_SPECIFICATION]:/home/user/code/src/main/scala/onera/pmlanalyzer/views/interference/examples/[MY_PLATFORM] \
-v [PATH_TO_ANALYSIS_RESULT]:/home/user/code/analysis \
-v [PATH_TO_EXPORT_RESULT]:/home/user/code/export \
[IMAGE_NAME]
```

##### Running PML Analyzer in Docker

Once the container is run in an interactive mode, all the examples provided in the following sections can be run by
using SBT.
To display all the possible entry-points of the project, especially your models, just execute the following command

```shell
sbt run
```

Note that you can edit your code on the host and re-build it in the container by simply running again

```shell
sbt compile
```

You can also indicate the memory allocated to SBT using the `-J-XmxNG` option where N
is the number of Go.

```shell
# Allocate 4Go to run SBT
sbt -J-Xmx4G run
```

##### Exchanging files between container and host

The simplest way to retrieve files from the container or to modify internal files is to use the
`docker cp` command.

```shell
# From the container to the host
docker cp  CONTAINER:SRC_PATH DEST_PATH

# From the host to the container
docker cp  DEST_PATH CONTAINER:SRC_PATH 
```

A more integrated sharing can be achieved thanks to docker volumes as shown in the previous sections.

##### Dealing with multiple images and containers

Some useful commands for image and container management in Docker:

```shell
# to list all existing containers
docker ps -a

# to run an stopped container
docker start [CONTAINER_NAME] -t

# to remove a given container
docker rm [CONTAINER_NAME]

# remove all containers
docker rm $(docker ps -a -q)

# list images
docker images

# remove an image
docker rmi [IMAGE NAME]
```

If you want to keep a container available after its creation please create the container as follows:

```shell
docker run -it -t [CONTAINER_NAME] [VOLUME_OPTIONS] [IMAGE_NAME]
```

You can then use it again by running

```shell
doker start -i [CONTAINER_NAME]
```
