---
layout: doc-page
title: "Transaction library"
---

The application layer is composed of five tasks:
* app4 is an asynchronous microcode running on the Ethernet component.
  Each time an Ethernet frame arrives, it transfers the payload of the frame to SRAM (transaction t41).
* app21 and app22 are two periodic tasks running core1.
* * At each period app21 reads the last Ethernet message from SRAM,
    makes some input treatments on the message, and makes it available for app1 in DDR.
* * Similarly, at each period app22 reads output data of app1 from DDR. It transforms them into SPI frames.
    The frames are then store in SRAM. And finally app22 wakes up the DMA (app3)
    by writing the address of the SPI frames into the DMA registers.
* app3 is a microcode running on DMA. When woke up, app3 reads the SPI frame from
  SRAM and transfers it to SPI.
* app1 is an asynchronous applicative task running on core0 and activated each time a external interrupt arrives.
  It begins by reading the interrupt code from MPIC (transaction t11).
  It reads its input data from DDR (transaction t12).
  Then it runs using the internal cache of core0 (transaction t13).
  And finally it stores its output data in DDR (transaction t14).

The transactions are drawn in Figure 2.

<img alt="transaction" src="images/simpleKeystone/transactions.PNG" width=100% title="Transactions of app1, app21 app22, app3,and app4"/>

Figure 2: Transactions of app1, app21 app22, app3,and app4.

By design, app22 and app3 do not run simultaneously, as app22 wakes up app3 at the end of its execution.

PML Encoding is provided in src/main/scala/pml/examples/simple/SimpleTransactionLibrary.scala

In this example all defined transaction are used, the configuration of the library is provided in src/main/scala/examples/simple/SimpleLibraryConfiguration
