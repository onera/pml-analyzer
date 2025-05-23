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

import onera.pmlanalyzer.pml.model.{PMLNodeBuilder, PMLNodeMap}
import onera.pmlanalyzer.pml.model.relations.ProvideRelation
import onera.pmlanalyzer.pml.model.service.{Load, Service, Store}
import onera.pmlanalyzer.pml.model.utils.{Owner, ReflexiveInfo}
import sourcecode.{File, Line, Name}

/** Base trait for all hardware node builder the name of the transporter is
  * implicitly derived from the name of the variable used during instantiation.
  * Usually an hardware can be constructed without arguments, where T can be
  * [[Initiator]], [[SimpleTransporter]], [[Virtualizer]], [[Target]]
  * {{{
  *          val myHardware = T()
  * }}}
  *
  * It is also possible to give a specific name, for instance when creating the
  * component in a loop then the following constructor can bee used
  * {{{
  *            val hardwareSeq = for { i <- O to N } yield T(s"myHardware\$i")
  * }}}
  *
  * It is also possible to add specific services, by default each hardware has a
  * [[pml.model.service.Load]] and a [[pml.model.service.Store]] service.
  * {{{
  *          val myLoadService = Load()
  *          val myOtherLoadService = Load()
  *          val myStoreService = Store()
  *          val myHardware = T(Set(myLoadService,myOtherLoadService,myStoreService)))
  * }}}
  *
  * It is also possible to provide the name and the services for instance
  * {{{
  *            val hardwareSeq = for { i <- O to N } yield T(s"myHardware\$i", Set(Load(s"myLoad\$i"), Store(s"myStore\$i"))
  * }}}
  * @see
  *   usage are available in
  *   [[pml.examples.simpleKeystone.SimpleKeystonePlatform]]
  * @tparam T
  *   the concrete type of built object
  * @group builder
  */
trait BaseHardwareNodeBuilder[T <: Hardware] extends PMLNodeBuilder[T] {

  /** The builder that must be implemented by specific builder
    * @param name
    *   the name of the object
    * @return
    *   the object
    * @note
    *   this method is implemented by concrete members (e.g.
    *   [[SimpleTransporter]], no further extension should be provided
    */
  protected def builder(name: Symbol)(using givenInfo: ReflexiveInfo): T

  /** A physical component can be defined only with the basic services it
    * provides The name will be retrieved by using the implicit declaration
    * context (the name of the value enclosing the object)
   *
   * @example
    *   {{{val mySimpleTransporter = SimpleTransporter()}}}
    * @param basics
    *   the set of basic services provided, if empty a default store and load
    *   services are added
    * @param withDefaultServices
    *   add default Load/Store services on creation
   * @param givenName
    *   implicitly retrieved name from the declaration context
    * @param p
    *   implicitly retrieved relation linking components to their provided
    *   services
    * @param owner
    *   implicitly retrieved name of the platform
    * @return
    *   the physical component
    * @group publicConstructor
    */
  def apply(
      basics: Set[Service] = Set.empty,
      withDefaultServices: Boolean = true
  )(using
      givenName: Name,
      p: ProvideRelation[Hardware, Service],
      givenInfo: ReflexiveInfo,
      PMLNodeMap: PMLNodeMap[T],
      lMap: PMLNodeMap[Load],
      sMap: PMLNodeMap[Store]
  ): T =
    apply(Symbol(givenName.value), basics, withDefaultServices)

  /** A physical component can be defined by its name and the basic services it
    * provides A transporter is only defined by its name, so if the transporter
    * already exists it will simply add the services provided by basics
    *
    * @param name
    *   the physical component name
    * @param basics
    *   the set of basic services provided, if empty a default store and load
    *   services are added
    * @param withDefaultServices
    *   add default Load/Store services on creation
    * @param p
    *   implicitly retrieved relation linking components to their provided
    *   services
    * @param owner
    *   implicitly retrieved name of the platform
    * @return
    *   the physical component
    * @group publicConstructor
    */
  def apply(
      name: Symbol,
      basics: Set[Service],
      withDefaultServices: Boolean
  )(using
      p: ProvideRelation[Hardware, Service],
      givenInfo: ReflexiveInfo,
      PMLNodeMap: PMLNodeMap[T],
      lMap: PMLNodeMap[Load],
      sMap: PMLNodeMap[Store]
  ): T = {
    val formattedName = PMLNodeBuilder.formatName(name, givenInfo.owner)
    val hwOwner = givenInfo.owner.add(name)
    val result =
      getOrElseUpdate(formattedName, builder(formattedName))
    val mutableBasic = collection.mutable.Set(basics.toSeq: _*)
    if (withDefaultServices && !basics.exists(_.isInstanceOf[Load]))
      mutableBasic += Load(PMLNodeBuilder.formatName(Symbol("load"), hwOwner))
    if (withDefaultServices && !basics.exists(_.isInstanceOf[Store]))
      mutableBasic += Store(PMLNodeBuilder.formatName(Symbol("store"), hwOwner))
    p.add(result, mutableBasic)
    result
  }

  /** A physical component can be defined by its name and the basic services it
    * provides
    *
    * @param name
    *   the physical component name
    * @param basics
    *   the set of basic services provided, if empty a default store and load
    *   services are added
    * @param p
    *   implicitly retrieved relation linking components to their provided
    *   services
    * @param owner
    *   implicitly retrieved name of the platform
    * @return
    *   the physical component
    * @group publicConstructor
    */
  def apply(name: Symbol, basics: Set[Service])(using
      p: ProvideRelation[Hardware, Service],
      givenInfo: ReflexiveInfo,
      PMLNodeMap: PMLNodeMap[T],
      lMap: PMLNodeMap[Load],
      sMap: PMLNodeMap[Store]
  ): T = {
    apply(name, basics, true)
  }

  /** A physical component can be defined only its name, the services will be
    * defined by default
    * @group publicConstructor
    * @param name
    *   the physical component name
    * @param p
    *   implicitly retrieved relation linking components to their provided
    *   services
    * @param owner
    *   implicitly retrieved name of the platform
    * @return
    *   the physical component
    */
  def apply(
      name: Symbol
  )(using
      p: ProvideRelation[Hardware, Service],
      givenInfo: ReflexiveInfo,
      PMLNodeMap: PMLNodeMap[T],
      lMap: PMLNodeMap[Load],
      sMap: PMLNodeMap[Store]
  ): T =
    apply(name, Set.empty, true)

  /** A physical component can be defined only its name, the services will be
    * defined by default
    *
    * @group publicConstructor
    * @param name
    *   the physical component name
    * @param withDefaultServices
    *   add default Load/Store services on creation
    * @param p
    *   implicitly retrieved relation linking components to their provided
    *   services
    * @param owner
    *   implicitly retrieved name of the platform
    * @return
    *   the physical component
    */
  def apply(name: Symbol, withDefaultServices: Boolean)(using
      p: ProvideRelation[Hardware, Service],
      givenInfo: ReflexiveInfo,
      PMLNodeMap: PMLNodeMap[T],
      lMap: PMLNodeMap[Load],
      sMap: PMLNodeMap[Store]
  ): T =
    apply(name, Set.empty, withDefaultServices)
}
