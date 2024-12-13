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

package onera.pmlanalyzer.pml.experiments.hbus

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name

class HbusCl2C8B8Platform(name: Symbol) extends Platform(name) {
  def this()(implicit implicitName: Name) = {
    this(Symbol(implicitName.value))
  }

  object Cl0 extends Composite {

    val C0: Initiator = Initiator()

    val C1: Initiator = Initiator()

    val C2: Initiator = Initiator()

    val C3: Initiator = Initiator()

    val C4: Initiator = Initiator()

    val C5: Initiator = Initiator()

    val C6: Initiator = Initiator()

    val C7: Initiator = Initiator()

    val C0_L1: Target = Target()

    val C1_L1: Target = Target()

    val C2_L1: Target = Target()

    val C3_L1: Target = Target()

    val C4_L1: Target = Target()

    val C5_L1: Target = Target()

    val C6_L1: Target = Target()

    val C7_L1: Target = Target()

    val bus: SimpleTransporter = SimpleTransporter()

    val L2: Target = Target()

    C0 link bus

    C1 link bus

    C2 link bus

    C3 link bus

    C4 link bus

    C5 link bus

    C6 link bus

    C7 link bus

    C0 link C0_L1

    C1 link C1_L1

    C2 link C2_L1

    C3 link C3_L1

    C4 link C4_L1

    C5 link C5_L1

    C6 link C6_L1

    C7 link C7_L1

    bus link L2

  }
  object Cl1 extends Composite {

    val C0: Initiator = Initiator()

    val C1: Initiator = Initiator()

    val C2: Initiator = Initiator()

    val C3: Initiator = Initiator()

    val C4: Initiator = Initiator()

    val C5: Initiator = Initiator()

    val C6: Initiator = Initiator()

    val C7: Initiator = Initiator()

    val C0_L1: Target = Target()

    val C1_L1: Target = Target()

    val C2_L1: Target = Target()

    val C3_L1: Target = Target()

    val C4_L1: Target = Target()

    val C5_L1: Target = Target()

    val C6_L1: Target = Target()

    val C7_L1: Target = Target()

    val bus: SimpleTransporter = SimpleTransporter()

    val L2: Target = Target()

    C0 link bus

    C1 link bus

    C2 link bus

    C3 link bus

    C4 link bus

    C5 link bus

    C6 link bus

    C7 link bus

    C0 link C0_L1

    C1 link C1_L1

    C2 link C2_L1

    C3 link C3_L1

    C4 link C4_L1

    C5 link C5_L1

    C6 link C6_L1

    C7 link C7_L1

    bus link L2

  }
  val Pfbus: SimpleTransporter = SimpleTransporter()
  val DMAReg: Target = Target()
  val ETH: Target = Target()
  val DDRCtrl: SimpleTransporter = SimpleTransporter()
  val DMA: Initiator = Initiator()
  val BK0: Target = Target()
  val BK1: Target = Target()
  val BK2: Target = Target()
  val BK3: Target = Target()
  val BK4: Target = Target()
  val BK5: Target = Target()
  val BK6: Target = Target()
  val BK7: Target = Target()

  Cl0.bus link Pfbus
  Cl1.bus link Pfbus
  Pfbus link DMAReg
  Pfbus link ETH
  Pfbus link DDRCtrl
  DMA link Pfbus
  DDRCtrl link BK0
  DDRCtrl link BK1
  DDRCtrl link BK2
  DDRCtrl link BK3
  DDRCtrl link BK4
  DDRCtrl link BK5
  DDRCtrl link BK6
  DDRCtrl link BK7

}
