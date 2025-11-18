package onera.pmlanalyzer.pml.model.instances.keystone

import onera.pmlanalyzer.pml.operators.*

trait KeystoneRoutingConstraints {
  self: KeystonePlatform =>

  /** -----------------------------------------------------------
    * Routing constraints (OPTIONAL)
    * Restrict the possible paths to target by encoding
    * of some routing rules
    * ----------------------------------------------------------- */

  // All access (R/W) to DDR or MSMC SRAM from ARMs are routed to the AXI by the MMU
  //  ARMPac.cores foreach { core =>
  //    core.mmu route (MSMC_SRAM.banks ++ DDR.banks) by core.core to AXI
  //  }
  for {
    arm <- ARMPac.cores
  } yield {
    arm.core targeting (MSMC_SRAM.banks ++ DDR.banks) useLink arm.mmu to AXI
  }

  // All access (R/W) to DDR or MSMC SRAM from DSPs are routed to the MSMC by the MPAX
  //  corePacs foreach { core =>
  //    core.mpax route (MSMC_SRAM.banks ++ DDR.banks) by core.dsp to MSMC
  //  }
  for {
    core <- corePacs.take(5)
  } yield {
    core.dsp targeting (MSMC_SRAM.banks ++ DDR.banks) useLink core.mpax to MSMC
  }

  // All the access (R/W) to the DSP private SRAM are stopped by the MPAX (request is not sent to the TeraNet)
  //  corePacs foreach { core =>
  //    core.mpax forbid core.dsram by core.dsp to TeraNet
  //  }
  for {
    core <- corePacs.take(5)
  } yield {
    core.dsp targeting core.dsram blockedBy core.mpax
  }

}
