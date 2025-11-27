package onera.pmlanalyzer.views.interference.model.specification.keystone

import onera.pmlanalyzer.pml.model.hardware.Hardware
import onera.pmlanalyzer.pml.model.instances.keystone.{
  KeystonePlatform,
  RosaceConfiguration
}
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification

trait RosaceInterferenceSpecification
    extends InterferenceSpecification.Default {
  self: KeystonePlatform with RosaceConfiguration =>

  override def interfereWith(l: Service, r: Service): Boolean =
    l.hardwareOwner == r.hardwareOwner && !l.hardwareOwnerIs(TeraNet)

  def areEquivalent(l: Hardware, r: Hardware): Boolean = {
    val classes: Set[Set[Hardware]] = Set(
      // DSPs are equivalent
      corePacs.map(_.dsp).toSet,
      // private DSRAM & ISRAM are equivalent
      corePacs.map(_.dsram).toSet,
      corePacs.map(_.isram).toSet,
      // DSP MPAXs are equivalent
      corePacs.map(_.mpax).toSet,
      // ARMs are equivalent
      ARMPac.cores.map(_.core).toSet,
      // ARM MMUs are equivalent
      ARMPac.cores.map(_.mmu).toSet,
      // ARMs L1s are equivalent
      ARMPac.cores.map(_.L1).toSet,
      // SRAM banks are equivalent
      MSMC_SRAM.banks.toSet,
      // DDR banks are equivalent
      DDR.banks.toSet
    )

    classes.exists(c => Set(l, r).subsetOf(c))
  }

  override def areEquivalent(l: Service, r: Service): Boolean =
    l.typeName == r.typeName && {
      val owners = l.hardwareOwner.union(r.hardwareOwner)
      owners.size >= 2 && owners
        .subsets(2)
        .forall(ss => areEquivalent(ss.head, ss.last))
    }
}
