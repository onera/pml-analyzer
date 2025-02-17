/** *****************************************************************************
  * Copyright (c) 2023. ONERA This file is part of PML Analyzer
  *
  * PML Analyzer is free software ; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation ; either version 2 of the License, or (at your
  * option) any later version.
  *
  * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY ; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program ; if not, write to the Free Software Foundation,
  * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */

package onera.pmlanalyzer.pml.model.cycle.DbusC2D2B8

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

trait DbusC2D2B8RoutingConstraints {
  self: DbusC2D2B8Platform =>

  private val dma_targets: Seq[Target] = Seq(
    rosace.ddr.BK0,
    rosace.ddr.BK1,
    rosace.ddr.BK2,
    rosace.ddr.BK3,
    rosace.ddr.BK4,
    rosace.ddr.BK5,
    rosace.ddr.BK6,
    rosace.ddr.BK7,
    rosace.eth
  )

  private val cluster_inputs: Seq[Hardware] = Seq(
    rosace.cg0.cl0.input_port,
    rosace.cg0.input_port,
    rosace.dg0.cl0.input_port,
    rosace.dg0.input_port
  )

  private val cluster_outputs: Seq[Hardware] = Seq(
    rosace.cg0.cl0.output_port,
    rosace.cg0.output_port,
    rosace.dg0.cl0.output_port,
    rosace.dg0.output_port
  )

  private val cores: Seq[Initiator] = Seq(
    rosace.cg0.cl0.C0,
    rosace.cg0.cl0.C1,
    rosace.dg0.cl0.C0,
    rosace.dg0.cl0.C1
  )

  private val srams: Seq[Target] = Seq(
    rosace.dg0.cl0.C0_SRAM,
    rosace.dg0.cl0.C1_SRAM
  )

  for {
    i <- cores
    target <- Target.all
    in_port <- cluster_inputs
  } {
    i targeting target blockedBy in_port
  }

  for {
    i <- Seq(rosace.dma)
    target <- dma_targets
    in_port <- cluster_inputs
  } {
    i targeting target blockedBy in_port
  }

  for {
    i <- Seq(rosace.dma)
    target <- srams
    out_port <- cluster_outputs
  } {
    i targeting target blockedBy out_port
  }
}
