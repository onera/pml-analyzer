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

package onera.pmlanalyzer.pml.experiments

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.relations.ProvideRelation
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.utils.{Context, ReflexiveInfo}
import onera.pmlanalyzer.*

sealed abstract class Cluster(n: Symbol, clusterInfo: ReflexiveInfo, c: Context)
    extends Composite(n, clusterInfo, c) {
  val cores: Seq[Initiator]
  val input_port: SimpleTransporter = SimpleTransporter()
  val output_port: SimpleTransporter = SimpleTransporter()
  val bus: SimpleTransporter = SimpleTransporter()

  def prepare_topology(): Unit = {
    for { core <- cores }
      core link bus

    input_port link bus

    bus link output_port
  }

}

final class ClusterCore private (
    val id: String,
    nbCorePerCluster: Int,
    clusterCoreInfo: ReflexiveInfo,
    clusterContext: Context
) extends Cluster(Symbol(s"ClC$id"), clusterCoreInfo, clusterContext) {

  def this(ident: String, nbCorePerCl: Int, dummy: Int = 0)(using
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(ident, nbCorePerCl, givenInfo, givenContext)
  }

  val cores: Seq[Initiator] =
    for { i <- 0 until nbCorePerCluster } yield Initiator(s"C$i")

  val coresL1: Seq[Target] =
    for { i <- 0 until nbCorePerCluster } yield Target(s"C${i}_L1")

  val L2: Target = Target()

  prepare_topology()

  for { (core, l1) <- cores.zip(coresL1) }
    core link l1

  bus link L2
}

final class ClusterDSP private (
    val id: String,
    nbDSPPerCluster: Int,
    clusterDSPInfo: ReflexiveInfo,
    clusterDSPContext: Context
) extends Cluster(Symbol(s"ClD$id"), clusterDSPInfo, clusterDSPContext) {

  def this(ident: String, nbDSPPerCl: Int, dummy: Int = 0)(using
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(ident, nbDSPPerCl, givenInfo, givenContext)
  }

  val cores: Seq[Initiator] =
    for { i <- 0 until nbDSPPerCluster } yield Initiator(s"C$i")

  val SRAM: Seq[Target] =
    for { i <- 0 until nbDSPPerCluster } yield Target(s"C${i}_SRAM")

  prepare_topology()

  for { sram <- SRAM }
    bus link sram

  for { (core, sram) <- cores.zip(SRAM) }
    core link sram
}
