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

import onera.pmlanalyzer.pml.model.{PMLNodeBuilder, PMLNodeMap}
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserScenarioId
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.pml.model.utils.ReflexiveInfo
import onera.pmlanalyzer.pml.operators.{ToTransaction, TransactionParam}
import sourcecode.Name

/** Class encoding the defined transactions (not already used)
 *
 * @group scenario_class
    * @param userName
    *   the name of the node
    * @param iniTgt
    *   a by-name value providing the origin-destination service couples of the
    *   scenario (not evaluated during the object initialisation)
    * @param sw
    *   the application that can use this scenario
    */
final class Scenario private (
    val userName: UserScenarioId,
    val iniTgt: () => Set[(Service, Service)],
    val sw: () => Set[Application],
    info: ReflexiveInfo
) extends ScenarioLike(userName.id, info) {

  /** Consider the transaction for the analysis
      *
      * @return
      *   the used scenario class
      */
  def used(using
      givenInfo: ReflexiveInfo,
      map: PMLNodeMap[UsedScenario]
  ): UsedScenario =
    UsedScenario(userName, iniTgt(), sw())
}

/** Builder of platform [[Scenario]]
    * @group scenario_class
    */
object Scenario extends PMLNodeBuilder[Scenario] {

  /** Build a scenario from a transaction or another scenario
      * @param tr
      *   the original scenario like
      * @param name
      *   the implicitly derived name
      * @return
      *   the resulting scenario
      */
  def apply(
      tr: ScenarioLike
  )(using
      name: Name,
      givenInfo: ReflexiveInfo,
      map: PMLNodeMap[Scenario]
  ): Scenario =
    apply(UserScenarioId(Symbol(name.value)), tr.iniTgt, tr.sw)

  /** Build a Scenario from a bunch of transactions, this should not be used
      * with anonymous transaction
      *
      * @param tr
      *   the set of transactions
      * @param name
      *   the implicit name of the scenario (same as the variable used to refer
      *   to it)
      * @return
      *   a scenario
      */
  def apply(
      tr: Transaction*
  )(using
      name: Name,
      givenInfo: ReflexiveInfo,
      map: PMLNodeMap[Scenario]
  ): Scenario =
    apply(name.value, tr: _*)

  def apply(
      name: String,
      tr: Transaction*
  )(using
      givenInfo: ReflexiveInfo,
      map: PMLNodeMap[Scenario]
  ): Scenario =
    apply(
      UserScenarioId(Symbol(name)),
      () => {
        tr.flatMap(_.iniTgt()).toSet
      },
      () => {
        tr.flatMap(_.sw()).toSet
      }
    )

  /** Main constructor of a scenario, note that scenarios are memoized, so if
      * the same name is used in the same platform the constructor will send
      * back the previous definition of the scenario
      *
      * @param name
      *   the name of the scenario
      * @param iniTgt
      *   the set of initial/target services defining the scenario
      * @param sw
      *   the applications that may invoke this scenario
      * @param owner
      *   the owner of the transaction (the platform)
      * @return
      *   the transaction (not used for now)
      */
  def apply(
      name: UserScenarioId,
      iniTgt: () => Set[(Service, Service)],
      sw: () => Set[Application]
  )(using givenInfo: ReflexiveInfo, map: PMLNodeMap[Scenario]): Scenario = {
    getOrElseUpdate(
      PMLNodeBuilder.formatName(name.id, givenInfo.owner),
      new Scenario(name, iniTgt, sw, givenInfo)
    )
  }
}
