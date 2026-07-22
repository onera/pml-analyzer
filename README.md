<div align="center">
        <picture>
          <source media="(prefers-color-scheme: dark)" srcset="doc/_assets/images/phylog2_dark.png">
          <source media="(prefers-color-scheme: light)" srcset="doc/_assets/images/phylog2.png">
          <img alt="logo" src="doc/_assets/images/phylog2.png">
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

+ The Java Runtime Environment version
  8 [JRE 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) or newer.
+ The Simple Build Tool [SBT](https://www.scala-sbt.org/)

## Configurations

## Installing dependencies

### Java 8

You need a working installation of the Java Runtime Environment
version 8 (either OpenJDK or Oracle will do). Installation procedures
may vary depending on your system (Windows, OSX, Linux), please follow
the official guidelines for your system.

### SBT

The compilation of a PML model can be easily performed
with [SBT](https://www.scala-sbt.org/). Installation procedures may vary depending on your system (Windows, OSX, Linux),
please follow the official guidelines for your system.

### Monosat

The [Monosat](https://github.com/sambayless/monosat) enables PML Analyzer to perform interference analysis.
You should add the monosat.jar library to your class path (or put it in the lib folder) and ensure that the library (.so
for Linux,
.dylib for Mac, .dll for Windows) is accessible from the java library path. If not update it by running sbt or the
executable with the VM option:

```shell
# to run SBT with a given library path
java -jar -Djava.library.path=yourPath sbt-launch.jar 

# to run a JAR
java -jar -Djava.library.path=yourPath youJar.jar 
```

## Using the PML analyzer to generate experiments artifacts

The compilation of a PML model can be easily perform with [SBT](https://www.scala-sbt.org/)
Simply run the following commands, first enter sbt shell by running sbt at the root of the project

```shell
 sbt
```

Then in the sbt shell run

```sbtShell
 projects
```

You should see the following output

```sbtShell
[info] In file:/home/kdelmas/pml-analyzer/
[info] 	 * PMLAnalyzer
[info] 	   experiments
```

You can now ask to consider experiments project as follows:

```sbtShell
project experiments
```

You can see the possible experiments by using:

```sbtShell
run
```

You should see:

```sbtShell
Multiple main classes detected. Select one to run:
[1] keystone.views.interference.KeystoneInterferenceGeneration
[2] synthetic.GeneratedPlatformsTest
```

You can then perform the experiments which results will be located in:
* `analysis/KeystoneWithRosace_Default_method_monosat_solver_itf_calculus_summary.txt` for the experiments on the Keystone obtained by running `keystone.views.interference.KeystoneInterferenceGeneration`
* `export/experiments.csv` for the tests on synthetic architectures specified obtained by running `synthetic.GeneratedPlatformsTest`
 