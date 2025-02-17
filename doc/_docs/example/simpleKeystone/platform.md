---
layout: doc-page
title: "Platform"
---

As shown in Figure 1 the two core are linked by an
AXI bus. The IO devices (DMA, Ethernet controler and SPI) are
linked through a dedicated peripheral bus. These two buses are
connected to the memory subsystem (containing the DDR and
the SRAM memories) through a dedicated controller
called Memory Shared Multicore Controller (MSMC). This controller
acts as a switch from the two buses to the two memories.

All the resources necessary for executing program instructions are locally hosted by each core: ordinal counter,
registers, computing units, etc.
These resources are private to each core.
They can be used simultaneously without interference by each core.
Conversely, the memory hierarchy is composed of resources local
to each core (the cache memories), and also global
resources (such as DDR and SRAM) simultaneously reachable
by the cores and the IO devices.
These global memories are shared resources.

<img alt="platform" src="images/simpleKeystone/platform.PNG" width=100% title="multicore processor"/>

Figure 1: Multicore processor

PML Encoding is provided in src/main/scala/pml/examples/simpleKeystone/SimpleKeystonePlatform.scala