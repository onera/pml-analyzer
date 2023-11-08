---
layout: index
---

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