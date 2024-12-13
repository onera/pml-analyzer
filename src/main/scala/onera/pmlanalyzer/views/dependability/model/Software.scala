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

import onera.pmlanalyzer.views.dependability.exporters._
import onera.pmlanalyzer.views.dependability.model.CustomTypes.TargetStatus
import onera.pmlanalyzer.views.dependability.operators._

trait Software[FM] extends Component {
  val id: SoftwareId
  val loadI: InputPort[TargetStatus[FM]] =
    InputPort[TargetStatus[FM]](Symbol("loadI"))
  val storeO: OutputPort[TargetStatus[FM]]
}

/** @param id
  *   name of the software
  * @param softwareState
  *   core's state and loads impact on software state
  * @param stores
  *   core's state and stores impact on software stores
  * @tparam FM
  *   type of the failure modes
  */
class Application[FM: IsCriticityOrdering: IsFinite] private (
    val id: SoftwareId,
    val softwareState: (Variable[FM], Variable[TargetStatus[FM]]) => Expr[FM],
    val stores: (Variable[FM], Variable[TargetStatus[FM]]) => Expr[
      TargetStatus[FM]
    ]
) extends Software[FM] {
  val coreState: InputPort[FM] = InputPort(Symbol("coreState"))
  val storeO: OutputPort[TargetStatus[FM]] =
    OutputPort(
      VariableId(Symbol("storeO")),
      () => stores(coreState, loadI).eval()
    )
  val stateO: OutputPort[FM] =
    OutputPort(
      VariableId(Symbol("stateO")),
      () => softwareState(coreState, loadI).eval()
    )
}

object Application {
  def apply[FM: IsCriticityOrdering: IsFinite](
      id: SoftwareId,
      softwareState: (Variable[FM], Variable[TargetStatus[FM]]) => Expr[FM],
      stores: (Variable[FM], Variable[TargetStatus[FM]]) => Expr[
        TargetStatus[FM]
      ]
  )(implicit ev: Builder[SubComponent] with Owner): Application[FM] = {
    val result = new Application(id, softwareState, stores)
    ev.portOwner(result.coreState.id) = result
    ev.portOwner(result.storeO.id) = result
    ev.portOwner(result.loadI.id) = result
    ev.portOwner(result.stateO.id) = result
    ev.toBuild.getOrElseUpdate(
      id.name,
      () => SubComponent(id.name, result.toCecilia)
    )
    result
  }
}

class Descriptor[FM: IsCriticityOrdering: IsFinite] private (
    val id: SoftwareId,
    val transferts: List[Copy]
)(implicit owner: Owner)
    extends Software[FM] {
  val storeO: OutputPort[TargetStatus[FM]] = OutputPort(
    VariableId(Symbol("requestO")),
    () => {
      for (loadStatus <- loadI.eval()) yield {
        val copies = transferts.map(t =>
          t -> worst(
            (t.targetNeeded
              .map(
                loadStatus
              ))
              .toSeq: _*
          ) // TODO Raise error when the status cannot be computed => connection error
        )
        val reqs = copies.map(p =>
          TargetStatus(p._1.targetWritten.map(_ -> p._2).toSeq: _*)
        )
        reqs.foldLeft(TargetStatus.empty[FM])((acc, r) => {
          acc.mergeWith(r, (a, b) => worst(a, b))
        })
      }
    }
  )
}

object Descriptor {
  def apply[FM: IsCriticityOrdering: IsFinite](
      id: SoftwareId,
      transferts: List[Copy]
  )(implicit context: Builder[SubComponent] with Owner): Descriptor[FM] = {
    val result = new Descriptor(id, transferts)
    context.toBuild.getOrElseUpdate(
      id.name,
      () => SubComponent(id.name, result.toCecilia)
    )
    context.portOwner(result.loadI.id) = result
    context.portOwner(result.storeO.id) = result
    result
  }
}
