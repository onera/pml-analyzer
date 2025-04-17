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

import onera.pmlanalyzer.pml.model.{PMLNode, PMLNodeBuilder, PMLNodeMap}
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserScenarioId
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.pml.model.utils.{Message, ReflexiveInfo}
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  PhysicalTransaction,
  PhysicalTransactionId
}

/** Class encoding the user defined scenarios used in the configuration
 *
 * @group scenario_class
    * @param userName
    *   the name of the node
    * @param iniTgt
    *   the origin-destination services couples
    * @param sw
    *   the application that can use this scenario
    */
final class UsedScenario private (
    val userName: UserScenarioId,
    iniTgt: Set[(Service, Service)],
    val sw: Set[Application],
    info: ReflexiveInfo
) extends PMLNode(info) {

  val name: Symbol = userName.id

  /** Try to find a physical transaction of the scenario
      *
      * @return
      *   the set of physical transaction if possible
      */
  def toPhysical(
      transactionsByName: Map[PhysicalTransactionId, PhysicalTransaction]
  ): Set[PhysicalTransactionId] = {
    iniTgt.flatMap(it =>
      transactionsByName
        .filter(p => p._2.size >= 2 && it._1 == p._2.head && it._2 == p._2.last)
        .toList match {
        case Nil =>
          println(Message.impossibleScenarioWarning(userName))
          None
        case (k, _) :: Nil => Some(k)
        case h :: t =>
          println(Message.multiPathScenarioWarning(userName, h +: t))
          (h +: t).map(_._1)
      }
    )
  }
}

/** Builder of platform [[UsedScenario]]
    * @group scenario_class
    */
object UsedScenario extends PMLNodeBuilder[UsedScenario] {

  /** Build a used scenario from its attributes
      * @param name
      *   the explicit name
      * @param iniTgt
      *   the initiator/target couples
      * @param sw
      *   the application that can use it
      * @param owner
      *   the implicitly derived owner of the scenario
      * @return
      *   the corresponding scenario (used in the interference analysis)
      */
  def apply(
      name: UserScenarioId,
      iniTgt: Set[(Service, Service)],
      sw: Set[Application]
  )(using
      givenInfo: ReflexiveInfo,
      map: PMLNodeMap[UsedScenario]
  ): UsedScenario = {
    getOrElseUpdate(
      PMLNodeBuilder.formatName(name.id, givenInfo.owner),
      new UsedScenario(name, iniTgt, sw, givenInfo)
    )
  }
}
