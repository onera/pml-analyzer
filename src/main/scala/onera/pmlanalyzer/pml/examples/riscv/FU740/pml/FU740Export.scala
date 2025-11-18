/*******************************************************************************
 * Copyright (c)  2025. ONERA
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

package onera.pmlanalyzer.pml.examples.riscv.FU740.pml

import onera.pmlanalyzer.views.interference.examples.riscv.FU740.{
  FU740ApplicativeTableBasedInterferenceSpecification,
  FU740InclusiveCacheInterferenceSpecification,
  FU740PhysicalTableBasedInterferenceSpecification
}
import onera.pmlanalyzer.pml.exporters.*
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.exporters.*

/**
  * Program entry point to export several version of FU740 platform
  */
object FU740Export extends App {

  /**
    * FU740 where all transactions are considered
    * @group platform_def
    */
  object FU740ConfiguredFull
      extends FU740Platform(
        _u74CoreCnt = 4,
        _sdramInputNb = 1,
        _l2Partitioned = false
      )
      with FU740LibraryConfigurationFull
      with RoutingConfiguration
      with FU740PhysicalTableBasedInterferenceSpecification
      with FU740ApplicativeTableBasedInterferenceSpecification

  /**
   * FU740 where all benchmarked transactions are considered
   * @group platform_def
   */
  object FU740BenchmarkConfiguredFull
      extends FU740Platform(
        _u74CoreCnt = 4,
        _sdramInputNb = 1,
        _l2Partitioned = false
      )
      with FU740BenchmarkConfiguration
      with RoutingConfiguration
      with FU740PhysicalTableBasedInterferenceSpecification
      with FU740ApplicativeTableBasedInterferenceSpecification

  /**
   * FU740 where all benchmarked transactions are considered
   * with interference specification for cache inclusion.
   * @group platform_def
   */
  object FU740BenchmarkConfiguredInclusiveFull
      extends FU740Platform(
        _u74CoreCnt = 4,
        _sdramInputNb = 1,
        _l2Partitioned = false
      )
      with FU740BenchmarkConfiguration
      with RoutingConfiguration
      with FU740PhysicalTableBasedInterferenceSpecification
      with FU740InclusiveCacheInterferenceSpecification
      with FU740ApplicativeTableBasedInterferenceSpecification

  /**
   * FU740 where all transactions are considered
   * with cache partitioning.
   * @group platform_def
   */
  object FU740PartitionedConfiguredFull
      extends FU740Platform(
        _u74CoreCnt = 4,
        _sdramInputNb = 1,
        _l2Partitioned = false
      )
      with FU740LibraryConfigurationFull
      with RoutingConfiguration
      with FU740PhysicalTableBasedInterferenceSpecification
      with FU740ApplicativeTableBasedInterferenceSpecification

  // Generate export for all configured platforms
  for (
    p <- Seq(
      FU740ConfiguredFull,
      FU740BenchmarkConfiguredFull,
      FU740BenchmarkConfiguredInclusiveFull,
      FU740PartitionedConfiguredFull
    )
  ) {
    // Export only HW used by SW (explicit)
    p.exportRestrictedHWAndSWGraph()

    // Export HW and SW graph whether used or not
    p.exportHWAndSWGraph()
    // p.exportShortHWAndSWGraph()

    // Export individually the Service graph of each software
    p.applications foreach { s => p.exportRestrictedServiceGraphForSW(s) }

    // Export the application allocation table
    p.exportAllocationTable()

    // Export the data allocation table
    p.exportDataAllocationTable()

    // Export the target used by software
    p.exportSWTargetUsageTable()

    // Export the routing constraints
    p.exportRouteTable()

    // Export the transactions defined by the user
    p.exportUserTransactions()

    // Export the service graph taking into account service interference
    p.exportRestrictedServiceGraphWithInterfere()

    p.exportInterferenceGraphFromString(Set("t0_2", "t1_2"))
    p.exportInterferenceGraphFromString(Set("t0_2", "t2_6"))
    p.exportInterferenceGraphFromString(Set("t0_0", "t1_2"))
  }

}
