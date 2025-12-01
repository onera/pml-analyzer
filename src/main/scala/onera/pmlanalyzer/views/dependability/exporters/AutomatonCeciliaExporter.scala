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

package onera.pmlanalyzer.views.dependability.exporters

import onera.pmlanalyzer.views.dependability.exporters.CeciliaExporter.Aux
import onera.pmlanalyzer.views.dependability.exporters.GenericImage.*
import onera.pmlanalyzer.views.dependability.exporters.PhylogFolder.automatonFamilyFolder
import onera.pmlanalyzer.views.dependability.model.{
  Direction,
  Fire,
  InputFMAutomaton,
  SimpleFMAutomaton
}
import onera.pmlanalyzer.*
import onera.pmlanalyzer.views.dependability.operators.{
  IsCriticalityOrdering,
  IsFinite,
  IsShadowOrdering
}

private[pmlanalyzer] trait AutomatonCeciliaExporter {
  self: TypeCeciliaExporter =>
  implicit def simpleFMAutomatonIsExportable[T: IsCriticalityOrdering: IsFinite]
      : Aux[SimpleFMAutomaton[T], ComponentModel] =
    new CeciliaExporter[SimpleFMAutomaton[T]] {
      type R = ComponentModel

      def toCecilia(x: SimpleFMAutomaton[T]): ComponentModel = {
        val events =
          allOf[T]
            .flatMap(to =>
              allOf[T].collect({
                case from if from < to =>
                  StochasticEventModel(x.eventMap(to.name).name)
              })
            )
            .toList
            .distinct
        val transitions =
          allOf[T]
            .flatMap(to =>
              allOf[T].collect({
                case from if from < to =>
                  s"s = ${from.name.name} |- ${x.eventMap(to.name).name.name} -> s := ${to.name.name};"
              })
            )
            .mkString("trans\n", "\n", "\n")
        val assertions =
          s"""assert
           |o = s;
           |icone = (if o = ${x.initialState.name.name} then 1 else 2);""".stripMargin

        ComponentModel(
          Symbol("inputIndepFMAutomaton"),
          automatonFamilyFolder,
          sourceBlueCircle,
          sourceGreenCircle :: sourceRedCross :: Nil,
          List(Flow(Symbol("o"), typeModel[T], Out)),
          events,
          List(State(Symbol("s"), typeModel[T], x.initialState.name.name)),
          transitions + assertions
        )
      }
    }

  implicit def inputFMAutomatonIsExportable[
      T: IsCriticalityOrdering: IsFinite: IsShadowOrdering
  ]: Aux[InputFMAutomaton[T], ComponentModel] =
    new CeciliaExporter[InputFMAutomaton[T]] {
      type R = ComponentModel

      def toCecilia(x: InputFMAutomaton[T]): ComponentModel = {
        val in = Flow(x.in.id.name, typeModel[T], In)
        val fire = Flow(Symbol("fire"), typeModel[Fire], In)
        val o = Flow(x.o.id.name, typeModel[T], Out)
        val state = State(Symbol("s"), typeModel[T], x.initialState.name.name)
        val nextState = Flow(Symbol("nextState"), typeModel[T], Local)
        val nextStateAssertion = allOf[T]
          .flatMap(to =>
            allOf[T].collect({
              case from if to.inputShadow(from) != from =>
                s"$in = ${to.name.name} and s = ${from.name.name} : ${to.name.name},"
            })
          )
          .mkString(s"$nextState = case {\n", "\n", s"\nelse $state};")
        val direction =
          Flow(Symbol("direction"), typeModel[Direction], Out)
        val directionAssertion = allOf[T]
          .flatMap(from =>
            allOf[T].collect {
              case to if from < to =>
                s"$state = $from and $nextState = $to : ${Direction.Degradation},"
              case to if from > to =>
                s"$state = $from and $nextState = $to : ${Direction.Reparation},"
            }
          )
          .mkString(
            s"$direction = case {\n",
            "\n",
            s"\nelse ${Direction.Constant}};"
          )
        val events =
          allOf[T]
            .flatMap(to =>
              allOf[T].collect({
                case from if from < to =>
                  StochasticEventModel(x.eventMap(to.name).name)
              })
            )
            .toList
            .distinct :+ DeterministicEventModel(x.epsilon.name)
        val transitionsFailure =
          allOf[T]
            .flatMap(to =>
              allOf[T].collect({
                case from if from < to =>
                  s"s = ${from.name.name} |- ${x.eventMap(to.name).name.name} -> s := ${to.name.name};"
              })
            )
            .mkString("\n")
        ComponentModel(
          Symbol("inputDepFMAutomaton"),
          automatonFamilyFolder,
          functionBlueCircle,
          functionGreenCircle :: functionRedCross :: Nil,
          in :: o :: nextState :: direction :: fire :: Nil,
          events,
          state :: Nil,
          s"""trans
           |//if an update is asked by the scheduler then update the state
           |$fire = ${Fire.Apply} |- ${x.epsilon.name.name} -> $state := $nextState;
           |
           |//if updates must be performed by other components then hold the current state (so do nothing)
           |$fire = ${Fire.Wait} |- ${x.epsilon.name.name} -> ;
           |
           |//apply failure mode as soon as the event occurs, keeping in mind the SHADOW relation over failure modes
           |$transitionsFailure
           |
           |assert
           |$o = $state;
           |
           |$nextStateAssertion
           |
           |$directionAssertion
           |
           |icone = (if $o = ${x.initialState.name.name} then 1 else 2);
           |""".stripMargin
        )
      }
    }
}
