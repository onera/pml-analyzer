---
layout: doc-page
title: "Software Allocation"
---

The application layer is composed of five tasks:

* app4 is an asynchronous microcode running on the eth component.
* app21 is a periodic task running on core2.
* app22 is a periodic task running on core2.
* app3 a microcode running on DMA.
* app1 is an asynchronous applicative task running on core1.

PML Encoding is provided in src/main/scala/pml/examples/simple/SimpleSoftwareAllocation.scala
