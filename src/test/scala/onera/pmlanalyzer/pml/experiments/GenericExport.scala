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

import onera.pmlanalyzer.pml.exporters.*
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.examples.mySys.{
  MyProcInterferenceSpecification,
  MySysInterferenceSpecification
}
import onera.pmlanalyzer.views.interference.exporters.*
import onera.pmlanalyzer.views.interference.model.specification.{
  ApplicativeTableBasedInterferenceSpecification,
  PhysicalTableBasedInterferenceSpecification
}
import onera.pmlanalyzer.views.interference.operators.*

/** Program entry point to export several version of Keystone
  */
object GenericExport extends App {

  def generatePlatformFromConfiguration(
      coreCount: Int,
      dspCount: Int,
      ddrPartitions: Int,
      coresPerBankPerPartition: Int
  ): Platform
    with TransactionLibrary
    with PhysicalTableBasedInterferenceSpecification
    with ApplicativeTableBasedInterferenceSpecification = {
    // FIXME Assert ddrPartitions <= GP Core Count
    // FIXME Assert GP Core count is multiple of ddrPartitions

    // Create a GP group per partition
    // FIXME How to configure more than 1 GP cluster per group?
    val gp_group_count: Int = ddrPartitions
    val gp_cluster_per_group: Int = 1
    val gp_cores_per_cluster = coreCount / gp_group_count / gp_cluster_per_group

    // Allocate banks to groups and cores
    val bank_count: Int =
      Math.ceil(coreCount / ddrPartitions / coresPerBankPerPartition).toInt

    // Create a single DSP group
    // - No activity from DSP Cores outside their cluster/group
    // - Only the (single) DMA comes into the DSP clusters
    val dsp_group_count: Int = 1
    val dsp_cluster_per_group: Int = 1
    val dsp_cores_per_cluster: Int =
      dspCount / dsp_group_count / dsp_cluster_per_group

    // Derive additional Generic Platform parameters
    val name = Symbol(
      s"GenericSample_${coreCount}Cores_${dspCount}Dsp_${ddrPartitions}Prt_${coresPerBankPerPartition}CorePerBank"
    )
    val ddr_count: Int = ddrPartitions

    new GenericPlatform(
      name = name,
      nbGroupCore = gp_group_count,
      nbGroupDSP = dsp_group_count,
      nbClusterGroupDSP =
        1, // FIXME This input seems redundant with the DSP Per Group
      nbClusterGroupCore =
        1, // FIXME This input seems redundant with the Core Per Group
      nbClusterCorePerGroup = gp_cluster_per_group,
      nbClusterDSPPerGroup = dsp_cluster_per_group,
      nbCorePerCluster = gp_cores_per_cluster,
      nbDSPPerCluster = dsp_cores_per_cluster,
      nbDDRBank = bank_count,
      nbDDRController = ddr_count
    ) with GenericSoftware
      with GenericTransactionLibrary
      with GenericRoutingConstraints
      with GenericTransactionInterferenceSpecification
      with GenericApplicationInterferenceSpecification
  }

  val log2 = (x: Int) => (Math.log10(x) / Math.log10(2.0)).toInt
  val candidatePlatforms = for {
    coreCount <- Seq(0, 1, 2, 4, 8)
    dspCount <- Seq(0, 1, 2, 4, 8)
    ddrPartitions <- {
      for { i <- 0 to Math.min(log2(coreCount), 1) } yield {
        Math.pow(2.0, i).toInt
      }
    }
    coresPerBankPerPartition <- {
      for {
        i <- 0 to log2(coreCount / ddrPartitions)
      } yield {
        Math.pow(2.0, i).toInt
      }
    }
    if (0 < coreCount + dspCount)
    if (coreCount + dspCount <= 12)
  } yield {
    generatePlatformFromConfiguration(
      coreCount = coreCount,
      dspCount = dspCount,
      ddrPartitions = ddrPartitions,
      coresPerBankPerPartition = coresPerBankPerPartition
    )
  }

  println(s"Generated ${candidatePlatforms.length} platforms")

  for (platform <- candidatePlatforms) {
    // Export only HW used by SW (explicit)
    platform.exportRestrictedHWAndSWGraph()

    // Export HW and SW graph whether used or not
    platform.exportHWAndSWGraph()

    // Export Service graph whether used or not and considering that all services are non-exclusive
    platform.exportServiceGraph()

    // Export Service graph considering and SW
    platform.exportRestrictedServiceAndSWGraph()

    // Export Service graph considering that all services are non-exclusive
    platform.exportServiceGraphWithInterfere()

    // Export individually the Service graph of each software
    platform.applications foreach { s =>
      platform.exportRestrictedServiceGraphForSW(s)
    }

    // Export the application allocation table
    platform.exportAllocationTable()

    // Export the data allocation table
    platform.exportDataAllocationTable()

    // Export the target used by software
    platform.exportSWTargetUsageTable()

    // Export the routing constraints
    platform.exportRouteTable()

    // Export the deactivated components
    platform.exportDeactivatedComponents()

    // Export the transactions defined by the user
    platform.exportUserScenarios()

    platform.exportSemanticsSize()

    platform.exportAnalysisGraph()
  }
}
