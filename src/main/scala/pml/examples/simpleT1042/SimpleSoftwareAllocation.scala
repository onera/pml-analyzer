/*******************************************************************************
 * Copyright (c)  2021. ONERA
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

package pml.examples.simpleT1042

import pml.model.software.{Application, Data}
import pml.operators._

import scala.language.postfixOps

trait SimpleSoftwareAllocation {
  self: SimpleT1042Platform =>

  val configName: Symbol = Symbol("WithConf")

  /** -----------------------------------------------------------
    * Configuration specification
    * ----------------------------------------------------------- */

  /** -----------------------------------------------------------
    * Application specification :
    * ----------------------------------------------------------- */
  val app1: Application = Application()
  val app21: Application = Application()
  val app22: Application = Application()

  val app3: Application = Application()
  val app4: Application = Application()


  /** -----------------------------------------------------------
    * Data specification
    * ----------------------------------------------------------- */
  // Data written by eth in mem2 and read by app21
  val input_d: Data = Data()
  // Data written by app21 in mem1 and read by app1
  val d1: Data = Data()
  // Interrupt read by app1
  val interrupt1: Data = Data()
  // Data written by app1 in men1 and read by app21
  val d2: Data = Data()
  // Data written by app21 in mem2 and read by dma
  val output_d: Data = Data()
  // Register value written by app21 in DMA_reg
  val dma_red_value: Data = Data()
  // PCIe frame put by dma on the PCIe port
  val output_pcie_frame: Data = Data()

  /** -----------------------------------------------------------
    * Data allocation
    * ----------------------------------------------------------- */

  input_d hostedBy mem2
  d1 hostedBy mem1
  interrupt1 hostedBy mpic
  d2 hostedBy mem1
  output_d hostedBy mem2
  dma_red_value hostedBy dma_reg
  output_pcie_frame hostedBy pcie

  /** -----------------------------------------------------------
    * Application allocation
    * ----------------------------------------------------------- */

  app1 hostedBy C1.core
  app21 hostedBy C2.core
  app22 hostedBy C2.core
  app3 hostedBy dma
  app4 hostedBy eth

}
