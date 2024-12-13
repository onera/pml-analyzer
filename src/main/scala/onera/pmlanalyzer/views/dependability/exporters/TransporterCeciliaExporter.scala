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
import onera.pmlanalyzer.views.dependability.exporters.GenericImage._
import onera.pmlanalyzer.views.dependability.exporters.PhylogFolder.phylogTransporterFolder
import onera.pmlanalyzer.views.dependability.model._
import onera.pmlanalyzer.views.dependability.operators._

trait TransporterCeciliaExporter {
  self: TypeCeciliaExporter
    with AutomatonCeciliaExporter
    with BasicOperationCeciliaExporter =>

  implicit def simpleTransporterIsExportable[
      FM: IsCriticityOrdering: IsFinite: IsShadowOrdering
  ]: Aux[SimpleTransporter[FM], EquipmentModel] =
    new CeciliaExporter[SimpleTransporter[FM]] {
      type R = EquipmentModel

      def toCecilia(x: SimpleTransporter[FM]): EquipmentModel = {
        val requestType = typeModel[(InitiatorId, TargetId), FM]
        val requestI = mkVariableName(
          x.storeI.id,
          requestType,
          In,
          x.storeI.eval().get.size
        ) // TODO dangerous
        val accessI = mkVariableName(
          x.loadI.id,
          requestType,
          In,
          x.loadI.eval().get.size
        ) // TODO dangerous
        val componentExport = x.fMAutomaton.toCecilia
        val automaton =
          SubComponent(x.fMAutomaton.id.name, componentExport) // TODO Placement
        val subStateName = s"$automaton.${x.fMAutomaton.o.id}"
        val requestShadowMap = requestType.fields
          .map(field =>
            field ->
              requestI.map(r =>
                mkContainerShadowSub(
                  Symbol(s"shadowRequest$r$field"),
                  s"$r^$field",
                  s"$subStateName"
                )
              )
          )
          .toMap
        val accessShadowMap = requestType.fields
          .map(field =>
            field ->
              accessI.map(r =>
                mkContainerShadowSub(
                  Symbol(s"shadowAccess$r$field"),
                  s"$r^$field",
                  s"$subStateName"
                )
              )
          )
          .toMap
        val worstShadowRequestMap = requestType.fields
          .map(field =>
            field ->
              mkWorstSub(requestShadowMap(field).map { _._1 }.toSet)
          )
          .toMap
        val worstShadowAccessMap = requestType.fields
          .map(field =>
            field ->
              mkWorstSub(accessShadowMap(field).map { _._1 }.toSet)
          )
          .toMap
        val wostRequestMap = requestType.fields
          .map(field =>
            field ->
              mkWorstSub(requestI.map(r => s"$r^$field").toSet)
          )
          .toMap
        val wostAccessMap = requestType.fields
          .map(field =>
            field ->
              mkWorstSub(accessI.map(r => s"$r^$field").toSet)
          )
          .toMap
        val requestO = Flow(x.storeO.id.name, requestType, Out)
        val accessO = Flow(x.loadO.id.name, requestType, Out)

        val rejectMap =
          allOf[(InitiatorId, TargetId)].map(p => p.name -> x.reject(p)).toMap

        val nonCoruptState =
          allOf[FM].filterNot(fm => fm.isCorruptingFM || fm == min[FM])
        val requestOAssertions = requestType.fields
          .sortBy(_.name)
          .map(field =>
            s"""$requestO^$field =
           |${
                if (rejectMap(field.name))
                  s" ${noneOf[FM].name.name};"
                else
                  s"""case {${allOf[FM]
                      .map(state =>
                        s"$subStateName = $state : ${worstShadowRequestMap(field)._1},"
                      )
                      .mkString("\n")}
             |else ${wostRequestMap(field)._1}};""".stripMargin
              }""".stripMargin
          )
        val accessOAssertions = requestType.fields
          .sortBy(_.name)
          .map(field =>
            s"""$accessO^$field =
           |${
                if (rejectMap(field.name))
                  s" ${noneOf[FM].name.name};"
                else
                  s"""case { ${allOf[FM]
                      .map(state =>
                        s"$subStateName = $state : ${worstShadowAccessMap(field)._1},"
                      )
                      .mkString("\n")}
               |else ${wostAccessMap(field)._1}};""".stripMargin
              }""".stripMargin
          )

        EquipmentModel(
          x.id.name,
          phylogTransporterFolder,
          phylogBlockBlue,
          phylogBlockGreen :: phylogBlockRed :: Nil,
          requestI ++ accessI :+ requestO :+ accessO,
          (requestShadowMap.values ++ accessShadowMap.values)
            .flatMap(_.map { _._3 })
            .toList ++
            (worstShadowAccessMap.values ++ worstShadowRequestMap.values ++ wostAccessMap.values ++ wostRequestMap.values)
              .map { _._3 } :+ automaton,
          Nil,
          Nil,
          s"""assert
           |// shadowing application on each input field
           |${(requestShadowMap.values ++ accessShadowMap.values)
              .flatMap(_.flatMap { _._2 })
              .toList
              .sorted
              .mkString("\n")}
           |
           |// worst among shadowed inputs
           |${(worstShadowAccessMap.values ++ worstShadowRequestMap.values)
              .flatMap { _._2 }
              .toList
              .sorted
              .mkString("\n")}
           |
           |// worst among non shadowed inputs
           |${(wostAccessMap.values ++ wostRequestMap.values)
              .flatMap { _._2 }
              .toList
              .sorted
              .mkString("\n")}
           |
           |// select store status according to component state
           |${requestOAssertions.sorted.mkString("\n")}
           |
           |// select load status according to component state
           |${accessOAssertions.sorted.mkString("\n")}
           |
           |// icone management
           |icone = (if $automaton.${x.fMAutomaton.o.id} = ${min[
              FM
            ].name.name} then 1 else 2);
         """.stripMargin
        )
      }
    }

