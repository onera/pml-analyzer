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

import onera.pmlanalyzer.pml.model.hardware.{Initiator, Target}
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.software.{Application, Data}
import sourcecode.Name

/** The relations used to encode the service use
  *
  * @param iniValues
  *   initial values of the relation
  * @tparam L
  *   the left type
  * @tparam R
  *   the right type
  */
final case class UseRelation[L, R] private (iniValues: Map[L, Set[R]])(using
    n: Name
) extends Relation[L, R](iniValues)

private[pmlanalyzer] object UseRelation {

  given (using c: Instances): UseRelation[Initiator, Service] =
    c.InitiatorUseService

  given (using c: Instances): UseRelation[Application, Initiator] =
    c.SWUseInitiator

  given (using c: Instances): UseRelation[Application, Service] = c.SWUseService

  given (using c: Instances): UseRelation[Data, Target] = c.DataUseTarget

  trait Instances {

    /** [[pml.model.service.Service]] directly used by
     * [[pml.model.hardware.Initiator]]
     *
     * @group use_relation
     */
    implicit val InitiatorUseService: UseRelation[Initiator, Service]

    /** [[pml.model.software.Application]] hosted on
     * [[pml.model.hardware.Initiator]]
     *
     * @group use_relation
     */
    implicit val SWUseInitiator: UseRelation[Application, Initiator]

    /** [[pml.model.service.Service]] used by [[pml.model.software.Application]]
     *
     * @group use_relation
     */
    implicit val SWUseService: UseRelation[Application, Service]

    /** [[pml.model.software.Data]] hosted on [[pml.model.hardware.Target]]
     *
     * @group use_relation
     */
    implicit val DataUseTarget: UseRelation[Data, Target]
  }

  /** The instances for the use relations
    */
  trait EmptyInstances extends Instances {

    /** [[pml.model.service.Service]] directly used by
      * [[pml.model.hardware.Initiator]]
      *
      * @group use_relation
      */
    final implicit val InitiatorUseService: UseRelation[Initiator, Service] =
      UseRelation(Map.empty)

    /** [[pml.model.software.Application]] hosted on
      * [[pml.model.hardware.Initiator]]
      * @group use_relation
      */
    final implicit val SWUseInitiator: UseRelation[Application, Initiator] =
      UseRelation(Map.empty)

    /** [[pml.model.service.Service]] used by [[pml.model.software.Application]]
      * @group use_relation
      */
    final implicit val SWUseService: UseRelation[Application, Service] =
      UseRelation(Map.empty)

    /** [[pml.model.software.Data]] hosted on [[pml.model.hardware.Target]]
      * @group use_relation
      */
    final implicit val DataUseTarget: UseRelation[Data, Target] = UseRelation(
      Map.empty
    )

  }
}
