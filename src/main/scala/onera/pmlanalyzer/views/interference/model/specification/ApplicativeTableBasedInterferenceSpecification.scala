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

package onera.pmlanalyzer.views.interference.model.specification

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserScenarioId
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.views.interference.model.relations.ExclusiveRelation
import onera.pmlanalyzer.views.interference.operators.Transform

trait ApplicativeTableBasedInterferenceSpecification extends TableBasedInterferenceSpecification
  with Transform.TransactionLibraryInstances
  with ExclusiveRelation.LibraryInstances
  {
  self: Platform & TransactionLibrary =>

    /**
      * Relation encoding the exclusivity constraints over [[pml.model.configuration.TransactionLibrary.UserScenarioId]]
      * considered by the user
      * @group exclusive_relation
      */
  final lazy val finalUserScenarioExclusive: Map[UserScenarioId, Set[UserScenarioId]] = {
    val exclusive = finalExclusive(purifiedScenarios.keySet)
    relationToMap(scenarioByUserName.keySet,
      (l,r) =>
        l != r && (
          scenarioId(scenarioByUserName(l)) == scenarioId(scenarioByUserName(r))
          || exclusive(scenarioId(scenarioByUserName(l))).contains(scenarioId(scenarioByUserName(r)))
          || scenarioSW(l).flatMap(sw => swExclusive.get(sw).getOrElse(Set.empty)).intersect(scenarioSW(r)).nonEmpty
          )
    )
  }
}