  implicit def VirtualizerIsExportable[
      FM: IsCriticityOrdering: IsFinite: IsShadowOrdering
  ]: Aux[Virtualizer[FM], EquipmentModel] =
    new CeciliaExporter[Virtualizer[FM]] {
      type R = EquipmentModel

      def toCecilia(x: Virtualizer[FM]): EquipmentModel = {
        val requestType = typeModel[(InitiatorId, TargetId), FM]
        val requestI = mkVariableName(
          x.storeI.id,
          requestType,
          In,
          x.storeI.eval().get.size
        ) // TODO dangerous
        val accessI = mkVariableName(
          x.loadI.id,
          requestType,
          In,
          x.loadI.eval().get.size
        ) // TODO dangerous
        val componentExport = x.fMAutomaton.toCecilia
        val automaton =
          SubComponent(x.fMAutomaton.id.name, componentExport) // TODO Placement
        val subStateName = s"$automaton.${x.fMAutomaton.o.id}"
        val requestShadowMap = requestType.fields
          .map(field =>
            field ->
              requestI.map(r =>
                mkContainerShadowSub(
                  Symbol(s"shadowRequest$r$field"),
                  s"$r^$field",
                  s"$subStateName"
                )
              )
          )
          .toMap
        val accessShadowMap = requestType.fields
          .map(field =>
            field ->
              accessI.map(r =>
                mkContainerShadowSub(
                  Symbol(s"shadowAccess$r$field"),
                  s"$r^$field",
                  s"$subStateName"
                )
              )
          )
          .toMap
        val worstShadowRequestMap = requestType.fields
          .map(field =>
            field ->
              mkWorstSub(requestShadowMap(field).map { _._1 }.toSet)
          )
          .toMap
        val worstShadowAccessMap = requestType.fields
          .map(field =>
            field ->
              mkWorstSub(accessShadowMap(field).map { _._1 }.toSet)
          )
          .toMap
        val wostRequestMap = requestType.fields
          .map(field =>
            field ->
              mkWorstSub(requestI.map(r => s"$r^$field").toSet)
          )
          .toMap
        val wostAccessMap = requestType.fields
          .map(field =>
            field ->
              mkWorstSub(accessI.map(r => s"$r^$field").toSet)
          )
          .toMap
        val requestO = Flow(x.storeO.id.name, requestType, Out)
        val accessO = Flow(x.loadO.id.name, requestType, Out)

        val rejectMap =
          allOf[(InitiatorId, TargetId)].map(p => p.name -> x.reject(p)).toMap

        val nonCoruptState =
          allOf[FM].filterNot(fm => fm.isCorruptingFM || fm == min[FM])
        val requestOAssertions = requestType.fields
          .sortBy(_.name)
          .map(field =>
            s"""$requestO^$field = case {
           |${allOf[FM]
                .filter(_.isCorruptingFM)
                .map(fm => s"$subStateName = $fm")
                .mkString(" or ")} : $subStateName,
           |${
                if (rejectMap(field.name))
                  s"else ${noneOf[FM].name.name}"
                else
                  s"""${nonCoruptState
                      .map(state =>
                        s"$subStateName = $state : ${worstShadowRequestMap(field)._1},"
                      )
                      .mkString("\n")}
               |else ${wostRequestMap(field)._1}""".stripMargin
              }
           |};""".stripMargin
          )
        val accessOAssertions = requestType.fields
          .sortBy(_.name)
          .map(field =>
            s"""$accessO^$field = case {
           |${allOf[FM]
                .filter(_.isCorruptingFM)
                .map(fm => s"$subStateName = $fm")
                .mkString(" or ")} : $subStateName,
           |${
                if (rejectMap(field.name))
                  s"else ${noneOf[FM].name.name}"
                else
                  s"""${nonCoruptState
                      .map(state =>
                        s"$subStateName = $state : ${worstShadowAccessMap(field)._1},"
                      )
                      .mkString("\n")}
               |else ${wostAccessMap(field)._1}""".stripMargin
              }
           |};""".stripMargin
          )

        EquipmentModel(
          x.id.name,
          phylogTransporterFolder,
          phylogBlockBlue,
          phylogBlockGreen :: phylogBlockRed :: Nil,
          requestI ++ accessI :+ requestO :+ accessO,
          (requestShadowMap.values ++ accessShadowMap.values)
            .flatMap(_.map { _._3 })
            .toList ++
            (worstShadowAccessMap.values ++ worstShadowRequestMap.values ++ wostAccessMap.values ++ wostRequestMap.values)
              .map { _._3 } :+ automaton,
          Nil,
          Nil,
          s"""assert
           |// shadowing application on each input field
           |${(requestShadowMap.values ++ accessShadowMap.values)
              .flatMap(_.flatMap { _._2 })
              .toList
              .sorted
              .mkString("\n")}
           |
           |// worst among shadowed inputs
           |${(worstShadowAccessMap.values ++ worstShadowRequestMap.values)
              .flatMap { _._2 }
              .toList
              .sorted
              .mkString("\n")}
           |
           |// worst among non shadowed inputs
           |${(wostAccessMap.values ++ wostRequestMap.values)
              .flatMap { _._2 }
              .toList
              .sorted
              .mkString("\n")}
           |
           |// select store status according to component state
           |${requestOAssertions.sorted.mkString("\n")}
           |
           |// select load status according to component state
           |${accessOAssertions.sorted.mkString("\n")}
           |
           |// icone management
           |icone = (if $automaton.${x.fMAutomaton.o.id} = ${min[
              FM
            ].name.name} then 1 else 2);
         """.stripMargin
        )
      }
    }

