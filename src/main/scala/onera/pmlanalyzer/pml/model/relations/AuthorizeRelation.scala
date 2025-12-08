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

package onera.pmlanalyzer.pml.model.relations

import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.software.Application

/** The relations used to encode authorized requests
  *
  * @param iniValues
  *   initial values of the relation
  * @tparam L
  *   the left type
  * @tparam R
  *   the right type
  */
private[pmlanalyzer] final case class AuthorizeRelation[L, R] private (
    iniValues: Map[L, Set[R]]
) extends Relation[L, R](iniValues)

private[pmlanalyzer] object AuthorizeRelation {

  given (using c: Instances): AuthorizeRelation[Application, Service] =
    c.SWAuthorizeService

  trait Instances {

    /** [[pml.model.service.Service]] that can be used by a
     * [[pml.model.software.Application]]
     *
     * @group auth_relation
     */
    val SWAuthorizeService: AuthorizeRelation[Application, Service]

  }

  /** The instances for the authorize relation
    */
  trait EmptyInstances extends Instances {

    /** [[pml.model.service.Service]] that can be used by a
      * [[pml.model.software.Application]]
      * @group auth_relation
      */
    final val SWAuthorizeService: AuthorizeRelation[Application, Service] =
      AuthorizeRelation(Map.empty)

  }
}
