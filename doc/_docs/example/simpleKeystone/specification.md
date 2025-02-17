---
layout: doc-page
title: "Specifications"
---

In this example we consider that

* bus services are independent
* DMA and dma-reg services impacts each others
* app21 and app22 are exclusive as they run on the same core
* app22 and app3 are exclusive as app22 wakes up app3 at the en of its execution.

PML encoding is provided in src/main/scala/views/interference/examples/simple/SimpleTableBasedInterferenceSpecification
