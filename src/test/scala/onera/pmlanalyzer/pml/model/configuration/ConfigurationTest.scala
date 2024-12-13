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

package onera.pmlanalyzer.pml.model.configuration

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.service.*
import onera.pmlanalyzer.pml.model.software.*
import onera.pmlanalyzer.pml.operators._
import onera.pmlanalyzer.pml.exporters._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sourcecode.Name

class ConfigurationTest
    extends AnyFlatSpec
    with ScalaCheckPropertyChecks
    with should.Matchers {

  /** Architecture of the graph formulation of
    * https://www.overleaf.com/project/5efb44712eb0ec00010737b3
    */
  object ConfigurationFixture
      extends Platform(Symbol("fixture"))
      with ApplicationTest
      with LoadTest
      with StoreTest
      with TargetTest
      with SimpleTransporterTest
      with SmartTest {
    val configName: Symbol = "conf"

    val dma1: Initiator = Initiator()

    val smart1: Core = new Core()
    val smart2: Core = new Core()

    class Core(coreName: Symbol) extends Composite(coreName) {

      val core: Initiator = Initiator()
      val cache: Target = Target()
      val cpu: SimpleTransporter = SimpleTransporter()

      core link cpu
      cpu link cache

      def this()(implicit name: Name) = {
        this(name.value)
      }
    }

    val pamu: Virtualizer = Virtualizer()
    val mem1: Target = Target()
    val mem2: Target = Target()
    val pcie: Target = Target()
    val bus: SimpleTransporter = SimpleTransporter()

    smart1.core link bus
    smart2.core link bus
    dma1 link pamu
    pamu link bus
    bus link mem1
    bus link mem2
    bus link pcie

    // add direct access of dma to caches
    pamu link smart1.cache
    pamu link smart2.cache

    val appSmart1: Application = Application()
    val appSmart21: Application = Application()
    val dmaDescriptor: Application = Application()
    val appSmart22: Application = Application()

    val dataMem1: Data = Data()
    val dataMem2: Data = Data()

    dataMem1 hostedBy mem1
    dataMem2 hostedBy mem2

    appSmart1 hostedBy smart1.core
    appSmart21 hostedBy smart2.core
    appSmart22 hostedBy smart2.core

    appSmart1 read dataMem1
    appSmart21 write dataMem1
    appSmart22 read dataMem2

    // WARNING THE USAGE OF COPIES BY DESCRIPTOR MUST BE DONE AFTER SPECIFYING THEM
    dmaDescriptor hostedBy dma1
    dmaDescriptor read dataMem1
    dmaDescriptor write smart1.cache
    dmaDescriptor read dataMem2
    dmaDescriptor write smart2.cache

    // THIS ROUTE IS VOLUNTARY IMPOSSIBLE SINCE BUS CANNOT ACCESS TO CACHES
    dma1 targeting smart1.cache useLink pamu to bus
    dma1 targeting smart2.cache useLink pamu to smart2.cache

  }

  import ConfigurationFixture.*

  ConfigurationFixture.exportRestrictedServiceAndSWGraph()
  ConfigurationFixture.exportRestrictedHWAndSWGraph()
  ConfigurationFixture.exportHWAndSWGraph()

  "A configured platform" should "encode the used relation properly" in {

    appSmart1.targetLoads should be(mem1.loads)
    appSmart1.targetStores should be(empty)
    appSmart1.hostingInitiators should be(Set(smart1.core))

    appSmart21.targetStores should be(mem1.stores)
    appSmart21.targetLoads should be(empty)
    appSmart21.hostingInitiators should be(Set(smart2.core))

    appSmart22.targetLoads should be(mem2.loads)
    appSmart22.targetStores should be(empty)
    appSmart22.hostingInitiators should be(Set(smart2.core))

    dmaDescriptor.hostingInitiators should be(Set(dma1))
    dmaDescriptor.targetLoads should be(dataMem1.loads and dataMem2.loads)
    dmaDescriptor.targetStores should be(
      smart1.cache.stores and smart2.cache.stores
    )
  }

  it should "encode the routing relation properly" in {
    for (st <- smart1.cache.stores; on <- pamu.stores) {
      InitiatorRouting.get((dma1, st, on)) should be(defined)
      InitiatorRouting((dma1, st, on)) should be(bus.stores)
    }
    for (st <- smart2.cache.stores; on <- pamu.stores) {
      InitiatorRouting.get((dma1, st, on)) should be(defined)
      InitiatorRouting((dma1, st, on)) should be(smart2.cache.stores)
    }
  }

  it should "derive the used transaction properly" in {
    transactionsBySW(appSmart1).size should be(1)
    mem1.loads should contain(
      transactionsByName(transactionsBySW(appSmart1).head).last
    )
    transactionsBySW(appSmart21).size should be(1)
    transactionsBySW(appSmart22).size should be(1)
    // One transaction is missing
    transactionsBySW(dmaDescriptor).size should be(3)
  }

  it should "detect cyclic service paths" in {}

  it should "detect multiple routes" in {
    for {
      a <- ConfigurationFixture.applications
      transactions <- transactionsBySW.get(a)
    } yield {
      for {
        t <- transactions
        path <- transactionsByName.get(t)
      } yield Used.checkMultiPaths(Set(path)) should be(empty)
    }
  }

  it should "detect impossible service accesses " in {
    for (a <- Set(appSmart1, appSmart21, appSmart22))
      Used.checkImpossible(
        transactionsBySW(a).map(transactionsByName),
        a.targetService,
        Some(a)
      ) should be(empty)
    Used
      .checkImpossible(
        transactionsBySW(dmaDescriptor).map(transactionsByName),
        dmaDescriptor.targetService,
        Some(dmaDescriptor)
      )
      .size should be(1)
  }
}
