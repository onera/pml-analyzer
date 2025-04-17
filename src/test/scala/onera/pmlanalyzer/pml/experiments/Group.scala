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

import onera.pmlanalyzer.pml.model.hardware.{Composite, SimpleTransporter}
import onera.pmlanalyzer.pml.model.relations.Context
import onera.pmlanalyzer.pml.model.utils.ReflexiveInfo
import onera.pmlanalyzer.pml.operators.*

sealed abstract class Group[+T <: Cluster](
                                            id: Int,
                                            nbCluster: Int,
                                            nbClusterGroup: Int,
                                            groupInfo: ReflexiveInfo,
                                            groupContext: Context
                                          ) extends Composite(Symbol(s"cg$id"), groupInfo, groupContext) {
  val clusters: Seq[Seq[T]]

  val bus: GroupCrossBar = GroupCrossBar(id, nbCluster, nbClusterGroup)
  val output_port: SimpleTransporter = SimpleTransporter()
  val input_port: SimpleTransporter = SimpleTransporter()

  def prepare_topology(): Unit = {
    for {
      i <- clusters.indices
      j <- clusters(i).indices
    } {
      clusters(i)(j).output_port link bus.clusterInput(i)(j)
      bus.clusterOuput(i)(j) link clusters(i)(j).input_port
    }

    bus.output_port link output_port
    input_port link bus.input_port

  }

}

final class GroupDSP private (
                               val id: Int,
                               nbClusterDSPPerGroup: Int,
                               nbClusterGroupDSP: Int,
                               nbDSPPerCluster: Int,
                               groupDSPInfo: ReflexiveInfo,
                               groupDSPContext: Context
                             ) extends Group[ClusterDSP](
  id,
  nbClusterDSPPerGroup,
  nbClusterGroupDSP,
  groupDSPInfo,
  groupDSPContext
) {

  def this(ident: Int, nbClusterDSPPerGr:Int, nbClusterGrDSP:Int, nbDSPPerCl:Int, dummy: Int = 0)(using
                                       givenInfo: ReflexiveInfo,
                                       givenContext:Context
  ) = {
    this(ident, nbClusterDSPPerGr, nbClusterGrDSP, nbDSPPerCl, givenInfo, givenContext)
  }

  val clusters: Seq[Seq[ClusterDSP]] =
    for {
      i <- 0 until nbClusterDSPPerGroup
    } yield {
      for {
        j <- 0 until nbClusterGroupDSP
      } yield ClusterDSP(s"G${id}_${i}_$j", nbDSPPerCluster)
    }

  prepare_topology()
}

final class GroupCore private (
                                val id: Int,
                                nbClusterCorePerGroup: Int,
                                nbClusterGroupCore: Int,
                                nbCorePerCluster:Int,
                                groupCoreInfo: ReflexiveInfo,
                                groupCoreContext: Context
                              ) extends Group[ClusterCore](
  id,
  nbClusterCorePerGroup,
  nbClusterGroupCore,
  groupCoreInfo,
  groupCoreContext
) {

  def this(ident: Int, nbClusterCorePerGr: Int, nbClusterGrCore:Int, nbCorePerCl:Int,  dummy: Int = 0)(using
                                       givenInfo: ReflexiveInfo,
                                       givenContext: Context
  ) = {
    this(ident, nbClusterCorePerGr, nbClusterGrCore, nbCorePerCl, givenInfo, givenContext)
  }

  val clusters: Seq[Seq[ClusterCore]] =
    for {
      i <- 0 until nbClusterCorePerGroup
    } yield {
      for {
        j <- 0 until nbClusterGroupCore
      } yield ClusterCore(s"G${id}_${i}_$j", nbCorePerCluster)
    }

  prepare_topology()
}