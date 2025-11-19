/*******************************************************************************
 * Copyright (c)  2023. ONERA
 * This file is part of PML Analyzer
 *
 * PML Analyzer is free software ;
 * you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation ;
 * either version 2 of  the License, or (at your option) any later version.
 *
 * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY ;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program ;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 ******************************************************************************/

package keystone.pml

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
