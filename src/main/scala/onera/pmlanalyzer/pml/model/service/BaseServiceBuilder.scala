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

package onera.pmlanalyzer.pml.model.service

import onera.pmlanalyzer.pml.model.PMLNodeBuilder
import onera.pmlanalyzer.pml.model.utils.{Owner, ReflexiveInfo}
import sourcecode.{File, Line, Name}

/** Base trait for all hardware node builder the name of the transporter is
  * implicitly derived from the name of the variable used during instantiation.
  * Usually an hardware can be constructed without arguments, where T can be
  * [[Load]], [[Store]], [[ArtificialService]]
  * {{{
  *          val myService = T()
  * }}}
  *
  * It is also possible to give a specific name, for instance when creating the
  * component in a loop then the following constructor can bee used
  * {{{
  *            val serviceSeq = for { i <- O to N } yield T(s"myService\$i")
  * }}}
  * @see
  *   usage are available in
  *   [[pml.examples.simpleKeystone.SimpleKeystonePlatform]]
  * @tparam T
  *   the concrete type of built object
  * @group builder
  */
trait BaseServiceBuilder[T <: Service] extends PMLNodeBuilder[T] {

  /** The builder that must be implemented by specific builder
    * @param name
    *   the name of the object
    * @return
    *   the object
    */
  protected def builder(name: Symbol)(using givenInfo: ReflexiveInfo): T

  /** A service can be defined by the name provided by the implicit declaration
    * context (the name of the value enclosing the object)
    *
   * @param givenName
    *   the implicit service name
    * @param owner
    *   implicitly retrieved name of the platform
    * @return
    *   the service
    */
  def apply()(using
      givenName: Name,
      givenInfo: ReflexiveInfo
  ): T = apply(Symbol(givenName.value))

  /** A service can be defined by its name
    *
    * @param name
    *   the service name
    * @param owner
    *   implicitly retrieved name of the platform
    * @return
    *   the service
    */
  def apply(
      name: Symbol
  )(using givenInfo: ReflexiveInfo): T = {
    getOrElseUpdate(name, builder(name))
  }
}
