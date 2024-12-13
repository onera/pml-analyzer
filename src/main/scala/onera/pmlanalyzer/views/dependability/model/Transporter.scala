/** *****************************************************************************
  * Copyright (c) 2023. ONERA This file is part of PML Analyzer
  *
  * PML Analyzer is free software ; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation ; either version 2 of the License, or (at your
  * option) any later version.
  *
  * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY ; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program ; if not, write to the Free Software Foundation,
  * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */

package onera.pmlanalyzer.views.dependability.model

import onera.pmlanalyzer.views.dependability.operators.{
  IsCriticityOrdering,
  IsFinite,
  IsShadowOrdering
}
import onera.pmlanalyzer.views.dependability.exporters.*
import onera.pmlanalyzer.views.dependability.model.CustomTypes.{
  Request,
  TargetStatus
}
import onera.pmlanalyzer.views.dependability.operators.*

abstract class Transporter[FM: IsCriticityOrdering: IsFinite: IsShadowOrdering](
    implicit owner: Owner
) extends Component {
  val loadI: InputPort[List[Request[FM]]] =
    InputPort[List[Request[FM]]](Symbol("loadI"))
  val initialState: FM = min[FM]
  val fMAutomaton: SimpleFMAutomaton[FM] =
    SimpleFMAutomaton[FM](AutomatonId(Symbol("fmAutomaton")), initialState)
  val storeO: OutputPort[Request[FM]]
}

abstract class BasicTransporter[
    FM: IsCriticityOrdering: IsFinite: IsShadowOrdering
](implicit owner: Owner)
    extends Transporter[FM] {
  val loadO: OutputPort[Request[FM]]
  val storeI: InputPort[List[Request[FM]]]
}

class SimpleTransporter[
    FM: IsCriticityOrdering: IsFinite: IsShadowOrdering
] private (
    val id: TransporterId,
    val reject: ((InitiatorId, TargetId)) => Boolean
)(implicit owner: Owner)
    extends BasicTransporter[FM] {
  val storeI: InputPort[List[Request[FM]]] =
    InputPort[List[Request[FM]]](Symbol("storeI"))
  val storeO: OutputPort[Request[FM]] =
    OutputPort(
      VariableId(Symbol("storeO")),
      () =>
        for (reqs <- storeI.eval(); s <- fMAutomaton.o.eval()) yield {
          reqs
            .map(r => {
              if (s.isCorruptingFM) {
                Request.all(s)
              } else if (s != min[FM]) {
                r.map(kv => kv._1 -> kv._2.containerShadow(s))
              } else {
                r.filterNot(p => reject(p._1))
              }
            })
            .foldLeft(Request.empty[FM])((acc, r) => {
              acc.mergeWith(r, (a, b) => worst[FM](a, b))
            })
        }
    )
  val loadO: OutputPort[Request[FM]] =
    OutputPort(
      VariableId(Symbol("loadO")),
      () =>
        for (access <- loadI.eval(); s <- fMAutomaton.o.eval()) yield {
          access
            .map(a => {
              a.map(kv => kv._1 -> kv._2.containerShadow(s))
            })
            .foldLeft(Request.empty[FM])((acc, r) => {
              acc.mergeWith(r, (a, b) => worst[FM](a, b))
            })
        }
    )
}

object SimpleTransporter {
  def apply[T: IsCriticityOrdering: IsFinite: IsShadowOrdering](
      id: TransporterId,
      reject: ((InitiatorId, TargetId)) => Boolean
  )(implicit
      context: Builder[SubComponent] with Owner
  ): SimpleTransporter[T] = {
    val result = new SimpleTransporter(id, reject)
    context.toBuild.getOrElseUpdate(
      id.name,
      () => SubComponent(id.name, result.toCecilia)
    )
    context.portOwner(result.loadO.id) = result
    context.portOwner(result.storeO.id) = result
    context.portOwner(result.loadI.id) = result
    context.portOwner(result.storeI.id) = result
    context.componentOwner(result.fMAutomaton) = result
    result
  }
}

