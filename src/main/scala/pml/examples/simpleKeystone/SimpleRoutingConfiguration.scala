package pml.examples.simpleKeystone

import pml.operators._

/**
  * Routing constraints considered for simple Keystone
  */
trait SimpleRoutingConfiguration {
  self: SimpleKeystonePlatform =>

  //Arm cores, ethernet and dma cannot use the periph_bus from msmc
  ARM0.core cannotUseLink MemorySubsystem.msmc to TeraNet.periph_bus
  ARM1.core cannotUseLink MemorySubsystem.msmc to TeraNet.periph_bus
  eth cannotUseLink MemorySubsystem.msmc to TeraNet.periph_bus
  dma cannotUseLink MemorySubsystem.msmc to TeraNet.periph_bus
}
