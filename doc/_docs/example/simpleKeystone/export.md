---
layout: doc-page
title: "Exports"
---

### Configured platform

The file src/main/scala/pml/examples/simple/SimpleExport shows how graphical exports are produced (stored in export
folder)
from a platform:

* graph of used SW and HW
* graph of used services per application
* table of transaction
* table of data
* table of SW allocation to HW
* table of component activation
* table of SW usage
* routing table
* transfert table

### Interference analysis

The file src/main/scala/views/interference/examples/SimpleInterferenceGeneration shows how interference analysis can be
performed
on a configured platform. The generated files are stored in analysis folder:

* computation of n-itf
* computation of n-free
* computation of n-channels

As an example the following interference is identified as a 3-itf in the first time slice:

< app1_wr_d2 || app21_wr_d1 || app4_wr_input_d >

<img alt="interference_channel"  width=100% src="images/simpleKeystone/interference.PNG" title="Footprint and interference channel (identified by the two circles)"/>

Figure 5: Example of footprint and interference channel (identified by the two circles) 