//TODO Refactoring with Simple transporter
class Virtualizer[FM: IsCriticityOrdering: IsFinite: IsShadowOrdering] private (
    val id: TransporterId,
    val reject: ((InitiatorId, TargetId)) => Boolean
)(implicit owner: Owner)
    extends BasicTransporter[FM] {
  val storeI: InputPort[List[Request[FM]]] =
    InputPort[List[Request[FM]]](Symbol("storeI"))
  val storeO: OutputPort[Request[FM]] =
    OutputPort(
      VariableId(Symbol("storeO")),
      () =>
        for (reqs <- storeI.eval(); s <- fMAutomaton.o.eval()) yield {
          reqs
            .map(r => {
              if (s.isCorruptingFM) {
                Request.all(s)
              } else if (s != min[FM]) {
                r.map(kv => kv._1 -> kv._2.containerShadow(s))
              } else {
                r.filterNot(p => reject(p._1))
              }
            })
            .foldLeft(Request.empty[FM])((acc, r) => {
              acc.mergeWith(r, (a, b) => worst[FM](a, b))
            })
        }
    )
  val loadO: OutputPort[Request[FM]] =
    OutputPort(
      VariableId(Symbol("loadO")),
      () =>
        for (access <- loadI.eval(); s <- fMAutomaton.o.eval()) yield {
          access
            .map(a => {
              a.map(kv => kv._1 -> kv._2.containerShadow(s))
            })
            .foldLeft(Request.empty[FM])((acc, r) => {
              acc.mergeWith(r, (a, b) => worst[FM](a, b))
            })
        }
    )
}

object Virtualizer {
  def apply[T: IsCriticityOrdering: IsFinite: IsShadowOrdering](
      id: TransporterId,
      reject: ((InitiatorId, TargetId)) => Boolean
  )(implicit context: Builder[SubComponent] with Owner): Virtualizer[T] = {
    val result = new Virtualizer(id, reject)
    context.toBuild.getOrElseUpdate(
      id.name,
      () => SubComponent(id.name, result.toCecilia)
    )
    context.portOwner(result.loadO.id) = result
    context.portOwner(result.storeO.id) = result
    context.portOwner(result.loadI.id) = result
    context.portOwner(result.storeI.id) = result
    context.componentOwner(result.fMAutomaton) = result
    result
  }
}

class Initiator[FM: IsCriticityOrdering: IsFinite: IsShadowOrdering] private (
    val id: InitiatorId
)(implicit owner: Owner)
    extends Transporter[FM] {
  val storeI: InputPort[List[TargetStatus[FM]]] =
    InputPort[List[TargetStatus[FM]]](Symbol("storeI"))
  val storeO: OutputPort[Request[FM]] =
    OutputPort(
      VariableId(Symbol("storeO")),
      () =>
        for (reqs <- storeI.eval(); s <- fMAutomaton.o.eval()) yield {
          reqs
            .map(r => {
              if (s.isCorruptingFM) {
                Request.all(s)
              } else if (s != min[FM]) {
                r.map(kv => (id, kv._1) -> kv._2.containerShadow(s))
              } else {
                r.map(p => (id, p._1) -> p._2)
              }
            })
            .foldLeft(Request.empty[FM])((acc, r) => {
              acc.mergeWith(r, (a, b) => worst[FM](a, b))
            })
        }
    )
  val loadO: OutputPort[TargetStatus[FM]] =
    OutputPort(
      VariableId(Symbol("loadO")),
      () =>
        for (access <- loadI.eval(); s <- fMAutomaton.o.eval()) yield {
          access
            .map(a => {
              a.collect({
                case ((i, t), v) if i == id => t -> v.containerShadow(s)
              })
            })
            .foldLeft(TargetStatus.empty[FM])((acc, r) => {
              acc.mergeWith(r, (a, b) => worst[FM](a, b))
            })
        }
    )
}

object Initiator {
  def apply[T: IsCriticityOrdering: IsFinite: IsShadowOrdering](
      id: InitiatorId
  )(implicit context: Builder[SubComponent] with Owner): Initiator[T] = {
    val result = new Initiator(id)
    context.toBuild.getOrElseUpdate(
      id.name,
      () => SubComponent(id.name, result.toCecilia)
    )
    context.portOwner(result.loadO.id) = result
    context.portOwner(result.storeO.id) = result
    context.portOwner(result.loadI.id) = result
    context.portOwner(result.storeI.id) = result
    context.componentOwner(result.fMAutomaton) = result
    result
  }
}
