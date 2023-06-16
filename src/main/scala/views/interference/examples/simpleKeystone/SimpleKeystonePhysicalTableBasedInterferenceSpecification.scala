package views.interference.examples.simpleKeystone

import pml.examples.simpleKeystone.SimpleKeystonePlatform
import pml.operators._
import views.interference.model.specification.PhysicalTableBasedInterferenceSpecification
import views.interference.operators._

/**
  * The interference calculus assumptions for the hardware components of the SimpleKeystone are gathered here.
  * For instance to specify that two service l and r interfere with each other if
  *
  *  - they are provided by the same owner except for
  * [[pml.examples.simpleKeystone.SimpleKeystonePlatform.TeraNet.periph_bus]], [[pml.examples.simpleKeystone.SimpleKeystonePlatform.axi_bus]],
  * [[pml.examples.simpleKeystone.SimpleKeystonePlatform.MemorySubsystem.msmc]]
  *  - they are provided by the [[pml.examples.simpleKeystone.SimpleKeystonePlatform.dma]] and [[pml.examples.simpleKeystone.SimpleKeystonePlatform.dma_reg]]
  * {{{
  *  for {
  *   l <- services
  *   r <- services
  *   if l != r
  *   if (l.hardwareOwnerIs(dma) && r.hardwareOwnerIs(dma_reg)) ||
  *    (l.hardwareOwner == r.hardwareOwner && !l.hardwareOwnerIs(TeraNet.periph_bus)
  *     && !l.hardwareOwnerIs(axi_bus) && !l.hardwareOwnerIs(MemorySubsystem.msmc))
  *  } yield {
  *   l interfereWith r
  *  }
  * }}}
  */
trait SimpleKeystonePhysicalTableBasedInterferenceSpecification extends PhysicalTableBasedInterferenceSpecification {
  self: SimpleKeystonePlatform =>

  for {
    l <- services
    r <- services
    if l != r
    if (l.hardwareOwnerIs(dma) && r.hardwareOwnerIs(dma_reg)) ||
      (l.hardwareOwner == r.hardwareOwner && !l.hardwareOwnerIs(TeraNet.periph_bus) && !l.hardwareOwnerIs(axi_bus) && !l.hardwareOwnerIs(MemorySubsystem.msmc))
  } yield {
    l interfereWith r
  }

  for {
    l <- transactions
    r <- transactions
    if l != r
    if l.initiator == r.initiator
  } yield {
    l exclusiveWith r
  }

}