  implicit def initiatorIsExportable[
      FM: IsCriticityOrdering: IsFinite: IsShadowOrdering
  ]: Aux[Initiator[FM], EquipmentModel] = new CeciliaExporter[Initiator[FM]] {
    type R = EquipmentModel

    def toCecilia(x: Initiator[FM]): EquipmentModel = {
      val requestType = typeModel[(InitiatorId, TargetId), FM]
      val tgtStatusType = typeModel[TargetId, FM]
      val requestI = mkVariableName(
        x.storeI.id,
        tgtStatusType,
        In,
        x.storeI.eval().get.size
      ) // TODO dangerous
      val accessI = mkVariableName(
        x.loadI.id,
        requestType,
        In,
        x.loadI.eval().get.size
      ) // TODO dangerous
      val componentExport = x.fMAutomaton.toCecilia
      val automaton =
        SubComponent(x.fMAutomaton.id.name, componentExport) // TODO Placement
      val subStateName = s"$automaton.${x.fMAutomaton.o.id}"
      val requestShadowMap = allOf[TargetId]
        .map(field =>
          field ->
            requestI.map(r =>
              mkContainerShadowSub(
                Symbol(s"shadowRequest$r$field"),
                s"$r^$field",
                s"$subStateName"
              )
            )
        )
        .toMap
      val accessShadowMap = allOf[TargetId]
        .map(field =>
          field ->
            accessI.map(r =>
              mkContainerShadowSub(
                Symbol(s"shadowAccess$r$field"),
                s"$r^${(x.id, field).name.name}",
                s"$subStateName"
              )
            )
        )
        .toMap
      val worstShadowRequestMap = allOf[TargetId]
        .map(field =>
          field ->
            mkWorstSub(requestShadowMap(field).map { _._1 }.toSet)
        )
        .toMap
      val worstShadowAccessMap = allOf[TargetId]
        .map(field =>
          field ->
            mkWorstSub(accessShadowMap(field).map { _._1 }.toSet)
        )
        .toMap
      val wostRequestMap = allOf[TargetId]
        .map(field =>
          field ->
            mkWorstSub(requestI.map(r => s"$r^$field").toSet)
        )
        .toMap
      val wostAccessMap = allOf[TargetId]
        .map(field =>
          field ->
            mkWorstSub(
              accessI.map(r => s"$r^${{ (x.id, field).name.name }}").toSet
            )
        )
        .toMap
      val requestO = Flow(x.storeO.id.name, requestType, Out)
      val accessO = Flow(x.loadO.id.name, tgtStatusType, Out)

      val nonCoruptState =
        allOf[FM].filterNot(fm => fm.isCorruptingFM || fm == min[FM])
      val requestOAssertions = allOf[(InitiatorId, TargetId)]
        .sortBy(_.name)
        .map(field =>
          if (field._1 != x.id)
            s"$requestO^${field.name.name} = ${noneOf[FM]};"
          else
            s"""$requestO^${field.name.name} = case {
           |${allOf[FM]
                .filter(_.isCorruptingFM)
                .map(fm => s"$subStateName = $fm")
                .mkString(" or ")} : $subStateName,
           |${nonCoruptState
                .map(state =>
                  s"$subStateName = $state : ${worstShadowRequestMap(field._2)._1},"
                )
                .mkString("\n")}
           |else ${wostRequestMap(field._2)._1}
           |};""".stripMargin
        )
      val accessOAssertions = allOf[TargetId]
        .sortBy(_.name)
        .map(field =>
          s"""$accessO^$field = case {
           |${allOf[FM]
              .filter(_.isCorruptingFM)
              .map(fm => s"$subStateName = $fm")
              .mkString(" or ")} : $subStateName,
           |${nonCoruptState
              .map(state =>
                s"$subStateName = $state : ${worstShadowAccessMap(field)._1},"
              )
              .mkString("\n")}
           |else ${wostAccessMap(field)._1}
           |};""".stripMargin
        )

      EquipmentModel(
        x.id.name,
        phylogTransporterFolder,
        phylogBlockBlue,
        phylogBlockGreen :: phylogBlockRed :: Nil,
        requestI ++ accessI :+ requestO :+ accessO,
        (requestShadowMap.values ++ accessShadowMap.values)
          .flatMap(_.map { _._3 })
          .toList ++
          (worstShadowAccessMap.values ++ worstShadowRequestMap.values ++ wostAccessMap.values ++ wostRequestMap.values)
            .map { _._3 } :+ automaton,
        Nil,
        Nil,
        s"""assert
           |// shadowing application on each input field
           |${(requestShadowMap.values ++ accessShadowMap.values)
            .flatMap(_.flatMap { _._2 })
            .toList
            .distinct
            .sorted
            .mkString("\n")}
           |
           |// worst among shadowed inputs
           |${(worstShadowAccessMap.values ++ worstShadowRequestMap.values)
            .flatMap { _._2 }
            .toList
            .distinct
            .sorted
            .mkString("\n")}
           |
           |// worst among non shadowed inputs
           |${(wostAccessMap.values ++ wostRequestMap.values)
            .flatMap { _._2 }
            .toList
            .distinct
            .sorted
            .mkString("\n")}
           |
           |// select store status according to component state
           |${requestOAssertions.distinct.sorted.mkString("\n")}
           |
           |// select load status according to component state
           |${accessOAssertions.distinct.sorted.mkString("\n")}
           |
           |// icone management
           |icone = (if $automaton.${x.fMAutomaton.o.id} = ${min[
            FM
          ].name.name} then 1 else 2);
         """.stripMargin
      )
    }
  }
}
