---
layout: doc-page
title: "Editing a PML Model"
---

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
