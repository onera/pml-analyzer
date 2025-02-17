---
layout: doc-page
title: "External Dependencies"
---

The PML modeling does not rely on any external dependency. Nevertheless, it is possible
to connect some backend analysis tools to directly perform analyses out of your PML model:

* for interference analysis: [IDP](https://dtai.cs.kuleuven.be/software/idp/try)
  or [Monosat](https://github.com/sambayless/monosat)
* for the safety analysis: [CeciliaOCAS]() or [OpenAltarica](https://www.openaltarica.fr/docs-downloads/)

The Monosat tool can be integrated as a dynamic library. To do so be sure that the library (.so for Linux,
.dylib for Mac, .dll for Windows) is accessible from the java library path. If not update it by running sbt or the
executable with the VM option:

```shell
# to run SBT with a given library path
java -jar -Djava.library.path=yourPath sbt-launch.jar 

# to run a JAR
java -jar -Djava.library.path=yourPath youJar.jar 
```