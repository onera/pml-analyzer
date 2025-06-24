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

import onera.pmlanalyzer.pml.examples.components.cores.{SiFiveS7Core, SiFiveU7Core}
import onera.pmlanalyzer.pml.examples.riscv.FU740.pml
import onera.pmlanalyzer.pml.model.hardware.Target
import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

/**
  * Definition and allocation of software and data to hardware for simple Keystone.
  * Declaring the application app1
  * {{{val app1: Application = Application()}}}
  * Allocating app1 on [[pml.examples.ZynqUltraScale.ZynqUltraScalePlatform.APU.Cor
  * {{{app1 hostedBy APU.Cortex_a53_0}}}
  * Declaring the data input_d
  * {{{val input_d: Data = Data()}}}
  * Allocating app1 on [[pml.examples.ZynqUltraScale.ZynqUltraScalePlatform.OCM.bank0]]
  * {{{input_d hostedBy OCM.bank0}}}
  * @see [[pml.operators.Use.Ops]] for hostedBy operator definition
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

  /* Helper class to allocate data in multiple layers of the memory hierarchy. */
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

  final class S7CacheableData(name: String, core: Int)
      extends CacheableData(name) {
    val S74: SiFiveS7Core = Cluster_U74_0.S74(core)

    val locations: Seq[Target] = Seq(
      Cluster_U74_0.CoreToL2Partition.filter(_._1 == S74.core).map(_._2).head,
      Cluster_U74_0.l2_lim,
      DDR.banks(0)
    )

    val instances: Seq[Data] =
      locations.map(t => Data(s"${name}_at_${t.name.name}"))

    val L2Cache: Data = instances(1)
    val L2LIM: Data = instances(2)
    val Mem: Data = instances(3)

    bindInstances()
  }

  final class U7CacheableData(name: String, core: Int)
      extends CacheableData(name) {
    val U74: SiFiveU7Core = Cluster_U74_0.U74(core)

    val locations: Seq[Target] = Seq(
      U74.dl1_cache,
      Cluster_U74_0.CoreToL2Partition
        .filter(_._1 == U74.core)
        .map(_._2).head,
      DDR.banks(0)
    )

    val instances: Seq[Data] =
      locations.map(t => Data(s"${name}_at_${t.name.name}"))

    val L1DCache: Data = instances(0)
    val L2Cache: Data = instances(1)
    val Mem: Data = instances(2)

    bindInstances()
  }

  /* Data allocation
   *
   * Allocate one Data per each core. Data is stored in the DDR, and it may be
   * cached anywhere in the core's memory hierarchy.
   */
  val ds: Data = Data()
  val d0 = S7CacheableData("DataC0", 0)
  val d1 = U7CacheableData("DataC1", 0)
  val di: Data = Data()
  val d2 = U7CacheableData("DataC2", 1)
  val d3 = U7CacheableData("DataC3", 2)
  val d4 = U7CacheableData("DataC4", 3)

  ds hostedBy Cluster_U74_0.C0.dtim
  di hostedBy Cluster_U74_0.l2_lim

  /* TODO Add data towards UART on C2, transaction transparent on tilelink_switch */

  /* -----------------------------------------------------------
   * Application allocation
   * ----------------------------------------------------------- */
  app0 hostedBy Cluster_U74_0.C0.core
  app1 hostedBy Cluster_U74_0.U74(0).core
  app2 hostedBy Cluster_U74_0.U74(1).core
  app3 hostedBy Cluster_U74_0.U74(2).core
  app4 hostedBy Cluster_U74_0.U74(3).core
}
