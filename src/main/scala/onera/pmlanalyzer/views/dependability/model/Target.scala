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

package onera.pmlanalyzer.views.dependability.model

import onera.pmlanalyzer.views.dependability.exporters.SubComponent
import onera.pmlanalyzer.views.dependability.operators.{IsCriticityOrdering, IsFinite, IsShadowOrdering}
import onera.pmlanalyzer.views.dependability.exporters.{SubComponent, *}
import onera.pmlanalyzer.views.dependability.model.CustomTypes.Request
import onera.pmlanalyzer.views.dependability.operators.*

trait Target[FM] extends Component {
  val id: TargetId
  val storeI: InputPort[List[Request[FM]]] = InputPort[List[Request[FM]]](Symbol("storeI"))
  val fMAutomaton: FMAutomaton[FM]
  val loadO: OutputPort[Request[FM]] =
    OutputPort(VariableId(Symbol("loadO")), () =>
      for (o <- fMAutomaton.o.eval()) yield Request(allOf[InitiatorId].map(sId => (sId, id) -> o): _*)
    )
}

class InputDepTarget[FM: IsCriticityOrdering : IsFinite : IsShadowOrdering] private(val id: TargetId)(implicit ev: Owner) extends Target[FM] {
  val fMAutomaton: InputFMAutomaton[FM] = InputFMAutomaton[FM](AutomatonId(Symbol("fmAutomaton")), min[FM])
  fMAutomaton.in := {
    for (requests <- storeI.eval()) yield {
      val stores = requests.flatMap{ r => r.collect { case (k, v) if k._2 == id => v }}
      if (stores.isEmpty)
        noneOf[FM]
      else
        worst(stores: _*)
    }
  }
}

object InputDepTarget {

  def apply[FM: IsCriticityOrdering : IsFinite : IsShadowOrdering](id: TargetId)(implicit context: Builder[SubComponent] with Owner): InputDepTarget[FM] = {
    val result = new InputDepTarget(id)
    context.toBuild.getOrElseUpdate(id.name, () => SubComponent(id.name, result.toCecilia))
    context.portOwner(result.loadO.id) = result
    context.portOwner(result.storeI.id) = result
    context.componentOwner(result.fMAutomaton) = result
    result
  }
}

class InputInDepTarget[FM: IsCriticityOrdering : IsFinite] private(val id: TargetId)(implicit owner: Owner) extends Target[FM] {
  val fMAutomaton: SimpleFMAutomaton[FM] = SimpleFMAutomaton[FM](AutomatonId(Symbol("fmAutomaton")), min[FM])
}

object InputInDepTarget {
  def apply[FM: IsCriticityOrdering : IsFinite : IsShadowOrdering](id: TargetId)(implicit context: Builder[SubComponent] with Owner): InputInDepTarget[FM] = {
    val result = new InputInDepTarget(id)
    context.toBuild.getOrElseUpdate(id.name, () => SubComponent(id.name, result.toCecilia))
    context.portOwner(result.loadO.id) = result
    context.portOwner(result.storeI.id) = result
    context.componentOwner(result.fMAutomaton) = result
    result
  }
}