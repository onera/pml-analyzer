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

package onera.pmlanalyzer.views.dependability.exporters

import onera.pmlanalyzer.views.dependability.exporters.CeciliaExporter.Aux
import onera.pmlanalyzer.views.dependability.exporters.GenericImage._
import onera.pmlanalyzer.views.dependability.exporters.PhylogFolder.phylogTargetFolder
import onera.pmlanalyzer.views.dependability.model._
import onera.pmlanalyzer.views.dependability.operators._

trait TargetCeciliaExporter {
  self: TypeCeciliaExporter
    with AutomatonCeciliaExporter
    with BasicOperationCeciliaExporter =>

  implicit def inputDepTargetExporter[
      FM: IsCriticityOrdering: IsFinite: IsShadowOrdering
  ]: Aux[InputDepTarget[FM], EquipmentModel] =
    new CeciliaExporter[InputDepTarget[FM]] {
      type R = EquipmentModel

      def toCecilia(x: InputDepTarget[FM]): EquipmentModel = {
        val tyype = typeModel[(InitiatorId, TargetId), FM]
        val iTyype = typeModel[InitiatorId, FM]
        val in =
          mkVariableName(x.storeI.id, tyype, In, x.storeI.eval().get.size)
        val out = Flow(x.loadO.id.name, tyype, Out)
        val obs = Flow(Symbol("storeO"), iTyype, Out)
        val direction =
          Flow(Symbol("direction"), typeModel[Direction.Value], Out)
        val fire = Flow(Symbol("fire"), typeModel[Fire.Value], In)
        val componentExport = x.fMAutomaton.toCecilia
        val automaton = SubComponent(x.fMAutomaton.id.name, componentExport)
        val myFields = allOf[(InitiatorId, TargetId)].collect {
          case p @ (_, t) if t == x.id => p.name.name
        }
        val otherFields = allOf[(InitiatorId, TargetId)].collect {
          case p @ (_, t) if t != x.id => p.name.name
        }
        val (worstFlow, worstAssertions, worstSub) = mkWorstSub(
          myFields.flatMap { f => in.map(i => s"$i^$f") }.toSet
        )
        val inAssertion =
          s"$automaton.${x.fMAutomaton.in.id} = $worstFlow;"
        val outAssertions =
          myFields.map(f => s"$out^$f = $automaton.${x.fMAutomaton.o.id};") ++
            otherFields.map(f => s"$out^$f = ${noneOf[FM].name.name};")
        val worstByIni = allOf[InitiatorId]
          .map(f =>
            f -> mkWorstSub(in.map(i => s"$i^${(f, x.id).name.name}").toSet)
          )
          .toMap
        val obsSub =
          allOf[InitiatorId]
            .map(f =>
              f ->
                mkContainerShadowSub(
                  Symbol(s"shadowStore$f"),
                  s"${worstByIni(f)._1}",
                  s"$automaton.${x.fMAutomaton.o}"
                )
            )
            .toMap
        EquipmentModel(
          x.id.name,
          phylogTargetFolder,
          phylogBlockBlue,
          phylogBlockGreen :: phylogBlockRed :: Nil,
          in :+ out :+ direction :+ fire :+ obs,
          obsSub.values.map(_._3).toList ++ worstByIni.values.map(
            _._3
          ) :+ automaton :+ worstSub,
          Nil,
          Nil,
          s"""assert
           |// computing worst store on target
           |${worstAssertions.sorted.mkString("\n")}
           |$inAssertion
           |
           |// automaton evolution management
           |$direction = $automaton.$direction;
           |$automaton.$fire = $fire;
           |
           |// load status according to component state
           |${outAssertions.sorted.mkString("\n")}
           |
           |// computing store status from component state
           |${worstByIni.values
              .flatMap(_._2)
              .toList
              .distinct
              .sorted
              .mkString("\n")}
           |${
            obsSub.values
              .flatMap(_._2)
              .toList
              .distinct
              .sorted
              .mkString("\n")
          }
           |
           |// observator of store states according to component state
           |${obsSub
              .map(p => s"$obs^${p._1} = ${p._2._1};")
              .toList
              .sorted
              .mkString("\n")}
           |
           |// icone management
           |icone = (if $automaton.${x.fMAutomaton.o.id} = ${min[
              FM
            ].name.name} then 1 else 2);
         """.stripMargin
        )
      }
    }

  implicit def inputInDepTargetIsExportable[
      FM: IsCriticityOrdering: IsFinite: IsShadowOrdering
  ]: Aux[InputInDepTarget[FM], EquipmentModel] =
    new CeciliaExporter[InputInDepTarget[FM]] {
      type R = EquipmentModel

      def toCecilia(x: InputInDepTarget[FM]): EquipmentModel = {
        val tyype = typeModel[(InitiatorId, TargetId), FM]
        val iTyype = typeModel[InitiatorId, FM]
        val in = Flow(x.storeI.id.name, tyype, In)
        val out = Flow(x.loadO.id.name, tyype, Out)
        val obs = Flow(Symbol("storeO"), iTyype, Out)
        val componentExport = x.fMAutomaton.toCecilia
        val sub = SubComponent(x.fMAutomaton.id.name, componentExport)
        val myFields = allOf[(InitiatorId, TargetId)].collect {
          case p @ (_, t) if t == x.id => p.name.name
        }
        val otherFields = allOf[(InitiatorId, TargetId)].collect {
          case p @ (_, t) if t != x.id => p.name.name
        }
        val outAssertions =
          myFields.map(f => s"$out^$f = $sub.${x.fMAutomaton.o.id};") ++
            otherFields.map(f => s"$out^$f = ${noneOf[FM].name.name};")
        val obsSub =
          allOf[InitiatorId]
            .map(f =>
              f ->
                mkContainerShadowSub(
                  Symbol(s"shadowStore$f"),
                  s"$in^${(f, x.id).name.name}",
                  s"$sub.${x.fMAutomaton.o}"
                )
            )
            .toMap

        EquipmentModel(
          x.id.name,
          phylogTargetFolder,
          phylogBlockBlue,
          phylogBlockGreen :: phylogBlockRed :: Nil,
          in :: out :: obs :: Nil,
          obsSub.values.map(_._3).toList :+ sub,
          Nil,
          Nil,
          s"""assert
           |// subcomponent assertions
           |${
            obsSub.values
              .flatMap(_._2)
              .toList
              .distinct
              .sorted
              .mkString("\n")
          }
           |
           |// resulting store assertions
           |${obsSub
              .map(p => s"$obs^${p._1} = ${p._2._1};")
              .toList
              .sorted
              .mkString("\n")}
           |
           |// load status according to component state
           |${outAssertions.sorted.mkString("\n")}
           |
           |
           |//icon management
           |icone = (if $sub.${x.fMAutomaton.o.id} = ${min[FM]} then 1 else 2);
         """.stripMargin
        )
      }
    }
}
