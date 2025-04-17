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

package onera.pmlanalyzer.pml.model.hardware

import onera.pmlanalyzer.pml.model.PMLNodeBuilder
import onera.pmlanalyzer.pml.model.hardware.Composite.formatName
import onera.pmlanalyzer.pml.model.relations.Context
import onera.pmlanalyzer.pml.model.utils.{Message, Owner, ReflexiveInfo}
import sourcecode.{File, Line, Name}

/** Base class of sub-systems containing themselves hardware components
  * @see
  *   usage are available in
  *   [[pml.examples.simpleKeystone.SimpleKeystonePlatform]]
  * @param n
  *   the name of the node
  * @param _owner
  *   the id of the owner of the composite (the platform or another composite)
  * @group hierarchical_class
  */
abstract class Composite(n: Symbol, info: ReflexiveInfo, c:Context)
    extends Hardware(info)
      with ContainerLike {

  implicit val context: Context = c

  val name: Symbol = formatName(n, owner)

  /** the current owner id becomes the id of the current node and override the
    * previous definition of [[currentOwner]]
    * @note
    *   the initial value of the owner is stored in [[owner]]
    * @group identifier
    */
  implicit val currentOwner: Owner = owner.add(n)

  /** notify the initialisation of a new composite to the companion object
    */
  Composite.addComposite(this, owner)

  /** Provide all the physical elements declared inside the composite
    * @group component_access
    * @return
    *   set of declared component
    */
  def hardware: Set[Hardware] =
    Initiator.allDirect ++ Target.allDirect ++ Virtualizer.allDirect ++ SimpleTransporter.allDirect ++ Composite.allDirect

  /** Provide all the physical elements declared inside the composite and its
    * components
    * @group component_access
    * @return
    *   set of all sub-components
    */
  def allHardware: Set[Hardware] = hardware flatMap {
    case c: Composite => c.allHardware
    case o            => Set(o)
  }

  /** Alternative constructor without implicit owner
   *
   * @param compositeName
    *   the name of the composite
    * @param dummy
    *   dummy argument to avoid method signature conflict
   * @param givenInfo
    *   the implicit info of the composite
    */
  def this(compositeName: Symbol, dummy: Int = 0)(using
      givenInfo: ReflexiveInfo,
                                                  givenC: Context
  ) = {
    this(compositeName, givenInfo, givenC)
  }
}

/** Static methods of Composite
  * @group utilFun
  */
object Composite extends PMLNodeBuilder[Composite] {

  /** Reuse same formatting rule as [[BaseHardwareNodeBuilder]]
    * @param name
    *   the name of the composite
    * @param owner
    *   the name of its owner
    * @return
    *   the formatted name
    */
  def formatName(name: Symbol, owner: Owner): Symbol = Symbol(
    s"${owner}_${name.name}"
  )

  /** Notify that a new composite has been defined
    * @param c
    *   the composite
    * @param owner
    *   its owner
    */
  private def addComposite(c: Composite, owner: Owner): Unit = {
    add(c)
  }
}
