---
layout: doc-page
title: "Routing"
---


In this example, as shown in Figure 1, there are multiple paths between the cores and the configuration registers.
These registers can be reached from the core either through AXI-BUS, MSMC, PERIPH-BUS and CONFIG-BUS, or directly through CONFIG-BUS.
The platform is configured such that the read and store accesses by the core to the configuration registers are routed through the
the direct path.

The PML routing rules are encoded in src/main/scala/pml/examples/simple/SimpleRoutingConfiguration.scala
