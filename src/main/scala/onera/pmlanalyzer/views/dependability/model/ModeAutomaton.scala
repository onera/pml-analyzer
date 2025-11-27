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

package onera.pmlanalyzer.views.dependability.model

import onera.pmlanalyzer.views.dependability.model.Direction.{
  Constant,
  Degradation,
  Reparation
}
import onera.pmlanalyzer.views.dependability.operators.*

/** Base trait for all automaton-like component
  *
  * @tparam T
  *   Possible modes of the automaton
  */
abstract class ModeAutomaton[T: IsCriticityOrdering: IsFinite]
    extends Component {
  val events: Set[Event]
  val transitions: Set[Transition[T]]
  val initialState: T
  private var state: T = initialState
  private var nextState: Option[T] = None

  def getState: T = state

  def fire(e: Event): Unit =
    fireable(e) match {
      case None    => throw new Exception(s"$e is not fireable")
      case Some(t) => state = t.computeNewState()
    }

  def engage(e: Event): Unit = for (t <- fireable(e))
    yield nextState = Some(t.computeNewState())

  def update(): Unit = for (s <- nextState) yield state = s

  def fireable(e: Event): Option[Transition[T]] = {
    transitions.filter(t => t.e == e && t.guard()) match {
      case s if s.isEmpty   => None
      case s if s.size == 1 => Some(s.head)
      case s =>
        throw new Exception(
          s"non deterministic automaton, $s are available for $e"
        )
    }
  }

  def direction(e: Event): Option[Direction] = {
    for (t <- fireable(e); nextState = t.computeNewState()) yield {
      if (nextState == state)
        Constant
      else if (nextState < state)
        Reparation
      else
        Degradation
    }
  }
}

abstract class FMAutomaton[T: IsCriticityOrdering: IsFinite]
    extends ModeAutomaton[T] {
  val initialState: T
  val eventMap: Map[Symbol, Event]
  val o: OutputPort[T]
  val outputPorts: Set[OutputPort[T]]
  val transitions: Set[Transition[T]]
  val id: AutomatonId
}

class SimpleFMAutomaton[T: IsCriticityOrdering: IsFinite] private (
    val id: AutomatonId,
    val initialState: T
) extends FMAutomaton[T] {
  val events: Set[Event] =
    allWithNone[T].map(x => StochasticEvent(x.name, this)).toSet
  val eventMap: Map[Symbol, Event] = events.map(e => e.name -> e).toMap
  val transitions: Set[Transition[T]] =
    allWithNone[T]
      .map(to =>
        Transition(
          () => getState < to,
          eventMap(to.name),
          () => to
        )
      )
      .toSet
  val o: OutputPort[T] =
    OutputPort(VariableId(Symbol("o")), () => Some(getState))
  val outputPorts: Set[OutputPort[T]] = Set(o)
}

object SimpleFMAutomaton {
  def apply[T: IsCriticityOrdering: IsFinite](id: AutomatonId, initialState: T)(
      implicit owner: Owner
  ): SimpleFMAutomaton[T] = {
    val r = new SimpleFMAutomaton[T](id, initialState)
    owner.portOwner(r.o.id) = r
    r
  }
}

class InputFMAutomaton[T: IsCriticityOrdering: IsFinite: IsShadowOrdering](
    val id: AutomatonId,
    val initialState: T
) extends FMAutomaton[T] {
  val in: InputPort[T] = InputPort(Symbol("i"))
  val inputPorts: Set[InputPort[T]] = Set(in)
  val epsilon: DeterministicEvent[T] =
    DeterministicEvent(Symbol("espilon"), this, 0)
  val events: Set[Event] = allWithNone[T]
    .map(x => StochasticEvent(x.name, this))
    .toSet[Event] + epsilon
  val eventMap: Map[Symbol, Event] = events.map(e => e.name -> e).toMap
  val transitions: Set[Transition[T]] =
    allWithNone[T]
      .map(to => Transition(() => getState < to, eventMap(to.name), () => to))
      .toSet +
      Transition(
        () =>
          (for (i <- in.eval())
            yield i.inputShadow(getState) != getState) getOrElse false,
        epsilon,
        () => in.eval().get.inputShadow(getState)
      )
  val o: OutputPort[T] =
    OutputPort(VariableId(Symbol("o")), () => Some(getState))
  val outputPorts: Set[OutputPort[T]] = Set(o)
}

object InputFMAutomaton {
  def apply[T: IsCriticityOrdering: IsFinite: IsShadowOrdering](
      id: AutomatonId,
      initialState: T
  )(implicit owner: Owner): InputFMAutomaton[T] = {
    val r = new InputFMAutomaton[T](id, initialState)
    owner.portOwner(r.o.id) = r
    owner.portOwner(r.in.id) = r
    r
  }
}
