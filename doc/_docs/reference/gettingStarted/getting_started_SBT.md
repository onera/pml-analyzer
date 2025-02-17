---
layout: doc-page
title: "Installing dependencies"
---

The PML analyzer is an open source API providing a simple DSL to build
a description of the architecture of your chip based on the PHYLOG Model Language (PML).
From this representation a set of safety and interference model templates can be generated to perfom safety and
interference analyses of your platform.

The only dependencies of the PML analyzer are:

+ The Java Runtime Environment version
  8 [JRE 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) or newer.
+ The Simple Build Tool [SBT](https://www.scala-sbt.org/)

### Java 8

You need a working installation of the Java Runtime Environment
version 8 (either OpenJDK or Oracle will do). Installation procedures
may vary depending on your system (Windows, OSX, Linux), please follow
the official guidelines for your system.

### SBT

The compilation of a PML model can be easily performed
with [SBT](https://www.scala-sbt.org/). Installation procedures may vary depending on your system (Windows, OSX, Linux),
please follow the official guidelines for your system.