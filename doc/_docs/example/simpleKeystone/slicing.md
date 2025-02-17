---
layout: doc-page
title: "Temporal slices"
---

The tasks are scheduled into two periodic time slices as shown in Figure 3:
app4 and app21 are scheduled in the first time slice;  
app22 and app3 are scheduled in the second time slice;
and as app1 is asynchronous, it can run at any time, that is, possibly in the both slices.

<img alt="slices"  width=100% src="images/simpleKeystone/temporal_slices.PNG" title="temporal scheduling"/>

Figure 3: Temporal scheduling.

The footprint of the transactions of these five tasks on the architecture is shown of each time slice in Figure 4.

<img alt="transactions_footprint"  width=100% src="images/simpleKeystone/transactions_footprint.PNG" title="Footprint of the transactions on the HW architecture (the red, violet, blue, and green arrows represent respectively the transactions of app1, app2 (app21, app22), app3, and app3)."/>

Figure 4: Footprint of the transactions on the HW architecture (the red, violet, blue, and green arrows represent
respectively the transactions of app1, app2 (app21, app22), app3, and app3).

The PML encoding of the first time slice is provided in
src/main/scala/pml/examples/simple/SimpleKeystoneLibraryConfigurationPalnApp21.scala,
and the second time slice is provided in
src/main/scala/pml/examples/simple/SimpleKeystoneLibraryConfigurationPalnApp22.scala
