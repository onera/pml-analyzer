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

import onera.pmlanalyzer.pml.examples.generic.cores.{SiFiveS7Core, SiFiveU7Core}
import onera.pmlanalyzer.pml.examples.riscv.FU740.pml
import onera.pmlanalyzer.pml.model.hardware.Target
import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

/**
  * Definition and allocation of software and data to hardware for simple Keystone.
  * Declaring the application app0
  * {{{val app0: Application = Application()}}}
  * Allocating appO on [[onera.pmlanalyzer.pml.examples.riscv.FU740.pml.FU740Platform.u74_cluster.C0.core]]
  * {{{app1 hostedBy u74_cluster.C0.core}}}
  * Declaring the data ds
  * {{{val ds: Data = Data()}}}
  * Allocating app1 on [[onera.pmlanalyzer.pml.examples.riscv.FU740.pml.FU740Platform.u74_cluster.C0.dtim]]
  * {{{ds hostedBy u74_cluster.C0.dtim}}}
  * @see [[onera.pmlanalyzer.pml.operators.Use.Ops]] for hostedBy operator definition
  */
trait FU740SoftwareAllocation {
  self: FU740Platform =>

  /* -----------------------------------------------------------
   * Application declaration
   * ----------------------------------------------------------- */

  /** [[app0]], [[app1]], [[app2]], [[app3]], [[app4]] is a synchronous applicative activated each time a timer interrupt arrives.
   * Upon activation, a set of a transaction are made to the full memory hierarchy.
   *
    * @group application
    */
  val app0: Application = Application()
  val app1: Application = Application()
  val app2: Application = Application()
  val app3: Application = Application()
  val app4: Application = Application()

  /* -----------------------------------------------------------
   * Data declaration
   * ----------------------------------------------------------- */

  /** Data written by [[app11]], [[app21]], [[app31]], [[app41]] in [[FU740Platform.DdrSdram.memory]], [[FU740Platform.DdrSdram.memory]],
   * [[FU740Platform.DdrSdram.memory]], [[FU740Platform.DdrSdram.memory]] respectively and read by [[app32]]
 *
    * @group data */

  /* Helper class to allocate data in multiple layers of the memory hierarchy.
   * A same data may have instances in different locations based on cacheability
   * and the state of caches.
   */
  trait CacheableData(name: String) {
    val locations: Seq[Target]
    val instances: Seq[Data]

    def bindInstances(): Unit = {
      for {
        (t, d) <- locations.zip(instances)
      } {
        d hostedBy t
      }
    }

  }

  /* Definition of cacheable data locations for S7 cores.
   * Cacheable data is stored in the DDR, and cached in the L2.
   */
  final class S7CacheableData(name: String, core: Int)
      extends CacheableData(name) {
    val S74: SiFiveS7Core = u74_cluster.S74(core)

    val locations: Seq[Target] = Seq(
      u74_cluster.coreToL2Partition.filter(_._1 == S74.core).map(_._2).head,
      u74_cluster.l2_lim,
      ddr.banks.head
    )

    val instances: Seq[Data] =
      locations.map(t => Data(s"${name}_at_${t.name.name}"))

    val L2Cache: Data = instances.head
    val L2LIM: Data = instances(1)
    val Mem: Data = instances(2)

    bindInstances()
  }

  /* Definition of cacheable data locations of U7 cores.
   * Cacheable data is stored in the DDR, cached in the L2, and cache in the local L1.
   */
  final class U7CacheableData(name: String, core: Int)
      extends CacheableData(name) {
    val U74: SiFiveU7Core = u74_cluster.U74(core)

    val locations: Seq[Target] = Seq(
      U74.dl1_cache,
      u74_cluster.coreToL2Partition
        .filter(_._1 == U74.core)
        .map(_._2)
        .head,
      ddr.banks.head
    )

    val instances: Seq[Data] =
      locations.map(t => Data(s"${name}_at_${t.name.name}"))

    val DL1Cache: Data = instances(0)
    val L2Cache: Data = instances(1)
    val Mem: Data = instances(2)

    bindInstances()
  }

  /* Data allocation
   *
   * Each core uses a single cacheable data. Data is stored in the DDR, and it
   * may be cached anywhere in the coreś memory hierarchy.
   *
   * In addition to cacheable data:
   * - C0 (S74) accesses data in its DTIM.
   * - C1 (U74) accesses data in the L2 LIM
   * - C2 (U74) reads data from the UART
   */
  val d0 = S7CacheableData("DataC0", 0)
  val d1 = U7CacheableData("DataC1", 0)
  val d2 = U7CacheableData("DataC2", 1)
  val d3 = U7CacheableData("DataC3", 2)
  val d4 = U7CacheableData("DataC4", 3)

  val ds: Data = Data()
  val di: Data = Data()
  val du: Data = Data()

  ds hostedBy u74_cluster.C0.dtim
  di hostedBy u74_cluster.l2_lim
  du hostedBy uart.rx_fifo

  /* -----------------------------------------------------------
   * Application allocation
   * ----------------------------------------------------------- */
  app0 hostedBy u74_cluster.C0.core
  app1 hostedBy u74_cluster.U74(0).core
  app2 hostedBy u74_cluster.U74(1).core
  app3 hostedBy u74_cluster.U74(2).core
  app4 hostedBy u74_cluster.U74(3).core
}
