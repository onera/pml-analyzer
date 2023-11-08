---
layout: doc-page
title: "Use PML by the examples"
---

#### Argumentation patterns

The justification patterns considered for the CAST32-A are provided in the ``views.patterns`` package.
These patterns  can be used as a starting point to start your argumentation activity.

To compile and run the PHYLOG patterns example please enter the following commands:
```sbtshell
 sbt runMain views.patterns.examples.PhylogPatterns
```

To compile and run the PHYLOG pattern instances example please enter the following commands:
```sbtshell
 sbt runMain views.patterns.examples.PhylogPatternsInstances
```


#### Modelling
Various benchmark systems for platform modeling are provided
in the ``pml.examples`` package. These benchmarks can be used as a starting point to
your modeling activity.

To compile and run the Keystone example please enter the following commands:
```sbtshell
 sbt runMain pml.examples.keystone.KeystoneExport
```

To compile and run the SimplePlatform example please enter the following commands:
```sbtshell
 sbt runMain pml.examples.simple.SimpleExport
```

Documentation is available [here](example/simpleKeystone/index.md)

#### Analysis
For each view (interference, patterns and dependability) examples are provided in the dedicated ``views.X.examples``.
These benchmarks can be used as a starting point to
your analysis activity. For instance, we can carry out the interference analysis of the Keystone platform with
 ```sbtshell
 # example of a PML model where an IDP interference model is generated
 sbt runMain views.interference.examples.KeystoneExport

 # example of a PML model where a Cecilia export is generated 
sbt runMain views.dependability.examples.KeystoneExport
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